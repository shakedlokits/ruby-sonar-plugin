package com.godaddy.sonar.ruby.simplecovrcov;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.PathResolver;

import java.io.File;
import java.io.IOException;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SimpleCovRcovSensorTest {
    private static String RESULT_JSON_FILE_MUTLI_SRC_DIR = "src/test/resources/test-data/results.json";
    private static String RESULT_JSON_FILE_ONE_SRC_DIR = "src/test/resources/test-data/results-one-src-dir.json";

    private IMocksControl mocksControl;

    private PathResolver pathResolver;
    private SimpleCovRcovJsonParser simpleCovRcovJsonParser;
    private SimpleCovRcovSensor simpleCovRcovSensor;
    private SensorContext sensorContext;

    private Settings settings;
    private FileSystem fs;

    @Before
    public void setUp() throws Exception {
        mocksControl = EasyMock.createControl();
        pathResolver = mocksControl.createMock(PathResolver.class);
        fs = mocksControl.createMock(FileSystem.class);
        simpleCovRcovJsonParser = mocksControl.createMock(SimpleCovRcovJsonParser.class);
        settings = new MapSettings();

        simpleCovRcovSensor = new SimpleCovRcovSensor(settings, fs, pathResolver, simpleCovRcovJsonParser);
    }

    @Test
    public void testConstructor() {
        assertNotNull(simpleCovRcovSensor);
    }


    @Test
    public void testAnalyse() throws IOException {

        File jsonFile = new File("coverage/.resultset.json");
        sensorContext = mocksControl.createMock(SensorContext.class);

        expect(fs.baseDir()).andReturn(new File("bar"));
        expect(pathResolver.relativeFile(isA(File.class), isA(String.class))).andReturn(new File("foo"));
//		expect(simpleCovRcovJsonParser.parse(jsonFile)).andThrow(new IOException());

        mocksControl.replay();

        simpleCovRcovSensor.analyse(new Project("key_name"), sensorContext);
        mocksControl.verify();

        assertTrue(true);
    }

}
