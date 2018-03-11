package com.godaddy.sonar.ruby.rubocop;

import java.io.File;

/**
 * Created by sergio on 3/15/17.
 */
public class ReportFile {
    private String path;
    private File ioFile;

    public ReportFile(String baseDir, String reportPath) {
        this.path = baseDir + "/" + reportPath;
        this.ioFile = new File(path);
    }

    public String getPath() {
        return this.path;
    }

    public File getIoFile() {
        return this.ioFile;
    }

    public Boolean isFileExists() {
        return this.ioFile.exists();
    }
}
