package com.godaddy.sonar.ruby.simplecovrcov;

import org.sonar.api.BatchExtension;
import org.sonar.api.measures.CoverageMeasuresBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface CoverageReportFileAnalyzer extends BatchExtension {
    Map<String, CoverageMeasuresBuilder> analyze(File file) throws IOException;
}
