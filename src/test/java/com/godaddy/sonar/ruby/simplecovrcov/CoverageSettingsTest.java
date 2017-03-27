package com.godaddy.sonar.ruby.simplecovrcov;

import com.godaddy.sonar.ruby.RubyPlugin;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/27/17.
 */
public class CoverageSettingsTest {
    private Settings settings;
    private CoverageSettings coverageSettings;

    @Before
    public void setUp() throws Exception {
        this.settings = new Settings();
        this.coverageSettings = new CoverageSettings(settings);
    }

    @Test
    public void testProcessAllSuitesWhenPropertyIsEmpty() {
        settings.setProperty(RubyPlugin.COVERAGE_TEST_SUITES_PROPERTY, "");
        assertTrue(coverageSettings.processAllSuites());
    }

    @Test
    public void testProcessAllSuitesWhenPropertyContainsAllFlag() {
        settings.setProperty(RubyPlugin.COVERAGE_TEST_SUITES_PROPERTY, CoverageSettings.ALL_SUITES_FLAG);
        assertTrue(coverageSettings.processAllSuites());
    }

    @Test
    public void testProcessAllSuitesWhenPropertyContainsSomeOtherValue() {
        settings.setProperty(RubyPlugin.COVERAGE_TEST_SUITES_PROPERTY, "my_suite");
        assertFalse(coverageSettings.processAllSuites());
    }

    @Test
    public void testConfiguredSuitesNamesWhenAllSuitesSelected() {
        settings.setProperty(RubyPlugin.COVERAGE_TEST_SUITES_PROPERTY, CoverageSettings.ALL_SUITES_FLAG);
        assertTrue(coverageSettings.configuredSuitesNames().isEmpty());
    }

    @Test
    public void testConfiguredSuitesNamesWhenPropertyContainsSomeOtherValue() {
        settings.setProperty(RubyPlugin.COVERAGE_TEST_SUITES_PROPERTY, "MiniSpec,RSpec");
        List<String> suites = coverageSettings.configuredSuitesNames();
        assertFalse(suites.isEmpty());
        assertTrue(suites.contains("MiniSpec"));
        assertTrue(suites.contains("RSpec"));
    }
}