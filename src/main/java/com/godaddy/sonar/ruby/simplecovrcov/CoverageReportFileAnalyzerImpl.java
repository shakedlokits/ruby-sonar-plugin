package com.godaddy.sonar.ruby.simplecovrcov;

import com.godaddy.sonar.ruby.simplecovrcov.data.CoverageReport;
import com.godaddy.sonar.ruby.simplecovrcov.data.Mark;
import com.godaddy.sonar.ruby.simplecovrcov.data.Reporter;
import com.godaddy.sonar.ruby.simplecovrcov.data.ReporterItem;
import com.google.common.collect.Maps;
import org.sonar.api.measures.CoverageMeasuresBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

public class CoverageReportFileAnalyzerImpl implements CoverageReportFileAnalyzer {
    private CoverageSettings settings;

    public CoverageReportFileAnalyzerImpl(CoverageSettings settings) {
        this.settings = settings;
    }

    public Map<String, CoverageMeasuresBuilder> analyze(File file) throws IOException {
        CoverageReport coverageReport = readAndParseReportFile(file);
        return processCoverageReport(coverageReport);
    }

    private CoverageReport readAndParseReportFile(File file) throws IOException {
        return new CoverageReportFileParser(file).parse();
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

    /**
     * Merges hits count of a specified line with already existing coverage results for a given file.
     *
     * Coverage information for one file could be presented in several suites. So we have to merge all results per file.
     * Considering how {@link org.sonar.api.measures.CoverageMeasuresBuilder#setHits} works this a little bit
     * tricky method is needed: if you already have added some coverage info for the particular line, then you can not
     * add more information. Please, visit method source code for more understanding. It's very small :-)
     *
     * @param hitsCount
     * @param lineNumber
     * @param fileCoverage
     */
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
