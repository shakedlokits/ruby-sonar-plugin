package com.godaddy.sonar.ruby.simplecovrcov;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoverageMeasuresBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SimpleCovRcovJsonParserTest extends TestCase {
    private final static String VALID_JSON_FILE_NAME = "src/test/resources/test-data/simple_cov_results.json";
    private final static String MULTIPLE_SUITES_FILE_NAME = "src/test/resources/test-data/simple_cov_results_multiple_suites.json";

    private SimpleCovRcovJsonParserImpl parser = null;
    private DefaultCoverageSettings defaultCoverageSettings;
    private Settings settings;

    @Before
    public void setUp() throws Exception {
        this.settings = new Settings();
        this.defaultCoverageSettings = new DefaultCoverageSettings(this.settings);
        parser = new SimpleCovRcovJsonParserImpl(this.defaultCoverageSettings);
    }

    @Test
    public void testParserWithValidJson() throws IOException {
        File reportFile = new File(VALID_JSON_FILE_NAME);
        Map<String, CoverageMeasuresBuilder> coveredFiles = parser.parse(reportFile);

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
    public void testParserWithMultipleSuites() throws IOException {

    }
}
