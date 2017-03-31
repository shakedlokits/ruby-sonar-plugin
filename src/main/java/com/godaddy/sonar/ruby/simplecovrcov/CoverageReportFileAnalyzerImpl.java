package com.godaddy.sonar.ruby.simplecovrcov;

import com.godaddy.sonar.ruby.simplecovrcov.data.CoverageReport;
import com.godaddy.sonar.ruby.simplecovrcov.data.Mark;
import com.godaddy.sonar.ruby.simplecovrcov.data.Reporter;
import com.godaddy.sonar.ruby.simplecovrcov.data.ReporterItem;
import com.google.common.collect.Maps;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.measures.CoverageMeasuresBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

@BatchSide
@ExtensionPoint
public class CoverageReportFileAnalyzerImpl implements CoverageReportFileAnalyzer {
    private CoverageSettings settings;

    /**
     * Instantiates new analyzer instance
     *
     * @param settings an object with settings for coverage context
     */
    public CoverageReportFileAnalyzerImpl(CoverageSettings settings) {
        this.settings = settings;
    }

    /**
     * Analyzes a given coverage report file
     *
     * @param file an instance of a coverage report file
     * @return a map where a key is a file name and a value is a coverage information for this file
     * @throws IOException when coverage report file could not be read
     */
    public Map<String, CoverageMeasuresBuilder> analyze(File file) throws IOException {
        CoverageReport coverageReport = readAndParseReportFile(file);
        return processCoverageReport(coverageReport);
    }

    /**
     * Reads and parses coverage report file and provides high-level representation as data objects.
     *
     * Actually, for parsing {@link CoverageReportFileParser} is used. Please, take a look inside it for more
     * information regarding parsing mechanism.
     *
     * @param file an instance of a coverage report file
     * @return a data object which represents a coverage report file
     * @throws IOException when a coverage report file could not be read
     */
    private CoverageReport readAndParseReportFile(File file) throws IOException {
        return new CoverageReportFileParser(file).parse();
    }

    /**
     * Processes a whole coverage report
     *
     * @param coverageReport a data object with full information about coverage report
     * @return a map with coverage information per file
     */
    private Map<String, CoverageMeasuresBuilder> processCoverageReport(CoverageReport coverageReport) {
        Map<String, CoverageMeasuresBuilder> coveredFiles = Maps.newHashMap();
        // Iterate through all available reporters (test suites), but for first check
        // should it be taken for procession or not, and for that decision a settings are used.
        coverageReport.getReporters()
                .stream()
                .filter(reporter -> shouldProcessReporterWithName(reporter.getName()))
                .forEach(reporter -> processReporter(reporter, coveredFiles));
        return coveredFiles;
    }

    /**
     * Checks reporter by its name regarding availability for processing
     *
     * @param reporterName a name of a checking reporter
     * @return a boolean flag which specifies either should this reporter be processed or not
     */
    private Boolean shouldProcessReporterWithName(String reporterName) {
        return getSettings().processAllSuites() || getSettings().configuredSuitesNames().contains(reporterName);
    }

    /**
     * Processes a given reporter or test suite information
     *
     * @param reporter a data object which represents reporter or test suite in a report
     * @param coveredFiles a map with coverage information per file
     */
    private void processReporter(Reporter reporter, Map<String, CoverageMeasuresBuilder> coveredFiles) {
        // Iterate through all items or file inside a given reporter
        for (ReporterItem reporterItem : reporter.getItems()) {
            // For first fetch file name
            String filename = reporterItem.getFilename();
            // Try to find coverage builder (in a case, when file is represented in several test suites) and create new
            // one as a fallback for a case, when file is processing first time.
            CoverageMeasuresBuilder fileCoverage = coveredFiles.getOrDefault(filename, CoverageMeasuresBuilder.create());
            // Run processing reporter item
            processReporterItem(reporterItem, fileCoverage);
            // Replace or add new coverage builder for a given file
            coveredFiles.put(reporterItem.getFilename(), fileCoverage);
        }
    }

    /**
     * Processes one reporter item
     *
     * @param reporterItem a data object which represents a file in a test suite
     * @param fileCoverage an instance of a coverage builder for SonarQube
     */
    private void processReporterItem(ReporterItem reporterItem, CoverageMeasuresBuilder fileCoverage) {
        ArrayList<Mark> reporterItemMarks = (ArrayList<Mark>) reporterItem.getMarks();
        // Iterate through all marks inside this reporter item and process them one by one
        for (int markId = 0; markId < reporterItemMarks.size(); markId++) {
            Mark mark = reporterItemMarks.get(markId);
            processMark(mark, markId + 1, fileCoverage);
        }
    }

    /**
     * Processes one given mark and fills hits for it
     *
     * @param mark a data object with one mark information, which represents one line
     * @param lineNumber an index of this mark
     * @param fileCoverage an instance of a coverage builder for SonarQube
     */
    private void processMark(Mark mark, Integer lineNumber, CoverageMeasuresBuilder fileCoverage) {
        // If mark (or line) has `null` value then skip further processing
        if (mark.getIsNull()) { return; }
        // Read hits count as integer value
        int hitsCount = mark.getAsLong().intValue();
        // Call some magic merging hits counters method
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
     * @param hitsCount a hits count value for a line
     * @param lineNumber an index of a line in the file
     * @param fileCoverage an instance of a coverage builder for SonarQube
     */
    private void mergeHitsCounters(int hitsCount, int lineNumber, CoverageMeasuresBuilder fileCoverage) {
        // Instantiate some temporal variable to keep all current information about hits by line from builder
        SortedMap<Integer, Integer> hitsByLine = Maps.newTreeMap();
        // Fill this variable by hits by line information from coverage builder
        hitsByLine.putAll(fileCoverage.getHitsByLine());
        // Get current hits value for a given line by its number. If the line with such number is not represented
        // in a coverage builder, then lets use default value `0`.
        int oldHits = hitsByLine.getOrDefault(lineNumber, 0);
        // Update hits information for a given line by sum of old value and new hits value
        hitsByLine.put(lineNumber, oldHits + hitsCount);
        // Reset hits by line information
        fileCoverage.reset();
        // Iterate through all entries in the temporal variable and restore hits by line information into coverage builder
        for(Map.Entry<Integer, Integer> entry : hitsByLine.entrySet()) {
            fileCoverage.setHits(entry.getKey(), entry.getValue());
        }
    }

    private CoverageSettings getSettings() {
        return settings;
    }
}
