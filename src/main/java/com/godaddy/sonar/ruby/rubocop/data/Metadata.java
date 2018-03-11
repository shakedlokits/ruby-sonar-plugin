package com.godaddy.sonar.ruby.rubocop.data;

/**
 * Created by sergio on 3/13/17.
 */
public class Metadata {
    private String rubocopVersion;
    private String rubyEngine;
    private String rubyVersion;
    private String rubyPatchlevel;
    private String rubyPlatform;

    public String getRubocopVersion() {
        return rubocopVersion;
    }

    public void setRubocopVersion(String rubocopVersion) {
        this.rubocopVersion = rubocopVersion;
    }

    public String getRubyEngine() {
        return rubyEngine;
    }

    public void setRubyEngine(String rubyEngine) {
        this.rubyEngine = rubyEngine;
    }

    public String getRubyVersion() {
        return rubyVersion;
    }

    public void setRubyVersion(String rubyVersion) {
        this.rubyVersion = rubyVersion;
    }

    public String getRubyPatchlevel() {
        return rubyPatchlevel;
    }

    public void setRubyPatchlevel(String rubyPatchlevel) {
        this.rubyPatchlevel = rubyPatchlevel;
    }

    public String getRubyPlatform() {
        return rubyPlatform;
    }

    public void setRubyPlatform(String rubyPlatform) {
        this.rubyPlatform = rubyPlatform;
    }
}
