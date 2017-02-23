package com.godaddy.sonar.ruby.metricfu;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.core.LanguageRuby;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;
import org.sonar.api.internal.apachecommons.lang.reflect.FieldUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.log.LogTester;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MetricfuYamlParserTest {
    private final static String YML_SYNTAX_FILE_NAME = "/metricfu_report.yml";

    private Settings settings;
    private FileSystem fs;
    private Project project;

    @org.junit.Rule
    public LogTester logTester = new LogTester();

    @Before
    public void setUp() throws Exception {
        fs = new DefaultFileSystem(new File("./src/test/resources/test-data"));
        project = new Project("test project");
        settings = new MapSettings();
    }

    @Test
    public void shouldFailWithInvalidPath() throws Exception {
        String filename = "bad_path";

        // make sure that the constructor logs the error
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, "bad_path");
        assertThat(logTester.logs().toString(), containsString("File '" + filename + "' not found."));
    }

    @Test
    public void shouldBeInitializedWithValidPath() throws Exception {

        // verify results are being loaded correctly from valid file
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, YML_SYNTAX_FILE_NAME);

        // use reflection to retrieve the private yaml results field
        Object metricfuResult = FieldUtils.readField(parser, "metricfuResult", true);
        assertThat(metricfuResult, is(notNullValue()));
    }

    @Test
    public void shouldUseSettingReportFileProperty() throws Exception {

        // initialize and verify the correct path was loaded from settings
        settings.setProperty(RubyPlugin.METRICFU_REPORT_PATH_PROPERTY, YML_SYNTAX_FILE_NAME);
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, "bad_path");

        // we should have found the setting syntax file
        assertThat(logTester.logs().toString(), containsString("Looking up report file: file:**/" + YML_SYNTAX_FILE_NAME));

    }

    @Test
    public void shouldParseAndReturnCaneComplexityViolations() throws IOException {

        // initialize parser and request a file's violations
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, YML_SYNTAX_FILE_NAME);
        List<CaneViolation> violations = parser.parseCane("app/models/setting/auth.rb");

        // filter cane violations to complexity violation and assert only 1 complexity violation
        List<CaneViolation> onlyCaneComplexities = violations.stream()
                .filter(violation -> violation instanceof CaneComplexityViolation)
                .collect(Collectors.toList());
        assertThat(onlyCaneComplexities.size(), equalTo(1));

        // validate correct complexity violation
        CaneComplexityViolation complexityViolation = (CaneComplexityViolation) onlyCaneComplexities.get(0);
        assertTrue(complexityViolation.getMethod().equals("Auth.load_defaults"));
        assertTrue(complexityViolation.getComplexity() == 72);
    }

    @Test
    public void shouldParseAndReturnCaneLineStyleViolations() throws Exception {

        // initialize parser and request a file's violations
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, YML_SYNTAX_FILE_NAME);
        List<CaneViolation> violations = parser.parseCane("app/models/setting/auth.rb");

        // filter cane violations to line style violation and assert 26 line style violations
        List<CaneViolation> onlyCaneLineStyles = violations.stream()
                .filter(violation -> violation instanceof CaneLineStyleViolation)
                .collect(Collectors.toList());
        assertThat(onlyCaneLineStyles.size(), equalTo(26));

        // validate violation data was set correctly
        CaneLineStyleViolation styleViolation = (CaneLineStyleViolation) onlyCaneLineStyles.get(0);
        assertThat(styleViolation.getLine(), is(equalTo(15)));
        assertThat(styleViolation.getFile(), is(equalTo("app/models/setting/auth.rb")));
        assertThat(styleViolation.getDescription(), startsWith("Line is >80 characters"));
    }

    @Test
    public void shouldParseAndReturnCaneCommentViolations() throws Exception {

        // initialize parser and request a file's violations
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, YML_SYNTAX_FILE_NAME);
        List<CaneViolation> violations = parser.parseCane("app/models/setting/auth.rb");

        // filter cane violations to comment violations and assert 1 comment violations
        List<CaneViolation> onlyCaneComments = violations.stream()
                .filter(violation -> violation instanceof CaneCommentViolation)
                .collect(Collectors.toList());
        assertThat(onlyCaneComments.size(), equalTo(1));

        // validate violation data was set correctly
        CaneCommentViolation commentViolation = (CaneCommentViolation) onlyCaneComments.get(0);
        assertThat(commentViolation.getLine(), is(equalTo(2)));
        assertThat(commentViolation.getFile(), is(equalTo("app/models/setting/auth.rb")));
        assertThat(commentViolation.getClassName(), is(equalTo("Setting::Auth")));
    }

    @Test
    public void shouldParseAndReturnSaikuroComplexityIssues() throws IOException {

        // initialize parser and request the saikuro complexity issues
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, YML_SYNTAX_FILE_NAME);
        List<SaikuroClassComplexity> saikuroClassComplexities =
                parser.parseSaikuro("app/controllers/api/v2/hosts_controller.rb");

        // validate 3 classes were found
        assertThat("validate 3 classes were found", saikuroClassComplexities.size(), is(equalTo(3)));

        // validate appropriate class data was set
        assertThat(saikuroClassComplexities.get(2).getName(), is(equalTo("Api::V2::HostsController")));
        assertThat(saikuroClassComplexities.get(2).getFile(), is(equalTo("app/controllers/api/v2/hosts_controller.rb")));
        assertThat(saikuroClassComplexities.get(2).getComplexity(), is(equalTo(69)));
        assertThat(saikuroClassComplexities.get(2).getLines(), is(equalTo(328)));

        // validate all 24 class methods were parsed
        assertThat("validate all 24 class methods were parsed", saikuroClassComplexities.get(2).getMethods().size(),
                is(equalTo(24)));

        // validate the correct method data scheme was propagated
        assertThat(saikuroClassComplexities.get(2).getMethods().get(0).getComplexity(), is(equalTo(8)));
        assertThat(saikuroClassComplexities.get(2).getMethods().get(0).getName(),
                is(equalTo("Api::V2::HostsController#action_permission")));
        assertThat(saikuroClassComplexities.get(2).getMethods().get(0).getLines(), is(equalTo(19)));
    }

    @Test
    public void shouldParseAndReturnRoodiDesignProblems() throws Exception {

        // initialize parser and request the roodi design problems
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, YML_SYNTAX_FILE_NAME);
        List<RoodiProblem> roodiDesignProblems =
                parser.parseRoodi("app/controllers/concerns/deep_link_redirector.rb");

        // verify parser returns all of the problems
        assertThat("expect roodi not null", roodiDesignProblems, notNullValue());
        assertThat("expect roodi finds 3 results", roodiDesignProblems.size(), is(equalTo(3)));

        // verify roodi parser sets the correct data
        assertThat(roodiDesignProblems.get(0).getFile(), is(equalTo("app/controllers/concerns/deep_link_redirector.rb")));
        assertThat(roodiDesignProblems.get(0).getLine(), is(equalTo(32)));
        assertThat(roodiDesignProblems.get(0).getProblem(), is(equalTo("Case statement is missing an else clause.")));
    }

    @Test
    public void shouldParseAndReturnReekSmells() throws Exception {

        // initialize parser and request the reek smells
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, YML_SYNTAX_FILE_NAME);
        List<ReekSmell> reekSmells =
                parser.parseReek("app/controllers/about_controller.rb");

        // verify parser return all file smells
        assertThat("expect reek return not null", reekSmells, is(notNullValue()));
        assertThat("expect reek return 2 results", reekSmells.size(), is(equalTo(2)));

        // verify parser sets all the correct information
        assertThat(reekSmells.get(0).getFile(), is(equalTo("app/controllers/about_controller.rb")));
        assertThat(reekSmells.get(0).getMessage(), is(equalTo("has no descriptive comment")));
        assertThat(reekSmells.get(0).getMethod(), is(equalTo("AboutController")));
        assertThat(reekSmells.get(0).getType(), is(equalTo("IrresponsibleModule")));
        assertThat(reekSmells.get(1).getLines().get(0), is(equalTo(4)));
    }

    @Test
    public void shouldParseAndReturnFlayReasons() throws Exception {

        // initialize parser and request the flay reasons
        MetricfuYamlParser parser = new MetricfuYamlParser(settings, fs, YML_SYNTAX_FILE_NAME);
        List<FlayReason> flayReasons = parser.parseFlay();

        // verify flay returns correct number of reasons
        assertThat("expect flay reasons not null", flayReasons, is(notNullValue()));
        assertThat("expect to find 217 flay reasons", flayReasons.size(), is(equalTo(217)));

        // verify flay parses correct reason
        assertThat("expect flay reason to have correct name",
                flayReasons.get(1).getReason(), is(equalTo("2) IDENTICAL code found in :defn (mass*2 = 440)")));

        // verify flay matches correspond to the reason
        for (FlayReason.Match match : flayReasons.get(1).getMatches()) {

            // verify match set the correct file name and lines parameters
            assertThat("expect the flay reason to get correct match filenames", match.getFile(), anyOf(
                    equalTo("app/controllers/api/v1/statistics_controller.rb"),
                    equalTo("app/controllers/api/v2/statistics_controller.rb")));

            assertThat("expect flay reason matches to have correct start line", match.getStartLine(), is(equalTo(6)));
            assertThat("expect flay reason matches to have correct number of lines", match.getLines(), is(equalTo(1)));
        }
    }
}
