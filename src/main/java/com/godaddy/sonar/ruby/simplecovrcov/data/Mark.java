package com.godaddy.sonar.ruby.simplecovrcov.data;

/**
 * Created by sergio on 2/8/17.
 */
public class Mark {
    private String rawValue;
    private Boolean isNull;

    public Mark(String rawValue, Boolean isNull) {
        this.rawValue = rawValue;
        this.isNull = isNull;
    }

    public String getRawValue() {
        return rawValue;
    }

    public Long getAsLong() {
        return Long.parseLong(this.rawValue);
    }

    public Boolean getIsNull() {
        return isNull;
    }
}
