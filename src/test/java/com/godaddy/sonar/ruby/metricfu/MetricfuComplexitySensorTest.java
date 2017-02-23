package com.godaddy.sonar.ruby.metricfu;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.core.Ruby;
import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;


public class MetricfuComplexitySensorTest {

    // initialize metricfu report file address, mock source files
    // keys and mock project test directory data members
    private final static String YML_SYNTAX_FILE_NAME = "/metricfu_report.yml";

    static final String FILE1_KEY = "modulekey:app/controllers/about_controller.rb";
    static final String FILE2_KEY = "modulekey:app/models/setting/auth.rb";
    static final String FILE3_KEY = "modulekey:app/controllers/api/v2/hosts_controller.rb";

    private File moduleBaseDir = new File("src/test/resources/test-data");

    // declare the metricfu result parser, the examined complexity
    // sensor and a test sensor context to examine. setting object
    // is used to set address parameters for both the parser and the sensor
    private MetricfuYamlParser metricfuYamlParser;
    private MetricfuComplexitySensor metricfuComplexitySensor;
    private SensorContextTester context;
    private Settings settings;

    @org.junit.Rule
    public LogTester logTester = new LogTester();

    @SuppressWarnings("Duplicates")
    @Before
    public void setUp() throws Exception {

        // set mock project setting, default analysis
        // data(saikuro) and metricfu report address
        settings = new MapSettings();
        settings.setProperty(RubyPlugin.METRICFU_COMPLEXITY_METRIC_PROPERTY, "saikuro");
        settings.setProperty(RubyPlugin.METRICFU_REPORT_PATH_PROPERTY, YML_SYNTAX_FILE_NAME);

        // create a test sensor context and initialize it
        context = SensorContextTester.create(moduleBaseDir);
        context.setSettings(settings);

        // input mock files into context file system
        inputFile("app/controllers/about_controller.rb", InputFile.Type.MAIN);
        inputFile("app/models/setting/auth.rb", InputFile.Type.MAIN);
        inputFile("app/controllers/api/v2/hosts_controller.rb", InputFile.Type.MAIN);

        // initialize report parser and complexity sensor with staged context and settings
        metricfuYamlParser = new MetricfuYamlParser(settings, context.fileSystem());
        metricfuComplexitySensor = new MetricfuComplexitySensor(settings, context.fileSystem(), metricfuYamlParser);

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
        DefaultInputFile inputFile = new DefaultInputFile("modulekey", relativePath)
                .setModuleBaseDir(moduleBaseDir.toPath())
                .setLanguage("ruby")
                .setType(type);

        // set the corresponding file metadata and add to context file system
        inputFile.initMetadata(new FileMetadata().readMetadata(inputFile.file(), Charsets.UTF_8));
        context.fileSystem().add(inputFile);

        return inputFile;
    }

    @Test
    public void shouldInitializeConstructor() {
        assertThat(metricfuComplexitySensor, is(notNullValue()));
    }

    @Test
    public void shouldUseDefaultReportType() throws Exception {

        // set a bad report data analysis and run the sensor
        // to verify default(saikuro) report is used
        context.settings().setProperty(RubyPlugin.METRICFU_COMPLEXITY_METRIC_PROPERTY, "non-existent report type");
        metricfuComplexitySensor.execute(context);

        // assert that the default report type was logged in
        assertThat(logTester.logs(), allOf(
                hasItem(containsString("forcing complexity to saikuro.")),
                hasItem(containsString("MetricfuComplexitySensor: using saikuro complexity."))));
    }

    @Test
    public void shouldHaveCorrectDescription() throws Exception {

        // initialize and set a complexity sensor descriptor
        DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
        metricfuComplexitySensor.describe(descriptor);

        // verify correct parameters were set for sensor
        assertThat(descriptor.languages(), everyItem(is(equalTo(Ruby.KEY))));
        assertThat(descriptor.name(), is(equalTo("MetricfuComplexitySensor")));
    }

