package com.godaddy.sonar.ruby.simplecovrcov;

import com.godaddy.sonar.ruby.RubyPlugin;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.config.Settings;

import java.util.*;

/**
 * Provides a wrapper around SonarQube settings.
 *
 * This wrapper provides a high-level API for coverage and SimpleCov related settings entries.
 *
 * @author Sergey Gernyak
 */
@BatchSide
@ExtensionPoint
public class DefaultCoverageSettings implements CoverageSettings {
    public static final String ALL_SUITES_FLAG = "all";

    private Settings settings;

    /**
     * Instantiates a new settings wrapper for coverage handling subsystem.
     *
     * @param settings the user settings object
     */
    public DefaultCoverageSettings(Settings settings) {
        this.settings = settings;
    }

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
    public Boolean processAllSuites() {
        String propertyValue = fetchCoverageTestSuitesProperty();
        return propertyValue == null || propertyValue.isEmpty() || propertyValue.equals(ALL_SUITES_FLAG);
    }

    /**
     * Specifies a list of manually desired list of test suites, which should be processed.
     *
     * The property value should be comma-delimited string.
     *
     * If all suites are selected then this method will return an empty list.
     *
     * @return a list of suites names or empty list
     */
    public List<String> configuredSuitesNames() {
        if (this.processAllSuites()) { return Collections.emptyList(); }
        return Arrays.asList(fetchCoverageTestSuitesProperty().split(","));
    }

    private String fetchCoverageTestSuitesProperty() {
        return settings.getString(RubyPlugin.COVERAGE_TEST_SUITES_PROPERTY);
    }
}
