package com.godaddy.sonar.ruby.metricfu;

import com.godaddy.sonar.ruby.RubyPlugin;
import com.godaddy.sonar.ruby.metricfu.FlayReason.Match;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@BatchSide
@ExtensionPoint
public class MetricfuYamlParser {
    private static final Logger LOG = Loggers.get(MetricfuYamlParser.class);

    private static final String REPORT_FILE = "tmp/metric_fu/report.yml";
    private static Pattern escapePattern = Pattern.compile("\\e\\[\\d+m", Pattern.CASE_INSENSITIVE);

    private Map<String, Object> metricfuResult = null;

    ArrayList<Map<String, Object>> saikuroFiles = null;
    Map<String, Object> caneViolations = null;
    ArrayList<Map<String, Object>> roodiProblems = null;
    ArrayList<Map<String, Object>> reekFiles = null;
    ArrayList<Map<String, Object>> flayReasons = null;

    /**
     * Instantiates a new Metricfu yaml parser
     * with the default report file path.
     *
     * @param settings   the settings object
     * @param fileSystem the file system object
     */
    public MetricfuYamlParser(Settings settings, FileSystem fileSystem) {
        this(settings, fileSystem, REPORT_FILE);
    }


    /**
     * Instantiates a new Metricfu yaml parser.
     * This is the main constructor, enabling the
     * modification of the report file path.
     *
     * @param settings   the settings
     * @param fileSystem the file system
     * @param filename   the filename
     */
    public MetricfuYamlParser(Settings settings, FileSystem fileSystem, String filename) {

        // if a report file path was set by the user in the settings, use it instead
        String prop = settings.getString(RubyPlugin.METRICFU_REPORT_PATH_PROPERTY);
        if (prop != null) {
            filename = prop;
        }

        // FIXME: a predicate for a report file in a different location don't why it exists..
        FilePredicate p = fileSystem.predicates().matchesPathPattern("katello/" + filename);

        // attempts to open the report file and saves it's
        // yaml representation as an object
        LOG.debug("Looking up report file: file:**/" + filename);
        File report = new File(fileSystem.baseDir().toString() + '/' + filename);
        if (report.exists()) {
            LOG.info("Report file: " + report.getPath());

            // attempts to read and load the yaml into the metricfuResult Map
            try {
                FileInputStream input = new FileInputStream(report);
                Yaml yaml = new Yaml();
                this.metricfuResult = yaml.loadAs(input, Map.class);
            } catch (FileNotFoundException e) {
                LOG.error("File '" + report.getPath() + "' not found.", e);
            }
        }

        // FIXME: if the file wasn't found search in /katello/*/path, no idea why he did that..
        else {
            LOG.error("File '" + filename + "' not found.");
            Iterable<File> reports = fileSystem.files(p);
            LOG.debug("Got some files = " + (reports.iterator().hasNext() ? "true" : " false"));
            File ff = fileSystem.resolvePath("katello/" + filename);
            LOG.debug("Got a file = " + (ff != null ? "true" : " false"));
        }
    }

