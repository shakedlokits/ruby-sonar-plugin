package com.godaddy.sonar.ruby.rubocop;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.core.Ruby;
import com.godaddy.sonar.ruby.rubocop.data.File;
import com.godaddy.sonar.ruby.rubocop.data.Offense;
import com.godaddy.sonar.ruby.rubocop.data.Report;
import com.godaddy.sonar.ruby.rubocop.parsing.ReportJsonParser;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.sonar.api.batch.fs.*;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.config.Settings;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.batch.fs.FileSystem;

import java.io.IOException;
import java.util.List;

/**
 * Created by sergio on 3/13/17.
 */
public class RubocopSensor implements Sensor {
    private static final Logger LOG = Loggers.get(RubocopSensor.class);

    private FileSystem fileSystem;
    private ReportJsonParser reportJsonParser;
    private Report reportData;
    private ReportFile reportFile;

    public RubocopSensor(FileSystem fileSystem,
                         ReportJsonParser reportJsonParser,
                         Settings settings){
        this.fileSystem = fileSystem;
        this.reportJsonParser = reportJsonParser;

        String reportPath = settings.getString(RubyPlugin.RUBOCOP_REPORT_PATH_PROPERTY);
        this.reportFile = new ReportFile(fileSystem.baseDir().toString(), reportPath);
    }

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor.onlyOnLanguage(Ruby.KEY).name("RubocopSensor");
    }

    @Override
    public void execute(SensorContext context) {
        if (!this.reportFile.isFileExists()) {
            LOG.info("There is no Rubocop report file at path " + this.reportFile.getPath() + ". Skip Rubocop sensor.");
            return;
        }
        this.parseJsonReport();
        for (InputFile file : inputRubyFiles()) {
            LOG.debug("analyzing issues in the file: " + file.absolutePath());
            try {
                analyzeFile(file, context);
            } catch (IOException e) {
                LOG.error("Can not analyze the file " + file.absolutePath() + " for issues");
            }
        }
    }

    private List<InputFile> inputRubyFiles() {
        FilePredicate filePredicate = fileSystem.predicates().hasLanguage(Ruby.KEY);
        return Lists.newArrayList(fileSystem.inputFiles(filePredicate));
    }

    private void parseJsonReport() {
        try {
            this.reportData = reportJsonParser.parse(this.reportFile.getIoFile());
        } catch (IOException e) {
            LOG.error("Unable to load Rubocop report file from " + this.reportFile.getPath() + "!");
            e.printStackTrace(System.out);
        }
    }

    private void analyzeFile(InputFile file, SensorContext context) throws IOException {
        File fileData = this.reportData.getFileInfoByPath(file.relativePath());
        if (fileData == null) {
            LOG.error("Unable to find report for file: " + file.relativePath());
            return;
        }
        for(Offense offense : fileData.getOffenses()) {
            createIssueForOffence(context, file, offense);
        }
    }

    private void createIssueForOffence(SensorContext context, InputFile file, Offense offense) {
        NewIssue issue = context.newIssue();
        TextRange textRange = file.selectLine(offense.getLocation().getLine());
        NewIssueLocation issueLocation = issue.newLocation().on(file).at(textRange).message(offense.getMessage());
        RuleKey ruleKey = RuleKey.of(RubyPlugin.KEY_REPOSITORY_RUBOCOP, offense.getCopName());
        issue.forRule(ruleKey).at(issueLocation).save();
    }
}
