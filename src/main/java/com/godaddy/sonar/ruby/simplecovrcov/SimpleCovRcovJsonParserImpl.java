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
import javafx.util.Pair;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.io.FileUtils;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import sun.tools.asm.Cover;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class SimpleCovRcovJsonParserImpl implements SimpleCovRcovJsonParser {
    private static final Logger LOG = Loggers.get(SimpleCovRcovJsonParserImpl.class);

    public Map<String, CoverageMeasuresBuilder> parse(File file) throws IOException {
        CoverageReport coverageReport = readAndParseReportFile(file);
        return processCoverageReport(coverageReport);

//        String fileString = FileUtils.readFileToString(file, "UTF-8");
//
//        JsonParser parser = new JsonParser();
//        JsonObject resultJsonObject = parser.parse(fileString).getAsJsonObject();
//
//        String coverageRootNode = "";
//        if(resultJsonObject.get("RSpec") != null) { coverageRootNode = "RSpec"; }
//        if(resultJsonObject.get("MiniTest") != null) { coverageRootNode = "MiniTest"; }
//
//        if(coverageRootNode.equals("")) { return coveredFiles; }
//
//        JsonObject coverageJsonObj = resultJsonObject.get(coverageRootNode).getAsJsonObject().get("coverage").getAsJsonObject();
//
//        // for each file in the coverage report
//        for (int j = 0; j < coverageJsonObj.entrySet().size(); j++) {
//            CoverageMeasuresBuilder fileCoverage = CoverageMeasuresBuilder.create();
//
//            String filePath = ((Map.Entry) coverageJsonObj.entrySet().toArray()[j]).getKey().toString();
//            LOG.debug("filePath " + filePath);
//
//            JsonArray coverageArray = coverageJsonObj.get(filePath).getAsJsonArray();
//
//            // for each line in the coverage array
//            for (int i = 0; i < coverageArray.size(); i++) {
//                Long line = null;
//
//                if (!coverageArray.get(i).isJsonNull()) {
//                    line = coverageArray.get(i).getAsLong();
//                }
//
//                Integer intLine = 0;
//                int lineNumber = i + 1;
//                if (line != null) {
//                    intLine = line.intValue();
//                    fileCoverage.setHits(lineNumber, intLine);
//                }
//            }
//            LOG.info("FILE COVERAGE = " + fileCoverage.getCoveredLines());
//            coveredFiles.put(filePath, fileCoverage);
//        }
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
        if (coverageReport.hasSeveralReporters()) {
            LOG.warn("Coverage report has several reporters! Notice, please, that only the first one will be considered by Sonar!");
        }
        return processReporter(coverageReport.getFirstReporter());
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

    private Map<String, CoverageMeasuresBuilder> processReporter(Reporter reporter) {
        Map<String, CoverageMeasuresBuilder> coveredFiles = Maps.newHashMap();
        for (ReporterItem reporterItem : reporter.getItems()) {
            CoverageMeasuresBuilder fileCoverage = processReporterItem(reporterItem);
            coveredFiles.put(reporterItem.getFilename(), fileCoverage);
        }
        return coveredFiles;
    }

    private CoverageMeasuresBuilder processReporterItem(ReporterItem reporterItem) {
        CoverageMeasuresBuilder fileCoverage = CoverageMeasuresBuilder.create();
        for (int markId = 0; markId < reporterItem.getMarks().size(); markId++) {
            Mark mark = (Mark)reporterItem.getMarks().toArray()[markId];
            if (!mark.getIsNull()) {
                int intLine = mark.getAsLong().intValue();
                int lineNumber = markId + 1;
                fileCoverage.setHits(lineNumber, intLine);
            }
        }
        return fileCoverage;
    }
}
