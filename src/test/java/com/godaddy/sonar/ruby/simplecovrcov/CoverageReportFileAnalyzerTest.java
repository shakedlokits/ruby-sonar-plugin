package com.godaddy.sonar.ruby.simplecovrcov;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.measures.CoverageMeasuresBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CoverageReportFileAnalyzerTest {
    private final static String VALID_JSON_FILE_NAME = "src/test/resources/test-data/simple_cov_results.json";
    private final static String MULTIPLE_SUITES_FILE_NAME = "src/test/resources/test-data/simple_cov_results_multiple_suites.json";

    private CoverageReportFileAnalyzerImpl analyzer = null;
    private MockedCoverageSettings coverageSettings;

    @Before
    public void setUp() throws Exception {
        this.coverageSettings = new MockedCoverageSettings();
        analyzer = new CoverageReportFileAnalyzerImpl(this.coverageSettings);
    }

    @Test
    public void testParserWithValidJson() throws IOException {
        File reportFile = new File(VALID_JSON_FILE_NAME);
        Map<String, CoverageMeasuresBuilder> coveredFiles = analyzer.analyze(reportFile);

        String coveredFile1 = "/project/source/subdir/file.rb";
        String coveredFile2 = "/project/source/subdir/file1.rb";
        String coveredFile3 = "/project/source/subdir/file1.rb";

        assertEquals(12, coveredFiles.size());
        assertEquals(coveredFiles.containsKey(coveredFile1), true);
        assertEquals(coveredFiles.containsKey(coveredFile2), true);
        assertEquals(coveredFiles.containsKey(coveredFile3), true);

        CoverageMeasuresBuilder builder = coveredFiles.get(coveredFile1);
        assertEquals(13, builder.getCoveredLines());
    }

    @Test
    public void testParserWithAllSuites() throws IOException {
        this.coverageSettings.setProcessAllSuitesFlag(true);
        File reportFile = new File(MULTIPLE_SUITES_FILE_NAME);
        Map<String, CoverageMeasuresBuilder> coveredFiles = analyzer.analyze(reportFile);
        String coveredFile = "/project/source/subdir/file.rb";
        assertEquals(1, coveredFiles.size());
        assertEquals(coveredFiles.containsKey(coveredFile), true);
        CoverageMeasuresBuilder builder = coveredFiles.get(coveredFile);
        assertEquals(14, builder.getCoveredLines());
    }

    @Test
    public void testParserWithSpecifiedSuites() throws IOException {
        this.coverageSettings.setProcessAllSuitesFlag(false);
        this.coverageSettings.setConfiguredSuitesNamesList(Collections.singletonList("RSpec"));
        File reportFile = new File(MULTIPLE_SUITES_FILE_NAME);
        Map<String, CoverageMeasuresBuilder> coveredFiles = analyzer.analyze(reportFile);
        String coveredFile = "/project/source/subdir/file.rb";
        assertEquals(1, coveredFiles.size());
        assertEquals(coveredFiles.containsKey(coveredFile), true);
        CoverageMeasuresBuilder builder = coveredFiles.get(coveredFile);
        assertEquals(11, builder.getCoveredLines());
    }
}
