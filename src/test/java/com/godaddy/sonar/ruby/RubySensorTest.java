package com.godaddy.sonar.ruby;

import com.godaddy.sonar.ruby.core.LanguageRuby;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.*;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.measure.internal.DefaultMeasure;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RubySensorTest {
    public static String INPUT_SOURCE_DIR = "src/test/resources/test-data";
    public static String INPUT_SOURCE_FILE = "src/test/resources/test-data/hello_world.rb";

    private IMocksControl mocksControl;
    private SensorContext sensorContext;
    private Project project;
    private List<File> sourceDirs;
    private List<File> files;

    private Settings settings;
    private FileSystem fs;
    private FilePredicates filePredicates;
    private FilePredicate filePredicate;

    @Before
    public void setUp() throws Exception {
        mocksControl = EasyMock.createControl();
        fs = mocksControl.createMock(FileSystem.class);
        filePredicates = mocksControl.createMock(FilePredicates.class);
        filePredicate = mocksControl.createMock(FilePredicate.class);

        settings = new MapSettings();

        sensorContext = mocksControl.createMock(SensorContext.class);

        sourceDirs = new ArrayList<File>();
        sourceDirs.add(new File(INPUT_SOURCE_DIR));
        files = new ArrayList<File>();
        files.add(new File(INPUT_SOURCE_FILE));

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRubySensor() {
        RubySensor sensor = new RubySensor(settings, fs);
        assertNotNull(sensor);
    }

    @Test
    public void testShouldExecuteOnProject() {
        RubySensor sensor = new RubySensor(settings, fs);

        expect(fs.predicates()).andReturn(filePredicates).times(1);
        expect(fs.hasFiles(isA(FilePredicate.class))).andReturn(true).times(1);
        expect(filePredicates.hasLanguage(eq("ruby"))).andReturn(filePredicate).times(1);
        mocksControl.replay();

        sensor.shouldExecuteOnProject(project);

        mocksControl.verify();
    }

//    @Test
//    public void testExecute() {
//        RubySensor sensor = new RubySensor(settings, fs);
//
//        DefaultMeasure<Integer> measure = new DefaultMeasure<Integer>();
//        List<InputFile> inputFiles = new ArrayList<InputFile>();
//        File aFile = new File(INPUT_SOURCE_FILE);
//        DefaultInputFile difFile = new TestInputFileBuilder("test project", FileSystems.getDefault().getPath(INPUT_SOURCE_DIR).toFile(), aFile).build();//new TestInputFileBuilder("test project", FileSystems.getDefault().getPath(".").toFile(), new File(INPUT_SOURCE_FILE)).build();
//
//        inputFiles.add(difFile);
//
//        // EasyMock does not work with reference parameters. A custom test class may have to be created if we want to keep this test.
////        expect(sensorContext.newMeasure().on(isA(InputFile.class)).forMetric(isA(Metric.class)).withValue(isA(Integer.class)).save()).andAnswer(
////                new IAnswer<DefaultMeasure>() {
////                    @Override
////                    public DefaultMeasure<Integer> answer() throws Throwable {
////                        return measure;
////                    }
////                }).times(4);
//        expect(fs.predicates()).andReturn(filePredicates).times(1);
//        expect(filePredicates.hasLanguage(eq("ruby"))).andReturn(filePredicate).times(1);
//        expect(fs.inputFiles(isA(FilePredicate.class))).andReturn(inputFiles).times(1);
//        expect(fs.encoding()).andReturn(StandardCharsets.UTF_8).times(1);
//
//        mocksControl.replay();
//
//        sensor.execute(sensorContext);
//        mocksControl.verify();
//    }

    @Test
    public void testToString() {
        RubySensor sensor = new RubySensor(settings, fs);
        String result = sensor.toString();
        assertEquals("RubySensor", result);
    }
}