    /**
     * Parse the saikuro complexity issues and return a list of
     * issues corresponding to the requested file
     *
     * @param filename the filename to collect the issues for
     * @return saikuro complexity issues list corresponding to the requested file
     */
    public List<SaikuroClassComplexity> parseSaikuro(String filename) {

        // initialize the output saikuro issues list
        LOG.debug("parseSaikuro: " + filename);
        List<SaikuroClassComplexity> complexities = new ArrayList<>();

        // validate the metricfu results have been loaded
        if (metricfuResult == null) {
            LOG.warn("No metricfu results for saikuro.");
            return complexities;
        }

        // extract the saikuro issues from the metricfu results if needed
        if (saikuroFiles == null) {
            Map<String, Object> saikuro = (Map<String, Object>) metricfuResult.get(":saikuro");
            if (saikuro != null) {
                saikuroFiles = (ArrayList<Map<String, Object>>) saikuro.get(":files");
            }
        }

        // if the saikuro issues have been parsed and extracted,
        // locate the corresponding file issues
        if (saikuroFiles != null) {

            // iterate the saikuro files issues and select corresponding issues
            for (Map<String, Object> fileInfo : saikuroFiles) {
                String file = (String) fileInfo.get(":filename");
                if (file.equals(filename)) {

                    // extract the class issues from the saikuro file issue row
                    ArrayList<Map<String, Object>> classes =
                            (ArrayList<Map<String, Object>>) fileInfo.get(":classes");

                    // iterate the saikuro file classes complexity issues
                    // and save them with their method complexity issues
                    for (Map<String, Object> classInfo : classes) {

                        // create a new saikuro complexity issue and set it's appropriate data
                        SaikuroClassComplexity saikuroClassComplexity = new SaikuroClassComplexity();
                        saikuroClassComplexity.setFile(file);
                        saikuroClassComplexity.setName((String) classInfo.get(":class_name"));
                        saikuroClassComplexity.setLines((Integer) classInfo.get(":lines"));
                        saikuroClassComplexity.setComplexity((Integer) classInfo.get(":complexity"));

                        // extract the method issues from the saikuro class
                        ArrayList<Map<String, Object>> methods =
                                (ArrayList<Map<String, Object>>) classInfo.get(":methods");

                        // iterate the saikuro method complexity issues and save them
                        for (Map<String, Object> methodInfo : methods) {

                            // create a new saikuro method complexity issue and set it's appropriate data
                            SaikuroMethodComplexity saikuroMethodComplexity = new SaikuroMethodComplexity();
                            saikuroMethodComplexity.setName((String) methodInfo.get(":name"));
                            saikuroMethodComplexity.setComplexity((Integer) methodInfo.get(":complexity"));
                            saikuroMethodComplexity.setLine((Integer) methodInfo.get(":lines"));
                            LOG.debug("    adding method: " + saikuroMethodComplexity.getName()
                                    + ", complexit = " + saikuroMethodComplexity.getComplexity());

                            // add the new method issue to the class complexity issue
                            saikuroClassComplexity.getMethods().add(saikuroMethodComplexity);
                        }

                        // add the class complexity issue to the output saikuro issues list
                        LOG.debug("    adding class: " + saikuroClassComplexity.getName()
                                + ", complexity = " + saikuroClassComplexity.getComplexity());
                        complexities.add(saikuroClassComplexity);
                    }
                    break;
                }
            }
        }

        // return the output saikuro issues list
        return complexities;
    }


