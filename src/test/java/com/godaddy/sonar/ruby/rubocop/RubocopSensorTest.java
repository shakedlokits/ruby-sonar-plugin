package com.godaddy.sonar.ruby.rubocop;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.rubocop.parsing.ReportJsonParser;
import com.godaddy.sonar.ruby.rubocop.parsing.impl.DefaultReportJsonParser;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Settings;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class RubocopSensorTest {
    private File moduleBaseDir = new File("src/test/resources/test-data/rubocop");
    private final static String EXISTING_RUBOCOP_REPORT_FILE_NAME = "/rubocop-report-valid.json";
    private final static String UNEXISTING_RUBOCOP_REPORT_FILE_NAME = "/rubocop-report-unexisting.json";

    private ReportJsonParser reportJsonParser;
    private RubocopSensor rubocopSensor;
    private SensorContextTester context;
    private Settings settings;

    @SuppressWarnings("Duplicates")
    @Before
    public void setUp() throws Exception {
        settings = new MapSettings();

        context = SensorContextTester.create(moduleBaseDir);
        context.setSettings(settings);

        inputFile("Capfile", InputFile.Type.MAIN);
        inputFile("config.ru", InputFile.Type.MAIN);

        reportJsonParser = new DefaultReportJsonParser();
    }

    @Test
    public void shouldAnalyzeAndSetCorrectRubocopProblemsWhenReportFileExists() throws Exception {
        settings.setProperty(RubyPlugin.RUBOCOP_REPORT_PATH_PROPERTY, EXISTING_RUBOCOP_REPORT_FILE_NAME);
        rubocopSensor = new RubocopSensor(context.fileSystem(), reportJsonParser, settings);
        rubocopSensor.execute(context);

        List<Issue> issues = getRubocopRelatedIssues();

        assertEquals(1, issues.size());
    }

    @Test
    public void shouldAnalyzeAndSetCorrectRubocopProblemsWhenReportFileDoesNotExists() throws Exception {
        settings.setProperty(RubyPlugin.RUBOCOP_REPORT_PATH_PROPERTY, UNEXISTING_RUBOCOP_REPORT_FILE_NAME);
        rubocopSensor = new RubocopSensor(context.fileSystem(), reportJsonParser, settings);
        rubocopSensor.execute(context);

        List<Issue> issues = getRubocopRelatedIssues();

        assertEquals(0, issues.size());
    }

    /**
     * Setup helper method, initializes default input
     * files with metadata and path only, such as file
     * type and language which enables locating using the
     * file system predicates.
     *
     * @param relativePath the relative path to the file from module base directory
     * @param type         the input file type
     * @return the default input file generated
     */
    private InputFile inputFile(String relativePath, InputFile.Type type) {

        // generate the default input file by the relative path and type given
        try {
            DefaultInputFile inputFile = new TestInputFileBuilder("modulekey", relativePath)
                    .setModuleBaseDir(moduleBaseDir.toPath())
                    .setLanguage("ruby")
                    .setType(type)
                    .build();

            inputFile.setMetadata(new FileMetadata().readMetadata(new FileReader(inputFile.absolutePath())));
            context.fileSystem().add(inputFile);

            return inputFile;
        } catch (Exception e) {
            fail("File for test does not exist.");
        }
        return null;
    }

    private List<Issue> getRubocopRelatedIssues() {
        return context.allIssues().stream()
                .filter(issue -> issue.ruleKey().toString().contains("rubocop"))
                .collect(Collectors.toList());
    }
}