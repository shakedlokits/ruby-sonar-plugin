package com.godaddy.sonar.ruby.rubocop.data;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class ReportTest {
    private Report report;
    private File file;

    @Before
    public void setUp() {
        List<File> files = new ArrayList<>();
        file = new File();
        file.setPath("test/file.rb");
        files.add(file);

        report = new Report();
        report.setMetadata(new Metadata());
        report.setFiles(files);
    }

    @Test
    public void testSettersAndGetters() throws Exception {
        assertNotNull(report.getMetadata());
        assert report.getFiles().size() == 1;
    }

    @Test
    public void testGetFileInfoByPathWhenFileExists() {
        assertEquals(file, report.getFileInfoByPath("test/file.rb"));
    }

    @Test
    public void testGetFileInfoByPathWhenFileDoesNotExist() {
        assertEquals(null, report.getFileInfoByPath("test2/file.rb"));
    }
}