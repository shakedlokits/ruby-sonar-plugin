package com.godaddy.sonar.ruby.simplecovrcov;

import com.godaddy.sonar.ruby.simplecovrcov.data.CoverageReport;
import com.godaddy.sonar.ruby.simplecovrcov.data.Mark;
import com.godaddy.sonar.ruby.simplecovrcov.data.Reporter;
import com.godaddy.sonar.ruby.simplecovrcov.data.ReporterItem;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

public class SimpleCovRcovJsonParserImpl implements SimpleCovRcovJsonParser {
    private static final Logger LOG = Loggers.get(SimpleCovRcovJsonParserImpl.class);

    private CoverageSettings settings;

    public SimpleCovRcovJsonParserImpl(CoverageSettings settings) {
        this.settings = settings;
    }

    public Map<String, CoverageMeasuresBuilder> parse(File file) throws IOException {
        CoverageReport coverageReport = readAndParseReportFile(file);
        return processCoverageReport(coverageReport);
    }

    private CoverageReport readAndParseReportFile(File file) throws IOException {
        CoverageReport coverageReport = new CoverageReport();

        String fileString = FileUtils.readFileToString(file, "UTF-8");

        JsonParser parser = new JsonParser();
        JsonObject resultJsonObject = parser.parse(fileString).getAsJsonObject();

        for(Map.Entry coverageMapEntry : resultJsonObject.entrySet()){
            coverageReport.addReporter(buildReporter(coverageMapEntry));
        }
        return coverageReport;
    }

    private Map<String, CoverageMeasuresBuilder> processCoverageReport(CoverageReport coverageReport) {
        Map<String, CoverageMeasuresBuilder> coveredFiles = Maps.newHashMap();
        coverageReport.getReporters()
                .stream()
                .filter(reporter -> shouldProcessReporterWithName(reporter.getName()))
                .forEach(reporter -> processReporter(reporter, coveredFiles));
        return coveredFiles;
    }

    private Boolean shouldProcessReporterWithName(String reporterName) {
        return getSettings().processAllSuites() || getSettings().configuredSuitesNames().contains(reporterName);
    }

    private Reporter buildReporter(Map.Entry coverageMapEntry) {
        JsonObject coverageJsonObj = ((JsonObject)coverageMapEntry.getValue()).get("coverage").getAsJsonObject();
        String reporterName = coverageMapEntry.getKey().toString();
        Reporter reporter = new Reporter(reporterName);
        for(Map.Entry reportItemMapEntry : coverageJsonObj.entrySet()) {
            reporter.addItem(buildReporterItem(reportItemMapEntry));
        }
        return reporter;
    }

    private ReporterItem buildReporterItem(Map.Entry reporterItemMapEntry) {
        String filename = reporterItemMapEntry.getKey().toString();
        JsonArray marksJsonArr = ((JsonArray)reporterItemMapEntry.getValue());
        Collection<Mark> marks = new ArrayList<>();
        for(JsonElement marksEl : marksJsonArr) {
            marks.add(new Mark(marksEl.toString(), marksEl.isJsonNull()));
        }
        return new ReporterItem(filename, marks);
    }

    private Map<String, CoverageMeasuresBuilder> processReporter(Reporter reporter, Map<String, CoverageMeasuresBuilder> coveredFiles) {
        for (ReporterItem reporterItem : reporter.getItems()) {
            String filename = reporterItem.getFilename();
            CoverageMeasuresBuilder fileCoverage = coveredFiles.getOrDefault(filename, CoverageMeasuresBuilder.create());
            processReporterItem(reporterItem, fileCoverage);
            coveredFiles.put(reporterItem.getFilename(), fileCoverage);
        }
        return coveredFiles;
    }

    private void processReporterItem(ReporterItem reporterItem, CoverageMeasuresBuilder fileCoverage) {
        ArrayList<Mark> reporterItemMarks = (ArrayList<Mark>) reporterItem.getMarks();
        for (int markId = 0; markId < reporterItemMarks.size(); markId++) {
            Mark mark = reporterItemMarks.get(markId);
            processMark(mark, markId, fileCoverage);
        }
    }

    private void processMark(Mark mark, Integer index, CoverageMeasuresBuilder fileCoverage) {
        if (mark.getIsNull()) { return; }
        int hitsCount = mark.getAsLong().intValue();
        int lineNumber = index + 1;
        mergeHitsCounters(hitsCount, lineNumber, fileCoverage);
    }

    private void mergeHitsCounters(int hitsCount, int lineNumber, CoverageMeasuresBuilder fileCoverage) {
        SortedMap<Integer, Integer> hitsByLine = Maps.newTreeMap();
        hitsByLine.putAll(fileCoverage.getHitsByLine());
        int oldHits = hitsByLine.getOrDefault(lineNumber, 0);
        hitsByLine.put(lineNumber, oldHits + hitsCount);
        fileCoverage.reset();
        for(Map.Entry<Integer, Integer> entry : hitsByLine.entrySet()) {
            fileCoverage.setHits(entry.getKey(), entry.getValue());
        }
    }

    private CoverageSettings getSettings() {
        return settings;
    }
}
