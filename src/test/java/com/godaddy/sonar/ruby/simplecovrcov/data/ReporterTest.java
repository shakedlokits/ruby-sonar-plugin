package com.godaddy.sonar.ruby.simplecovrcov.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by sergio on 2/9/17.
 */
public class ReporterTest {
    @Test
    public void testGetters() throws Exception {
        String name = "RSpec";
        Reporter reporter = new Reporter(name);

        assertEquals(reporter.getName(), name);
        assert reporter.getItems() != null;
    }

    @Test
    public void testAddItem() throws Exception {
        Reporter reporter = new Reporter("MiniTest");

        assert reporter.getItems().size() == 0;

        reporter.addItem(new ReporterItem("some.file"));

        assert reporter.getItems().size() == 1;
    }
}