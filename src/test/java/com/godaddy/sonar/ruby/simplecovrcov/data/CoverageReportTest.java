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
}