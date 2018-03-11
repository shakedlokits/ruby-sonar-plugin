package com.godaddy.sonar.ruby.rubocop.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class MetadataTest {
    @Test
    public void testSettersAndGetters() throws Exception {
        Metadata metadata = new Metadata();
        metadata.setRubocopVersion("0.36.0");
        metadata.setRubyEngine("ruby");
        metadata.setRubyVersion("2.3.1");
        metadata.setRubyPatchlevel("112");
        metadata.setRubyPlatform("x86_64-darwin15");

        assert metadata.getRubocopVersion().equals("0.36.0");
        assert metadata.getRubyEngine().equals("ruby");
        assert metadata.getRubyVersion().equals("2.3.1");
        assert metadata.getRubyPatchlevel().equals("112");
        assert metadata.getRubyPlatform().equals("x86_64-darwin15");
    }
}