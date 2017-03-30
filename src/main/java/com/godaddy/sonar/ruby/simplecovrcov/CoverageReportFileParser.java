package com.godaddy.sonar.ruby.simplecovrcov;

import com.godaddy.sonar.ruby.simplecovrcov.data.CoverageReport;
import com.godaddy.sonar.ruby.simplecovrcov.data.Mark;
import com.godaddy.sonar.ruby.simplecovrcov.data.Reporter;
import com.godaddy.sonar.ruby.simplecovrcov.data.ReporterItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by sergio on 3/30/17.
 */
public class CoverageReportFileParser {
    private File file;

    /**
     * Instantiates a new parser instance
     *
     * @param file An instance of an input file
     */
    public CoverageReportFileParser(File file) {
        this.file = file;
    }

    /**
     * Parses a file with coverage information and provides its high-level representation in data objects
     *
     * @return coverage report data object
     * @throws IOException when couldn't read a file with coverage report
     */
    public CoverageReport parse() throws IOException {
        CoverageReport coverageReport = new CoverageReport();

        String fileString = FileUtils.readFileToString(file, "UTF-8");

        JsonParser parser = new JsonParser();
        // Parse coverage report file as JSON
        JsonObject resultJsonObject = parser.parse(fileString).getAsJsonObject();

        // Iterate through all test suites inside coverage report
        for(Map.Entry coverageMapEntry : resultJsonObject.entrySet()){
            coverageReport.addReporter(buildReporter(coverageMapEntry));
        }
        return coverageReport;
    }

    /**
     * Builds data object for a particular test suite
     *
     * @param coverageMapEntry a map entry where value is a json object with an information about particular test suite
     * @return an information about test suite as a data object
     */
    private Reporter buildReporter(Map.Entry coverageMapEntry) {
        // A key of a given map entry is a suite's name
        String reporterName = coverageMapEntry.getKey().toString();
        Reporter reporter = new Reporter(reporterName);
        // A value contains an information about what files are contained in this test suite and hits information per file
        JsonObject coverageJsonObj = ((JsonObject)coverageMapEntry.getValue()).get("coverage").getAsJsonObject();
        // Iterate through all files in the suite
        for(Map.Entry reportItemMapEntry : coverageJsonObj.entrySet()) {
            reporter.addItem(buildReporterItem(reportItemMapEntry));
        }
        return reporter;
    }

    /**
     * Builds data object for a particular file in a test suite
     *
     * @param reporterItemMapEntry a map entry where a key is a filename and value is an array with hits by line information
     * @return an information about a file as a data object
     */
    private ReporterItem buildReporterItem(Map.Entry reporterItemMapEntry) {
        // A key of a given map entry is file name
        String filename = reporterItemMapEntry.getKey().toString();
        // A value is an array with hits information
        JsonArray marksJsonArr = ((JsonArray)reporterItemMapEntry.getValue());
        Collection<Mark> marks = new ArrayList<>();
        // Iterate through the all hits
        for(JsonElement marksEl : marksJsonArr) {
            marks.add(new Mark(marksEl.toString(), marksEl.isJsonNull()));
        }
        return new ReporterItem(filename, marks);
    }
}
