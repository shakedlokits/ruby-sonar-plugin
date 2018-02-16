package com.godaddy.sonar.ruby;

import com.godaddy.sonar.ruby.core.Ruby;
import com.godaddy.sonar.ruby.core.RubyRecognizer;
import com.godaddy.sonar.ruby.parsers.CommentCountParser;
import com.google.common.collect.Lists;
import jnr.ffi.annotations.In;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.squidbridge.measures.Metric;
import org.sonar.squidbridge.text.Source;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class RubySensor implements Sensor {
    private FileSystem fileSystem;

    public RubySensor(Settings settings, FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public boolean shouldExecuteOnProject(Project project) {
        // This sensor is executed only when there are Ruby files
        return fileSystem.hasFiles(fileSystem.predicates().hasLanguage(Ruby.KEY));
    }

    public void describe(SensorDescriptor descriptor){
        descriptor.onlyOnLanguage(Ruby.KEY).name("Ruby Sensor").onlyOnFileType(InputFile.Type.MAIN);
    }

    public void execute(SensorContext context) {
        computeBaseMetrics(context);
    }

    protected void computeBaseMetrics(SensorContext sensorContext) {
        Reader reader = null;
        List<InputFile> inputFiles = Lists.newArrayList(fileSystem.inputFiles(fileSystem.predicates().hasLanguage(Ruby.KEY)));
        for (InputFile inputFile : inputFiles) {
            try {
                reader = new StringReader(FileUtils.readFileToString(inputFile.file(), fileSystem.encoding().name()));
                Source source = new Source(reader, new RubyRecognizer());

                sensorContext.<Integer>newMeasure().on(inputFile).forMetric(CoreMetrics.NCLOC).withValue(source.getMeasure((Metric.LINES_OF_CODE))).save();

                int numCommentLines = CommentCountParser.countLinesOfComment(inputFile.file());
                sensorContext.<Integer>newMeasure().on(inputFile).forMetric(CoreMetrics.COMMENT_LINES).withValue(numCommentLines).save();
                sensorContext.<Integer>newMeasure().on(inputFile).forMetric(CoreMetrics.FILES).withValue(1).save();
                sensorContext.<Integer>newMeasure().on(inputFile).forMetric(CoreMetrics.CLASSES).withValue(1).save();
            } catch (Exception e) {
                throw new IllegalStateException("Error computing base metrics for project.", e);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
