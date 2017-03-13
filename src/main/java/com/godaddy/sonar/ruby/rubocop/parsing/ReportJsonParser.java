package com.godaddy.sonar.ruby.rubocop.parsing;

import com.godaddy.sonar.ruby.rubocop.data.Report;

import java.io.File;
import java.io.IOException;

/**
 * Created by sergio on 3/13/17.
 */
public interface ReportJsonParser {
    Report parse(File file) throws IOException;
}
