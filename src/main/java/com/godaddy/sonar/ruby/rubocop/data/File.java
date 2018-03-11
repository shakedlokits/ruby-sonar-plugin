package com.godaddy.sonar.ruby.rubocop.data;

import java.util.List;

/**
 * Created by sergio on 3/13/17.
 */
public class File {
    private String path;
    private List<Offense> offenses;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Offense> getOffenses() {
        return offenses;
    }

    public void setOffenses(List<Offense> offenses) {
        this.offenses = offenses;
    }
}
