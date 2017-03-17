package com.godaddy.sonar.ruby.simplecovrcov.data;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by sergio on 2/9/17.
 */
public class CoverageReportTest {
    @Test
    public void testGetters() {
        CoverageReport coverageReport = new CoverageReport();

        assert coverageReport.getReporters() != null;
    }

    @Test
    public void testAddReporter() {
        CoverageReport coverageReport = new CoverageReport();

        assert coverageReport.getReporters().size() == 0;

        coverageReport.addReporter(new Reporter("RSpec"));

        assert coverageReport.getReporters().size() == 1;
    }

    @Test
    public void testHasSeveralReporters() {
        CoverageReport coverageReport = new CoverageReport();
        assertFalse(coverageReport.hasSeveralReporters());

        coverageReport.addReporter(new Reporter("RSpec"));
        assertFalse(coverageReport.hasSeveralReporters());

        coverageReport.addReporter(new Reporter("Minitest"));
        assertTrue(coverageReport.hasSeveralReporters());
    }

    @Test
    public void testGetFirstReporter() {
        CoverageReport coverageReport = new CoverageReport();

        Reporter reporter = coverageReport.getFirstReporter();
        assert reporter.getName().equals("null");

        coverageReport.addReporter(new Reporter("RSpec"));
        Reporter reporter2 = coverageReport.getFirstReporter();
        assert reporter2.getName().equals("RSpec");

        coverageReport.addReporter(new Reporter("Minitest"));
        Reporter reporter3 = coverageReport.getFirstReporter();
        assert reporter3.getName().equals("RSpec");
    }
}