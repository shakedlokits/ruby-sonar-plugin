package com.godaddy.sonar.ruby.rubocop.data;

import java.util.List;

/**
 * Created by sergio on 3/13/17.
 */
public class Report {
    private Metadata metadata;
    private List<File> files;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}