    /**
     * Parse the cane violations and return a list of
     * violations corresponding to requested file name
     *
     * @param filename file name to look up the violations for
     * @return the list of cane violations corresponding to the file
     */
    public List<CaneViolation> parseCane(String filename) {

        // initialize the output violations list
        List<CaneViolation> violations = new ArrayList<>();

        // verify the metricfu results have been parsed
        if (metricfuResult == null) {
            LOG.warn("No metricfu results for cane.");
            return violations;
        }

        // load the cane violations from the metricfu results if needed
        if (caneViolations == null) {
            Map<String, Object> caneResult = (Map<String, Object>) metricfuResult.get(":cane");
            if (caneResult != null) {
                caneViolations = (Map<String, Object>) caneResult.get(":violations");
            }
        }

        // if cane violations exist, analyze the different kind of violations
        if (caneViolations != null) {

            // locate and push the cane complexity violations for the requested file
            // extract the cane method complexity issues
            ArrayList<Map<String, Object>> caneViolationsComplexityResult =
                    (ArrayList<Map<String, Object>>) caneViolations.get(":abc_complexity");

            // iterate the complexity items and find issues corresponding to the requested file
            for (Map<String, Object> caneViolationsLineResultRow : caneViolationsComplexityResult) {
                String file = (String) caneViolationsLineResultRow.get(":file");
                if (filename.equals(file)) {

                    // input a new matching violation into the output list
                    CaneComplexityViolation violation = new CaneComplexityViolation();
                    violation.setFile(file);
                    violation.setMethod((String) caneViolationsLineResultRow.get(":method"));
                    violation.setComplexity(Integer.parseInt((String) caneViolationsLineResultRow.get(":complexity")));
                    violations.add(violation);
                }
            }

            // extract the cane line style issues
            ArrayList<Map<String, Object>> caneViolationsLineResult =
                    (ArrayList<Map<String, Object>>) caneViolations.get(":line_style");

            // iterate the line style issues and find issues corresponding to the requested file
            for (Map<String, Object> caneViolationsLineResultRow : caneViolationsLineResult) {
                String parts[] = ((String) caneViolationsLineResultRow.get(":line")).split(":");
                if (parts[0].length() > 0 && parts[0].equals(filename)) {

                    // create a new line style violation and push it to the violations list
                    CaneLineStyleViolation violation = new CaneLineStyleViolation();
                    violation.setFile(parts[0]);
                    violation.setLine(Integer.parseInt(parts[1]));
                    violation.setDescription((String) caneViolationsLineResultRow.get(":description"));
                    violations.add(violation);
                }
            }

            // extract the cane comment violations
            ArrayList<Map<String, Object>> caneViolationsCommentResult =
                    (ArrayList<Map<String, Object>>) caneViolations.get(":comment");

            // iterate the comment violations and find issues corresponding to the requested file
            for (Map<String, Object> caneViolationsLineResultRow : caneViolationsCommentResult) {
                String parts[] = ((String) caneViolationsLineResultRow.get(":line")).split(":");
                if (parts[0].length() > 0 && parts[0].equals(filename)) {

                    // push in a new comment violation
                    CaneCommentViolation violation = new CaneCommentViolation();
                    violation.setFile(parts[0]);
                    violation.setLine(Integer.parseInt(parts[1]));
                    violation.setClassName((String) caneViolationsLineResultRow.get(":class_name"));
                    violations.add(violation);
                }
            }
        }

        // return the cane violations list
        return violations;
    }

    /**
     * Parse the roodi problems and return a list of
     * problems corresponding to the requested file
     *
     * @param filename the filename to collect the problems for
     * @return list of roodi problems corresponding to the given file
     */
    public List<RoodiProblem> parseRoodi(String filename) {

        // initialize the output roodi problems list
        List<RoodiProblem> problems = new ArrayList<>();

        // verify the metricfu results have been parsed
        if (metricfuResult == null) {
            LOG.warn("No metricfu results for roodi.");
            return problems;
        }

        // load the roodi problems from the metricfu results if needed
        if (roodiProblems == null) {
            Map<String, Object> roodi = (Map<String, Object>) metricfuResult.get(":roodi");
            if (roodi != null) {
                roodiProblems = (ArrayList<Map<String, Object>>) roodi.get(":problems");
            }
        }

        // if roodi problems exist, analyze and save the corresponding problems
        if (roodiProblems != null) {

            // iterate the roodi problems and select the corresponding problems
            for (Map<String, Object> prob : roodiProblems) {
                String file = escapePattern.matcher(safeString((String) prob.get(":file"))).replaceAll("");
                if (filename.equals(file)) {

                    // create a new roodi problem and set it's appropriate data
                    RoodiProblem problem = new RoodiProblem();
                    problem.setFile(file);
                    problem.setLine(safeInteger((String) prob.get(":line")));
                    problem.setProblem(escapePattern.matcher(safeString((String) prob.get(":problem"))).replaceAll(""));

                    // save in the new problem if it's valid
                    if (problem.getFile().length() > 0 && problem.getLine() > 0) {
                        problems.add(problem);
                    }
                }
            }
        }

        //return the output roodi problems list
        return problems;
    }

