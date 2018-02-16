package com.godaddy.sonar.ruby.simplecovrcov;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.core.Ruby;
import com.google.common.collect.Lists;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.*;
import java.util.List;
import java.util.Map;

public class SimpleCovRcovSensor implements Sensor {
    private static final Logger LOG = Loggers.get(SimpleCovRcovSensor.class);

    private CoverageReportFileAnalyzer coverageReportFileAnalyzer;
    private Settings settings;
    private FileSystem fs;
    private PropertyDefinitions definitions;
    private PathResolver pathResolver;

    private String reportPath = "coverage/.resultset.json";

    /**
     * Use of IoC to get Settings
     */
    public SimpleCovRcovSensor(Settings settings, FileSystem fs,
                               PathResolver pathResolver,
                               CoverageReportFileAnalyzer coverageReportFileAnalyzer) {
        this.settings = settings;
        this.definitions = settings.getDefinitions();
        this.fs = fs;
        this.coverageReportFileAnalyzer = coverageReportFileAnalyzer;
        this.pathResolver = pathResolver;

        String reportpath_prop = settings.getString(RubyPlugin.SIMPLECOVRCOV_REPORT_PATH_PROPERTY);
        if (null != reportpath_prop) {
            this.reportPath = reportpath_prop;
        }
    }

    public boolean shouldExecuteOnProject(Project project) {
        // This sensor is executed only when there are Ruby files
        return fs.hasFiles(fs.predicates().hasLanguage("ruby"));
    }

    public void describe(SensorDescriptor descriptor){
        descriptor.onlyOnLanguage(Ruby.KEY).name("Ruby Rcov Sensor").onlyOnFileType(InputFile.Type.MAIN);
    }

    public void execute(SensorContext context) {
        File report = pathResolver.relativeFile(fs.baseDir(), reportPath);
        LOG.info("Calling analyse for report results: " + report.getPath());
        if (!report.isFile()) {
            LOG.warn("SimpleCovRcov report not found at {}", report);
            return;
        }

        List<InputFile> sourceFiles = Lists.newArrayList(fs.inputFiles(fs.predicates().hasLanguage(Ruby.KEY)));

        try {
            LOG.info("Calling Calculate Metrics");
            calculateMetrics(sourceFiles, report, context);
        } catch (IOException e) {
            LOG.error("unable to calculate Metrics:", e);
        }
    }

    private void printReportFile(String fileName) {
        File jsonFile2 = new File(fileName);

        FileInputStream fis;
        try {
            fis = new FileInputStream(jsonFile2);

            // Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            br.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateMetrics(List<InputFile> sourceFiles, File jsonFile, final SensorContext context) throws IOException {
        LOG.debug(jsonFile.toString());
        Map<String, CoverageMeasuresBuilder> jsonResults = coverageReportFileAnalyzer.analyze(jsonFile);

        LOG.trace("jsonResults: " + jsonResults);
        File sourceFile = null;
        for (InputFile inputFile : sourceFiles) {
            try {
                LOG.debug("SimpleCovRcovSensor processing file: " + inputFile.relativePath());

                sourceFile = inputFile.file();
                String jsonKey = inputFile.absolutePath();
                CoverageMeasuresBuilder fileCoverage = jsonResults.get(jsonKey);

                if (fileCoverage != null) {
                    for (Measure measure : fileCoverage.createMeasures()) {
                        LOG.debug("    Saving measure " + measure.getMetricKey());
                        context.<String>newMeasure().on(inputFile).forMetric(measure.getMetric()).withValue(measure.getValue()).save();
                    }
                }

            } catch (Exception e) {
                if (inputFile != null) {
                    LOG.error("Unable to save metrics for file: " + sourceFile.getName(), e);
                } else {
                    LOG.error("Unable to save metrics.", e);
                }
            }
        }
    }

}
