package com.godaddy.sonar.ruby.simplecovrcov.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sergio on 2/8/17.
 */
public class CoverageReport {
    private Collection<Reporter> reporters = new ArrayList<>();

    public void addReporter(Reporter reporter) { this.reporters.add(reporter); }

    public Collection<Reporter> getReporters() {
        return reporters;
    }
}
