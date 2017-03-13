package com.godaddy.sonar.ruby.rubocop.parsing.impl;

import com.godaddy.sonar.ruby.rubocop.data.Location;
import com.godaddy.sonar.ruby.rubocop.data.Metadata;
import com.godaddy.sonar.ruby.rubocop.data.Offense;
import com.godaddy.sonar.ruby.rubocop.data.Report;
import com.godaddy.sonar.ruby.rubocop.parsing.ReportJsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class DefaultReportJsonParserTest {
    private final static String JSON_FILE_NAME = "src/test/resources/test-data/rubocop-report-valid.json";

    private ReportJsonParser parser = null;

    @Before
    public void setUp() throws Exception {
        parser = new DefaultReportJsonParser();
    }

    @Test
    public void testParsingWithValidJson() throws IOException {
        File reportFile = new File(JSON_FILE_NAME);
        Report reportData = parser.parse(reportFile);

        assertNotNull(reportData.getMetadata());
        assertNotNull(reportData.getFiles());

        Metadata metadataData = reportData.getMetadata();
        assertEquals("0.36.0", metadataData.getRubocopVersion());
        assertEquals("ruby", metadataData.getRubyEngine());
        assertEquals("2.3.1", metadataData.getRubyVersion());
        assertEquals("112", metadataData.getRubyPatchlevel());
        assertEquals("x86_64-darwin15", metadataData.getRubyPlatform());

        List<com.godaddy.sonar.ruby.rubocop.data.File> files = reportData.getFiles();
        assertEquals(2, files.size());

        com.godaddy.sonar.ruby.rubocop.data.File capfileFileData = reportData.getFileInfoByPath("Capfile");
        assertEquals("Capfile", capfileFileData.getPath());
        assertEquals(0, capfileFileData.getOffenses().size());

        com.godaddy.sonar.ruby.rubocop.data.File configRuFileData = reportData.getFileInfoByPath("config.ru");
        assertEquals("config.ru", configRuFileData.getPath());
        assertEquals(1, configRuFileData.getOffenses().size());

        Offense offenseData = configRuFileData.getOffenses().get(0);
        assertEquals("convention", offenseData.getSeverity());
        assert offenseData.getMessage().length() > 0;
        assertEquals("Style/NegatedIf", offenseData.getCopName());
        assertEquals(false, offenseData.getCorrected());

        Location offenseLocation = offenseData.getLocation();
        assert offenseLocation.getColumn() == 5;
        assert offenseLocation.getLine() == 12;
        assert offenseLocation.getLength() == 78;
    }
}