package com.godaddy.sonar.ruby.metricfu;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.core.Ruby;
import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class MetricfuIssueSensorTest {

    // initialize metricfu report file address, mock source files
    // keys and mock project test directory data members
    private File moduleBaseDir = new File("src/test/resources/test-data");
    private final static String YML_SYNTAX_FILE_NAME = "/metricfu_report.yml";

    // declare the metricfu result parser, the examined complexity
    // sensor and a test sensor context to examine. setting object
    // is used to set address parameters for both the parser and the sensor
    private MetricfuYamlParser metricfuYamlParser;
    private MetricfuIssueSensor metricfuIssueSensor;
    private SensorContextTester context;
    private Settings settings;

    @org.junit.Rule
    public LogTester logTester = new LogTester();

    @SuppressWarnings("Duplicates")
    @Before
    public void setUp() throws Exception {

        // set mock project setting, default analysis
        // data(cane) and metricfu report address
        settings = new MapSettings();
        settings.setProperty(RubyPlugin.METRICFU_COMPLEXITY_METRIC_PROPERTY, "cane");
        settings.setProperty(RubyPlugin.METRICFU_REPORT_PATH_PROPERTY, YML_SYNTAX_FILE_NAME);

        // create a test sensor context and initialize it
        context = SensorContextTester.create(moduleBaseDir);
        context.setSettings(settings);

        // input mock files into context file system
        inputFile("app/controllers/about_controller.rb", InputFile.Type.MAIN);
        inputFile("app/models/setting/auth.rb", InputFile.Type.MAIN);
        inputFile("app/controllers/api/v2/hosts_controller.rb", InputFile.Type.MAIN);

        // initialize report parser and issues sensor with staged context and settings
        metricfuYamlParser = new MetricfuYamlParser(settings, context.fileSystem());
        metricfuIssueSensor = new MetricfuIssueSensor(context.fileSystem(), metricfuYamlParser);

        // set logger level according to testing phase (debug is crowded..)
        logTester.setLevel(LoggerLevel.INFO);
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
        DefaultInputFile inputFile = new TestInputFileBuilder("modulekey",  relativePath)
                .setModuleBaseDir(moduleBaseDir.toPath())
                .setLanguage("ruby")
                .setType(type).build();

        // set the corresponding file metadata and add to context file system
        try {
            inputFile.setMetadata(new FileMetadata().readMetadata(inputFile.inputStream(), Charsets.UTF_8, inputFile.absolutePath()));
            context.fileSystem().add(inputFile);
        }
        catch(Exception e){
            fail();
        }

        return inputFile;
    }

    @Test
    public void shouldInitializeConstructor() {
        assertThat(metricfuIssueSensor, is(notNullValue()));
    }

    @Test
    public void shouldHaveCorrectDescription() throws Exception {

        // initialize and set an issues sensor descriptor
        DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
        metricfuIssueSensor.describe(descriptor);

        // verify correct parameters were set for sensor
        assertThat(descriptor.languages(), everyItem(is(equalTo(Ruby.KEY))));
        assertThat(descriptor.name(), is(equalTo("MetricfuIssueSensor")));
    }

    @Test
    public void shouldAnalyzeAndSetCorrectRoodiProblems() throws Exception {

        // initialize issues sensor with mocked context
        metricfuIssueSensor.execute(context);

        // find roodi problems matching the file1 key
        List<Issue> issues = context.allIssues().stream()
                .filter(issue -> issue.ruleKey().toString().contains("roodi"))
                .filter(issue -> issue.primaryLocation().inputComponent().key().equals(MetricfuComplexitySensorTest.FILE1_KEY))
                .collect(Collectors.toList());

        // verify sensor saves correct issue data
        assertThat(issues.get(0).ruleKey().toString(), is(equalTo("roodi:CaseMissingElseCheck")));
        assertThat(issues.get(0).primaryLocation().textRange().start().line(), is(equalTo(15)));
        assertThat(issues.get(0).primaryLocation().message(), is(equalTo("Case statement is missing an else clause.")));
    }

    @Test
    public void shouldAnalyzeAndSetCorrectReekSmells() throws Exception {

        // initialize issues sensor with mocked context
        metricfuIssueSensor.execute(context);

        // find reek smells matching the file2 key
        List<Issue> issues = context.allIssues().stream()
                .filter(issue -> issue.ruleKey().toString().contains("reek"))
                .filter(issue -> issue.primaryLocation().inputComponent().key().equals(MetricfuComplexitySensorTest.FILE2_KEY))
                .collect(Collectors.toList());

        // verify sensor saves correct issue data
        assertThat("expect to find 10 reek smells", issues.size(), is(equalTo(10)));
        assertThat(issues.get(1).ruleKey().toString(), is(equalTo("reek:DuplicateMethodCall")));
        assertThat(issues.get(1).primaryLocation().message(), is(equalTo("calls N_(\"OAuth consumer key\") 2 times")));
        assertThat(issues.get(1).primaryLocation().textRange().start().line(), is(equalTo(16)));
    }

    @Test
    public void shouldAnalyzeAndSetCorrectCaneViolations() throws Exception {

        // initialize issues sensor with mocked context
        metricfuIssueSensor.execute(context);

        // find cane violations matching the file3 key
        List<Issue> issues = context.allIssues().stream()
                .filter(issue -> issue.ruleKey().toString().contains("cane"))
                .filter(issue -> issue.primaryLocation().inputComponent().key().equals(MetricfuComplexitySensorTest.FILE3_KEY))
                .filter(issue -> !issue.ruleKey().toString().contains("LineStyleLengthViolation"))
                .collect(Collectors.toList());

        // verify sensor saves correct issue data
        assertThat(issues.get(0).ruleKey().toString(), containsString("CommentViolation"));
        assertThat(issues.get(0).primaryLocation().textRange().start().line(), is(equalTo(3)));
        assertThat(issues.get(0).primaryLocation().message(),
                is(equalTo("Class 'HostsController' requires explanatory comments on preceding line.")));
    }
}
