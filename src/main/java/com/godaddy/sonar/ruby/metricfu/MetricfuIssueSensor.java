package com.godaddy.sonar.ruby.metricfu;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.core.Ruby;
import com.godaddy.sonar.ruby.metricfu.RoodiProblem.RoodiCheck;
import com.google.common.collect.Lists;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.List;

public class MetricfuIssueSensor implements Sensor {
    private static final Logger LOG = Loggers.get(MetricfuIssueSensor.class);

    private MetricfuYamlParser metricfuYamlParser;
    private FileSystem fileSystem;


    /**
     * Instantiates a new Metricfu issue sensor which
     * collects and saves the project issues data such as reek,
     * roodi and cane issues from the metricfu data collected
     *
     * @param fileSystem         the project analyzed file system object
     * @param metricfuYamlParser the metricfu yaml parser
     */
    public MetricfuIssueSensor(FileSystem fileSystem, MetricfuYamlParser metricfuYamlParser) {
        this.fileSystem = fileSystem;
        this.metricfuYamlParser = metricfuYamlParser;
    }

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor.onlyOnLanguage(Ruby.KEY).name("MetricfuIssueSensor");
    }

    @Override
    public void execute(SensorContext context) {
        for (InputFile file : Lists.newArrayList(fileSystem.inputFiles(fileSystem.predicates().hasLanguage(Ruby.KEY)))) {
            LOG.debug("analyzing issues in the file: " + file.absolutePath());
            try {
                analyzeFile(file, context);
            } catch (IOException e) {
                LOG.error("Can not analyze the file " + file.absolutePath() + " for issues");
            }
        }
    }

    /**
     * Analysis helper function used to analyze specific given project
     * files and save their metrics into the SonarQube system
     *
     * @param file    the file to scan and report for
     * @param context the project issues sensor context
     */
    private void analyzeFile(InputFile file, SensorContext context) throws IOException {

        // fetch reek smells and set file reek issues
        List<ReekSmell> smells = metricfuYamlParser.parseReek(file.relativePath());
        for (ReekSmell smell : smells) {

            // TODO: might want to recover severity stats if it seems needed, check the smell class
            NewIssue issue = context.newIssue();
            issue.forRule(RuleKey.of(RubyPlugin.KEY_REPOSITORY_REEK, smell.getType()))
                    .at(issue.newLocation()
                            .on(file)
                            .at(file.selectLine(smell.getLines().get(0)))
                            .message(smell.getMessage()))
                    .save();
        }

        // fetch roodi problems and set file roodi issues
        List<RoodiProblem> problems = metricfuYamlParser.parseRoodi(file.relativePath());
        for (RoodiProblem problem : problems) {

            // get the roodi problem check key
            RoodiCheck check = RoodiProblem.messageToKey(problem.getProblem());

            // save the roodi problem data into the sensor context
            NewIssue issue = context.newIssue();
            issue.forRule(RuleKey.of(RubyPlugin.KEY_REPOSITORY_ROODI, check.toString()))
                    .at(issue.newLocation()
                            .on(file)
                            .at(file.selectLine(problem.getLine()))
                            .message(problem.getProblem()))
                    .save();
        }

        // fetch cane violations and set file cane issues
        List<CaneViolation> violations = metricfuYamlParser.parseCane(file.relativePath());
        for (CaneViolation violation : violations) {

            // initialize the cane issue with it's appropriate key
            NewIssue issue = context
                    .newIssue()
                    .forRule(RuleKey.of(RubyPlugin.KEY_REPOSITORY_CANE, violation.getKey()));
            NewIssueLocation location = null;

            // dependant on the cane violation subtype, set the issue location and message
            if (violation instanceof CaneCommentViolation) {
                CaneCommentViolation caneCommentViolation = (CaneCommentViolation) violation;
                location = issue.newLocation()
                        .on(file)
                        .at(file.selectLine(caneCommentViolation.getLine()))
                        .message("Class '" + caneCommentViolation.getClassName()
                                + "' requires explanatory comments on preceding line.");

            } else if (violation instanceof CaneComplexityViolation) {
                CaneComplexityViolation caneComplexityViolation = (CaneComplexityViolation) violation;
                location = issue.newLocation()
                        .on(file)
                        .message("Method '" + caneComplexityViolation.getMethod() + "' has ABC complexity of "
                                + caneComplexityViolation.getComplexity() + ".");

            } else if (violation instanceof CaneLineStyleViolation) {
                CaneLineStyleViolation caneLineStyleViolation = (CaneLineStyleViolation) violation;
                location = issue.newLocation()
                        .on(file)
                        .at(file.selectLine(caneLineStyleViolation.getLine()))
                        .message(caneLineStyleViolation.getDescription() + ".");
            } else {
                LOG.error("Unauthorized cane violation type");
            }

            // save the final assigned issue type
            issue.at(location).save();
        }
    }
}