    /**
     * Parse metricfu reek smell results and return a list of reek
     * smells corresponding to a given project filename
     *
     * @param filename filename to search results for
     * @return list of reek smells corresponding to the given file
     */
    public List<ReekSmell> parseReek(String filename) {

        // initialize the reek smells output list
        List<ReekSmell> smells = new ArrayList<>();

        // verify the metricfu results have been parsed
        if (metricfuResult == null) {
            LOG.warn("No metricfu results for reek.");
            return smells;
        }

        // load the reek smells from the metricfu results if needed
        if (reekFiles == null) {
            Map<String, Object> reek = (Map<String, Object>) metricfuResult.get(":reek");
            if (reek != null) {
                reekFiles = (ArrayList<Map<String, Object>>) reek.get(":matches");
            }
        }

        // if reek results exist and are loaded,
        // analyze and save corresponding results
        if (reekFiles != null) {

            // iterate source files smell results and analyze
            // the ones corresponding to the requested file
            for (Map<String, Object> resultFile : reekFiles) {
                String file = safeString((String) resultFile.get(":file_path"));
                if (filename.equals(file)) {

                    // fetch and iterate the reek code smells for the requested file
                    ArrayList<Map<String, Object>> resultSmells =
                            (ArrayList<Map<String, Object>>) resultFile.get(":code_smells");
                    for (Map<String, Object> resultSmell : resultSmells) {

                        // create a new reek smell object and set the reek results
                        // TODO: check if it's needed to add the lines result as well, if could be also added to the Sensor
                        ReekSmell smell = new ReekSmell();
                        smell.setFile(file);
                        smell.setMethod(safeString((String) resultSmell.get(":method")));
                        smell.setMessage(safeString((String) resultSmell.get(":message")));
                        smell.setType(safeString((String) resultSmell.get(":type")));

                        // add smell violation line locations
                        for (int line : (List<Integer>) resultSmell.get(":lines")) {
                            smell.addLine(line);
                        }

                        // add the new smell to the output list
                        smells.add(smell);
                    }
                    break;
                }
            }
        }

        // return the reek smells output list for the requested file
        return smells;
    }

    /**
     * Parse metricfu flay reasons and return a list of
     * all of the reasons with their matches
     *
     * @return list of flay reasons which includes their matches
     */
    public List<FlayReason> parseFlay() {

        // initialize the flay reasons output list
        List<FlayReason> reasons = new ArrayList<>();

        // verify the metricfu results have been parsed
        if (metricfuResult == null) {
            LOG.warn("No metricfu results for flay.");
            return reasons;
        }

        // analyze and load the flay reasons from the metricfu results if needed
        if (flayReasons == null) {
            Map<String, Object> flay = (Map<String, Object>) metricfuResult.get(":flay");
            if (flay != null) {
                flayReasons = (ArrayList<Map<String, Object>>) flay.get(":matches");
            }
        }

        // if flay reasons exist and parsed, load and save the reasons
        if (flayReasons != null) {

            // iterate the flay reasons and save them
            for (Map<String, Object> resultReason : flayReasons) {

                // create new flay reason and set it's reason result
                // TODO: might need to remove reason number and score from reason string(check result format)
                FlayReason reason = new FlayReason();
                reason.setReason(safeString((String) resultReason.get(":reason")));

                // iterate the reason matches and save them into it
                ArrayList<Map<String, Object>> resultMatches =
                        (ArrayList<Map<String, Object>>) resultReason.get(":matches");
                for (Map<String, Object> resultDuplication : resultMatches) {

                    // create a new reason match and set it's data
                    Match match = reason.new Match((String) resultDuplication.get(":name"));

                    // If flay was run with --diff, we should have the number
                    // of lines in the duplication. If not, make it 1.
                    Integer line = safeInteger((String) resultDuplication.get(":line"));
                    if (line > 0) {
                        match.setStartLine(line);
                        match.setLines(1);
                    } else {
                        Integer start = safeInteger((String) resultDuplication.get(":start"));
                        if (start > 0) {
                            match.setStartLine(start);
                        }
                        Integer lines = safeInteger((String) resultDuplication.get(":lines"));
                        if (lines > 0) {
                            match.setLines(lines);
                        }
                    }

                    // save the match into the flay reason
                    reason.getMatches().add(match);
                }

                // save the new reason
                reasons.add(reason);
            }
        }

        // return the flay reasons output list
        return reasons;
    }

    private String safeString(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    private Integer safeInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
