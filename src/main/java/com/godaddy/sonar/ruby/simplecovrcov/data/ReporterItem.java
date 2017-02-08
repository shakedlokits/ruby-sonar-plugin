package com.godaddy.sonar.ruby.simplecovrcov.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sergio on 2/8/17.
 */
public class ReporterItem {
    private String filename;
    private Collection<Object> marks;

    public ReporterItem(String filename) {
        new ReporterItem(filename, new ArrayList<>());
    }

    public ReporterItem(String filename, Collection<Object> marks) {
        this.filename = filename;
        this.marks = marks;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Collection<Object> getMarks() {
        return marks;
    }
}
