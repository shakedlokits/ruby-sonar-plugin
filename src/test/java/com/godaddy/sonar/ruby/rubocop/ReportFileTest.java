package com.godaddy.sonar.ruby.rubocop;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/15/17.
 */
public class ReportFileTest {
    private File moduleBaseDir = new File("src/test/resources/test-data/rubocop");
    private final static String EXISTING_RUBOCOP_REPORT_FILE_NAME = "rubocop-report-valid.json";
    private final static String UNEXISTING_RUBOCOP_REPORT_FILE_NAME = "rubocop-report-unexisting.json";

    private ReportFile reportFile;
    private SensorContextTester context;

    @Before
    public void setUp() {
        context = SensorContextTester.create(moduleBaseDir);

        initReportFileWithFileName(EXISTING_RUBOCOP_REPORT_FILE_NAME);
    }

    @Test
    public void testGetPath() {
        String expectedPath = context.fileSystem().baseDirPath().toString() + "/" + EXISTING_RUBOCOP_REPORT_FILE_NAME;
        assertEquals(expectedPath, this.reportFile.getPath());
    }

    @Test
    public void testGetIoFile() {
        assertNotNull(this.reportFile.getIoFile());
        assertEquals(File.class, this.reportFile.getIoFile().getClass());
    }

    @Test
    public void testGetIsFileExistsWhenItActuallyExists() {
       assertTrue(this.reportFile.isFileExists());
    }

    @Test
    public void testGetIsFileExistsWhenItDoesNotExist() {
        initReportFileWithFileName(UNEXISTING_RUBOCOP_REPORT_FILE_NAME);
        assertFalse(this.reportFile.isFileExists());
    }

    private void initReportFileWithFileName(String reportFileName) {
        this.reportFile = new ReportFile(context.fileSystem().baseDir().toString(), reportFileName);
    }
}