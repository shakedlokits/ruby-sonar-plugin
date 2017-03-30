package com.godaddy.sonar.ruby.simplecovrcov;

import com.godaddy.sonar.ruby.simplecovrcov.data.CoverageReport;
import com.godaddy.sonar.ruby.simplecovrcov.data.Reporter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/30/17.
 */
public class CoverageReportFileParserTest {
    private final static String COVERAGE_REPORT_FILE_NAME = "src/test/resources/test-data/simple_cov_results_multiple_suites.json";

    private CoverageReport coverageReport;

    @Before
    public void setUp() throws IOException {
        File reportFile = new File(COVERAGE_REPORT_FILE_NAME);
        CoverageReportFileParser parser = new CoverageReportFileParser(reportFile);
        this.coverageReport = parser.parse();
    }

    @Test
    public void testParsesInformationAboutAllSuites() {
        assertEquals(2, coverageReport.getReporters().size());
    }

    @Test
    public void testParsesInformationAboutSuiteProperly() {
        Iterator reportsIterator = coverageReport.getReporters().iterator();

        Reporter coverageReport1 = (Reporter) reportsIterator.next();
        assertEquals("MiniTest", coverageReport1.getName());
        assertEquals(1, coverageReport1.getItems().size());

        Reporter coverageReport2 = (Reporter) reportsIterator.next();
        assertEquals("RSpec", coverageReport2.getName());
        assertEquals(1, coverageReport2.getItems().size());
    }
}