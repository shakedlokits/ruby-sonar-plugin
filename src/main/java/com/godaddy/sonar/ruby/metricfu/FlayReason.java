package com.godaddy.sonar.ruby.metricfu;

import java.util.ArrayList;

public class FlayReason {

    private String reason;
    private ArrayList<Match> matches = new ArrayList<Match>();

    public FlayReason(String reason) {
        this.reason = reason;
    }

    public FlayReason() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ArrayList<Match> getMatches() {
        return matches;
    }

    public void addMatch(String file, Integer start) {
        matches.add(new Match(file, start));
    }

    @Override
    public String toString() {
        return "reason: " + reason;
    }

    public class Match {
        private String file;
        private Integer start;
        private Integer lines;

        public Match(String file, Integer start, Integer lines) {
            this.file = file;
            this.start = start;
            this.lines = lines;
        }

        public Match(String file, Integer start) {
            this(file, start, 1);
        }

        public Match(String file) {
            this(file, 1, 1);
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public Integer getStartLine() {
            return start;
        }

        public void setStartLine(Integer start) {
            this.start = start;
        }

        public Integer getLines() {
            return lines;
        }

        public void setLines(Integer lines) {
            this.lines = lines;
        }
    }
}
