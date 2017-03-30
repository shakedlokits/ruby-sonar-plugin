package com.godaddy.sonar.ruby.simplecovrcov;

import java.util.Collections;
import java.util.List;

/**
 * Created by sergio on 3/27/17.
 */
public class MockedCoverageSettings implements CoverageSettings {
    private Boolean processAllSuitesFlag;
    private List<String> configuredSuitesNamesList;

    public MockedCoverageSettings() {
        this(true, Collections.emptyList());
    }

    public MockedCoverageSettings(Boolean processAllSuitesFlag, List<String> configuredSuitesNamesList) {
        this.processAllSuitesFlag = processAllSuitesFlag;
        this.configuredSuitesNamesList = configuredSuitesNamesList;
    }

    @Override
    public Boolean processAllSuites() {
        return this.processAllSuitesFlag;
    }

    @Override
    public List<String> configuredSuitesNames() {
        return this.configuredSuitesNamesList;
    }

    public void setProcessAllSuitesFlag(Boolean processAllSuitesFlag) {
        this.processAllSuitesFlag = processAllSuitesFlag;
    }

    public void setConfiguredSuitesNamesList(List<String> configuredSuitesNamesList) {
        this.configuredSuitesNamesList = configuredSuitesNamesList;
    }
}
