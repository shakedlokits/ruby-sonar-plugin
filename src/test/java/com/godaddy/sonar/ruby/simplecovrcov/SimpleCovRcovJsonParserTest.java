package com.godaddy.sonar.ruby.simplecovrcov;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.measures.CoverageMeasuresBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SimpleCovRcovJsonParserTest extends TestCase {
    private final static String VALID_JSON_FILE_NAME = "src/test/resources/test-data/valid_simple_cov_results.json";
    private final static String INVALID_JSON_FILE_NAME = "src/test/resources/test-data/invalid_simple_cov_results.json";

    private SimpleCovRcovJsonParserImpl parser = null;

    @Before
    public void setUp() throws Exception {
        parser = new SimpleCovRcovJsonParserImpl();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testParserWithValidJson() throws IOException {
        File reportFile = new File(VALID_JSON_FILE_NAME);
        Map<String, CoverageMeasuresBuilder> coveredFiles = parser.parse(reportFile);

        String coveredFile1 = "/project/source/subdir/file.rb";
        String coveredFile2 = "/project/source/subdir/file1.rb";
        String coveredFile3 = "/project/source/subdir/file1.rb";

        assertEquals(coveredFiles.size(), 12);
        assertEquals(coveredFiles.containsKey(coveredFile1), true);
        assertEquals(coveredFiles.containsKey(coveredFile2), true);
        assertEquals(coveredFiles.containsKey(coveredFile3), true);

        CoverageMeasuresBuilder builder = coveredFiles.get(coveredFile1);
        assertEquals(builder.getCoveredLines(), 13);
    }

    @Test
    public void testParserWithInValidJson() throws IOException {
        File reportFile = new File(INVALID_JSON_FILE_NAME);
        Map<String, CoverageMeasuresBuilder> coveredFiles = parser.parse(reportFile);

        assertEquals(coveredFiles.size(), 0);
    }

}
