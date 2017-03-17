package com.godaddy.sonar.ruby.simplecovrcov.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sergio on 2/8/17.
 */
public class ReporterItemTest {
    @Test
    public void testGetters() throws Exception {
        String filename = "some/file";
        Collection<Mark> marks = new ArrayList<>();
        ReporterItem reporterItem = new ReporterItem(filename, marks);

        assert reporterItem.getFilename().equals(filename);
        assert reporterItem.getMarks().equals(marks);
    }
}