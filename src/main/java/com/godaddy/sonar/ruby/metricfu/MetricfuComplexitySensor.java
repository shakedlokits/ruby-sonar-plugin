package com.godaddy.sonar.ruby.metricfu;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.core.Ruby;
import com.google.common.collect.Lists;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.ce.measure.RangeDistributionBuilder;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.List;

public class MetricfuComplexitySensor implements Sensor {
    private static final Logger LOG = Loggers.get(MetricfuComplexitySensor.class);

    private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};
    private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
    private static final String COMPLEXITY_SAIKURO = "saikuro";
    private static final String COMPLEXITY_CANE = "cane";

    private MetricfuYamlParser metricfuYamlParser;
    private Settings settings;
    private FileSystem fileSystem;

    /**
     * Instantiates a new Metricfu complexity sensor which
     * collects and saves the project complexity data using the metricfu data
     *
     * @param settings           the user settings object
     * @param fileSystem         the project analyzed file system object
     * @param metricfuYamlParser the metricfu yaml parser
     */
    public MetricfuComplexitySensor(Settings settings, FileSystem fileSystem, MetricfuYamlParser metricfuYamlParser) {
        this.settings = settings;
        this.fileSystem = fileSystem;
        this.metricfuYamlParser = metricfuYamlParser;
    }

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor.onlyOnLanguage(Ruby.KEY).name("MetricfuComplexitySensor");
    }

    @Override
    public void execute(SensorContext context) {

        // fetch user set complexity type
        String complexityType = settings.getString(RubyPlugin.METRICFU_COMPLEXITY_METRIC_PROPERTY);

        // verify complexity type is either saikuro or cane, otherwise set saikuro
        if (!complexityType.equalsIgnoreCase(COMPLEXITY_CANE) && !complexityType.equalsIgnoreCase(COMPLEXITY_SAIKURO)) {
            LOG.warn("Unknown/unsupported complexity type '" + complexityType + ", forcing complexity to " + COMPLEXITY_SAIKURO + ".");
            complexityType = COMPLEXITY_SAIKURO;
        }
        LOG.info("MetricfuComplexitySensor: using " + complexityType + " complexity.");

        // get ruby files to analyze
        FilePredicate predicate = fileSystem.predicates().hasLanguage(Ruby.KEY);
        List<InputFile> sourceFiles = Lists.newArrayList(fileSystem.inputFiles(predicate));

        // iterate and analyze ruby files
        for (InputFile inputFile : sourceFiles) {
            LOG.debug("Analyzing functions for classes in the file: " + inputFile.file().getName());
            analyzeFile(inputFile, context, complexityType);
        }
    }

    /**
     * Analysis helper function used to analyze specific given project
     * files and save their metrics into the SonarQube system
     *
     * @param inputFile      the file to scan and report for
     * @param sensorContext  the project complexity sensor context
     * @param complexityType the type of data to extract from the metricfu reports(saikuro or cane)
     */
    private void analyzeFile(InputFile inputFile, SensorContext sensorContext, String complexityType) {

        // initialize complexity distribution builders and complexity measures
        RangeDistributionBuilder fileDistribution = new RangeDistributionBuilder(FILES_DISTRIB_BOTTOM_LIMITS);
        RangeDistributionBuilder functionDistribution = new RangeDistributionBuilder(FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
        int fileTotalComplexity = 0;
        int numberOfMethods = 0;

        // analyze and fetch cane or saikuro file complexities correspondingly
        // to the set complexity type
        if (complexityType.equalsIgnoreCase(COMPLEXITY_CANE)) {

            // iterate the cane violations and fetch complexity violations
            for (CaneViolation caneViolation : metricfuYamlParser.parseCane(inputFile.relativePath())) {
                if (caneViolation instanceof CaneComplexityViolation) {

                    // cast the cane complexity violation and add
                    // it's complexity to the sum and distribution
                    CaneComplexityViolation caneComplexityViolation = (CaneComplexityViolation) caneViolation;
                    fileTotalComplexity += caneComplexityViolation.getComplexity();
                    functionDistribution.add(caneComplexityViolation.getComplexity());

                    // increment the number of methods for the total file complexity
                    numberOfMethods++;
                }
            }
        } else {

            // iterate the saikuro class and method complexities
            for (SaikuroClassComplexity saikuroClassComplexity : metricfuYamlParser.parseSaikuro(inputFile.relativePath())) {
                for (SaikuroMethodComplexity saikuroMethodComplexity : saikuroClassComplexity.getMethods()) {

                    // add the saikuro method complexity to the distribution function and sum
                    fileTotalComplexity += saikuroMethodComplexity.getComplexity();
                    functionDistribution.add(saikuroMethodComplexity.getComplexity());

                    // increment the number of methods for the total file complexity
                    numberOfMethods++;
                }
            }
        }

        // add the summed file complexity to file distribution metrics
        LOG.debug("SETTING COMPLEXITY METRICS, fileComplexity = " + fileTotalComplexity);
        fileDistribution.add(fileTotalComplexity);

        // save the complexity measures to sensor data if there are existing methods
        LOG.debug("NUMBER OF METHODS = " + numberOfMethods);
        if (numberOfMethods > 0) {

            // save the file complexity distribution metrics
            sensorContext.<String> newMeasure()
                    .on(inputFile)
                    .forMetric(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION)
                    .withValue(fileDistribution.build())
                    .save();

            // save the mean function complexity
            sensorContext.<Integer> newMeasure()
                    .on(inputFile)
                    .forMetric(CoreMetrics.COMPLEXITY)
                    .withValue(fileTotalComplexity / numberOfMethods)
                    .save();

            // save the function complexity distribution metrics
            sensorContext.<String> newMeasure()
                    .on(inputFile)
                    .forMetric(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION)
                    .withValue(functionDistribution.build())
                    .save();
        }
    }
}
