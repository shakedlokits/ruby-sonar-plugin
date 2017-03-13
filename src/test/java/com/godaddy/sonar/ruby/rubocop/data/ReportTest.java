package com.godaddy.sonar.ruby.rubocop.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class ReportTest {
    @Test
    public void testSettersAndGetters() throws Exception {
        List<File> files = new ArrayList<>();
        files.add(new File());

        Report report = new Report();
        report.setMetadata(new Metadata());
        report.setFiles(files);

        assertNotNull(report.getMetadata());
        assert report.getFiles() == files;
    }
}