    @Test
    public void shouldAnalyzeAndSetCorrectSaikuroFunctionComplexity() throws Exception {

        // set saikuro report data analysis and run the sensor to collect the data
        context.settings().setProperty(RubyPlugin.METRICFU_COMPLEXITY_METRIC_PROPERTY, "saikuro");
        metricfuComplexitySensor.execute(context);

        // verify sensor saves correct measured report data
        assertThat(context.measure(FILE1_KEY, CoreMetrics.COMPLEXITY).value(), is(equalTo(3)));
        assertThat(context.measure(FILE2_KEY, CoreMetrics.COMPLEXITY).value(), is(equalTo(2)));
        assertThat(context.measure(FILE3_KEY, CoreMetrics.COMPLEXITY).value(), is(equalTo(58 / 24)));
    }

    @Test
    public void shouldAnalyzeAndSetCorrectSaikuroDistributionComplexities() throws Exception {

        // set saikuro report data analysis and run the sensor to collect the data
        context.settings().setProperty(RubyPlugin.METRICFU_COMPLEXITY_METRIC_PROPERTY, "saikuro");
        metricfuComplexitySensor.execute(context);

        // map the function complexity distribution results to
        // boolean values corresponding to non-zero values
        // and verify not all values are zero(evaluated distribution)
        List<Boolean> functionComplexityDistribution =
                Arrays.stream(context.measure(FILE1_KEY, CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION)
                        .value().split(";"))
                        .map(measure -> Integer.parseInt(measure.split("=")[1]) > 0)
                        .collect(Collectors.toList());
        assertThat("expect function complexity distribution calculated", functionComplexityDistribution, hasItem(true));

        // map the file complexity distribution results to
        // boolean values corresponding to non-zero values
        // and verify not all values are zero(evaluated distribution)
        List<Boolean> fileComplexityDistribution =
                Arrays.stream(context.measure(FILE1_KEY, CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION)
                        .value().split(";"))
                        .map(measure -> Integer.parseInt(measure.split("=")[1]) > 0)
                        .collect(Collectors.toList());
        assertThat("expect file complexity distribution calculated", fileComplexityDistribution, hasItem(true));
    }

    @Test
    public void shouldAnalyzeAndSetCorrectCaneFunctionComplexity() throws Exception {

        // set cane report data analysis and run the sensor to collect the data
        context.settings().setProperty(RubyPlugin.METRICFU_COMPLEXITY_METRIC_PROPERTY, "cane");
        metricfuComplexitySensor.execute(context);

        // verify saved function complexity measure is correct
        assertThat(context.measure(FILE2_KEY, CoreMetrics.COMPLEXITY).value(), is(equalTo(72)));
    }

    @Test
    public void shouldAnalyzeAndSetCorrectCaneDistributionComplexities() throws Exception {

        // set cane report data analysis and run the sensor to collect the data
        context.settings().setProperty(RubyPlugin.METRICFU_COMPLEXITY_METRIC_PROPERTY, "cane");
        metricfuComplexitySensor.execute(context);

        // map the function complexity distribution results to
        // boolean values corresponding to non-zero values
        // and verify not all values are zero(evaluated distribution)
        List<Boolean> functionComplexityDistribution =
                Arrays.stream(context.measure(FILE2_KEY, CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION)
                        .value().split(";"))
                        .map(measure -> Integer.parseInt(measure.split("=")[1]) > 0)
                        .collect(Collectors.toList());
        assertThat("expect function complexity distribution calculated", functionComplexityDistribution, hasItem(true));

        // map the file complexity distribution results to
        // boolean values corresponding to non-zero values
        // and verify not all values are zero(evaluated distribution)
        List<Boolean> fileComplexityDistribution =
                Arrays.stream(context.measure(FILE2_KEY, CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION)
                        .value().split(";"))
                        .map(measure -> Integer.parseInt(measure.split("=")[1]) > 0)
                        .collect(Collectors.toList());
        assertThat("expect file complexity distribution calculated", fileComplexityDistribution, hasItem(true));
    }
}
