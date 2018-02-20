package com.godaddy.sonar.ruby.simplecovrcov;

import org.sonar.api.measures.CoverageMeasuresBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface CoverageReportFileAnalyzer {
    /**
     * Analyzes a given coverage report file
     *
     * @param file an instance of a coverage report file
     * @return a map where a key is a file name and a value is a coverage information for this file
     * @throws IOException when coverage report file could not be read
     */
    Map<String, CoverageMeasuresBuilder> analyze(File file) throws IOException;
}
