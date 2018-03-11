package com.godaddy.sonar.ruby.rubocop.data;

/**
 * Created by sergio on 3/13/17.
 */
public class Location {
    private Integer line;
    private Integer column;
    private Integer length;

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}
