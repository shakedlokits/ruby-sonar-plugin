package com.godaddy.sonar.ruby.rubocop;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.rubocop.parsing.ReportJsonParser;
import com.godaddy.sonar.ruby.rubocop.parsing.impl.DefaultReportJsonParser;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Settings;
import com.google.common.base.Charsets;
import org.sonar.api.batch.sensor.issue.Issue;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class RubocopSensorTest {
    private File moduleBaseDir = new File("src/test/resources/test-data/rubocop");
    private final static String RUBOCOP_REPORT_FILE_NAME = "/rubocop-report-valid.json";

    private ReportJsonParser reportJsonParser;
    private RubocopSensor rubocopSensor;
    private SensorContextTester context;
    private Settings settings;

    @SuppressWarnings("Duplicates")
    @Before
    public void setUp() throws Exception {
        settings = new Settings();
        settings.setProperty(RubyPlugin.RUBOCOP_REPORT_PATH_PROPERTY, RUBOCOP_REPORT_FILE_NAME);

        context = SensorContextTester.create(moduleBaseDir);
        context.setSettings(settings);

        inputFile("Capfile", InputFile.Type.MAIN);
        inputFile("config.ru", InputFile.Type.MAIN);

        reportJsonParser = new DefaultReportJsonParser();
        rubocopSensor = new RubocopSensor(context.fileSystem(), reportJsonParser, settings);
    }

    @Test
    public void shouldAnalyzeAndSetCorrectRubocopProblems() throws Exception {
        rubocopSensor.execute(context);

        List<Issue> issues = context.allIssues().stream()
                .filter(issue -> issue.ruleKey().toString().contains("rubocop"))
                .collect(Collectors.toList());

        assertEquals(1, issues.size());
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
        DefaultInputFile inputFile = new DefaultInputFile("modulekey", relativePath)
                .setModuleBaseDir(moduleBaseDir.toPath())
                .setLanguage("ruby")
                .setType(type);

        // set the corresponding file metadata and add to context file system
        inputFile.initMetadata(new FileMetadata().readMetadata(inputFile.file(), Charsets.UTF_8));
        context.fileSystem().add(inputFile);

        return inputFile;
    }
}