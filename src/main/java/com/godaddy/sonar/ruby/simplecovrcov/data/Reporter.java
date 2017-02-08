package com.godaddy.sonar.ruby.simplecovrcov.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by sergio on 2/8/17.
 */
public class Reporter {
    private String name;
    private Collection<ReporterItem> items = new ArrayList<>();

    public Reporter(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addItem(ReporterItem reporterItem) {
        this.items.add(reporterItem);
    }

    public Collection<ReporterItem> getItems() {
        return items;
    }
}
