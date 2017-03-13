package com.godaddy.sonar.ruby;

import com.godaddy.sonar.ruby.core.Ruby;
import com.godaddy.sonar.ruby.core.RubySourceCodeColorizer;
import com.godaddy.sonar.ruby.core.profiles.SonarWayProfile;
import com.godaddy.sonar.ruby.metricfu.*;
import com.godaddy.sonar.ruby.rubocop.CopsRulesDefinition;
import com.godaddy.sonar.ruby.simplecovrcov.SimpleCovRcovJsonParserImpl;
import com.godaddy.sonar.ruby.simplecovrcov.SimpleCovRcovSensor;
import org.sonar.api.CoreProperties;
import org.sonar.api.Properties;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is the entry point for all extensions
 */
@Properties({})
public final class RubyPlugin extends SonarPlugin {
    public static final String KEY_REPOSITORY_CANE = "cane";
    public static final String NAME_REPOSITORY_CANE = "Cane";

    public static final String KEY_REPOSITORY_REEK = "reek";
    public static final String NAME_REPOSITORY_REEK = "Reek";

    public static final String KEY_REPOSITORY_ROODI = "roodi";
    public static final String NAME_REPOSITORY_ROODI = "Roodi";

    public static final String KEY_REPOSITORY_RUBOCOP = "rubocop";
    public static final String NAME_REPOSITORY_RUBOCOP = "Rubocop";

    public static final String SIMPLECOVRCOV_REPORT_PATH_PROPERTY = "sonar.simplecovrcov.reportPath";
    public static final String METRICFU_REPORT_PATH_PROPERTY = "sonar.metricfu.reportPath";
    public static final String METRICFU_COMPLEXITY_METRIC_PROPERTY = "sonar.metricfu.complexityMetric";

    public static final String RUBOCOP_REPORT_PATH_PROPERTY = "sonar.rubocop.reportPath";

    public List<Object> getExtensions() {
        List<Object> extensions = new ArrayList<Object>();
        extensions.add(Ruby.class);
        // FIXME add back code duplication metrics once it's fixed
        // extensions.add(RubyCPDMapping.class);
        extensions.add(SimpleCovRcovSensor.class);
        extensions.add(SimpleCovRcovJsonParserImpl.class);
        extensions.add(MetricfuYamlParser.class);
        extensions.add(RubySourceCodeColorizer.class);
        extensions.add(RubySensor.class);
        extensions.add(MetricfuComplexitySensor.class);
        // extensions.add(MetricfuDuplicationSensor.class);
        extensions.add(MetricfuIssueSensor.class);
        extensions.add(CaneRulesDefinition.class);
        extensions.add(ReekRulesDefinition.class);
        extensions.add(RoodiRulesDefinition.class);
        extensions.add(CopsRulesDefinition.class);


        // Profiles
        extensions.add(SonarWayProfile.class);

        PropertyDefinition metricfuReportPath = PropertyDefinition.builder(METRICFU_REPORT_PATH_PROPERTY)
                .category(CoreProperties.CATEGORY_CODE_COVERAGE)
                .subCategory("Ruby Coverage")
                .name("MetricFu Report path")
                .description("Path (absolute or relative) to MetricFu yml report file.")
                .defaultValue("tmp/metric_fu/report.yml")
                .onQualifiers(Qualifiers.PROJECT)
                .build();
        extensions.add(metricfuReportPath);

        PropertyDefinition simplecovrcovReportPath = PropertyDefinition.builder(SIMPLECOVRCOV_REPORT_PATH_PROPERTY)
                .category(CoreProperties.CATEGORY_CODE_COVERAGE)
                .subCategory("Ruby Coverage")
                .name("SimpleCovRcov Report path")
                .description("Path (absolute or relative) to SimpleCovRcov json report file.")
                .defaultValue("coverage/.resultset.json")
                .onQualifiers(Qualifiers.PROJECT)
                .build();
        extensions.add(simplecovrcovReportPath);

        PropertyDefinition rubocopReportPathProperty = PropertyDefinition.builder(RUBOCOP_REPORT_PATH_PROPERTY)
                .category(CoreProperties.CATEGORY_CODE_COVERAGE)
                .subCategory("Ruby Coverage")
                .name("Rubocop Report path")
                .description("Path (absolute or relative) to Rubocop json report file.")
                .defaultValue("tmp/rubocop/report.json")
                .onQualifiers(Qualifiers.PROJECT)
                .build();
        extensions.add(rubocopReportPathProperty);

        List<String> options = Arrays.asList("Saikuro", "Cane");

        PropertyDefinition ComplexityMetric = PropertyDefinition.builder(METRICFU_COMPLEXITY_METRIC_PROPERTY)
                .category(CoreProperties.CATEGORY_CODE_COVERAGE)
                .subCategory("Ruby Coverage")
                .name("MetricFu Complexity Metric")
                .description("Type of complexity, Saikuro or Cane")
                .defaultValue("Saikuro")
                .onQualifiers(Qualifiers.PROJECT)
                .type(PropertyType.SINGLE_SELECT_LIST)
                .options(options)
                .build();
        extensions.add(ComplexityMetric);

        return extensions;
    }
}
