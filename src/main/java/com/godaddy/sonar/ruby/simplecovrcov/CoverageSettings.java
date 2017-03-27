package com.godaddy.sonar.ruby.simplecovrcov;

import java.util.List;

/**
 * Created by sergio on 3/27/17.
 */
public interface CoverageSettings {
    /**
     * Specifies whether all suites should be processed or not.
     *
     * Returns true only if appropriate property value:
     * - is null;
     * - is empty string;
     * - contains 'all' string.
     *
     * @return a flag, which specifies all suites should be processed or not
     */
    Boolean processAllSuites();

    /**
     * Specifies a list of manually desired list of test suites, which should be processed.
     *
     * The property value should be comma-delimited string.
     *
     * If all suites are selected then this method will return an empty list.
     *
     * @return a list of suites names or empty list
     */
    List<String> configuredSuitesNames();
}
