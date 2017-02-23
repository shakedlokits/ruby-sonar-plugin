package com.godaddy.sonar.ruby.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Scopes;

import static org.junit.Assert.*;

public class RubyPackageTest {
    private final static String keyName = "path.to.key";
    protected RubyPackage rubyPackage;

    @Before
    public void setUp() {
        rubyPackage = new RubyPackage(keyName);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testMatchFilePatternString() {
        assertTrue(rubyPackage.matchFilePattern("path.to.key.other-name"));
    }

    @Test
    public void testGetDescription() {
        assertNull(rubyPackage.getDescription());
    }

    @Test
    public void testGetName() {
        assertEquals(keyName, rubyPackage.getKey());
    }

    @Test
    public void testGetParent() {
        assertNull(rubyPackage.getParent());
    }

    @Test
    public void testGetLongName() {
        assertNull(rubyPackage.getLongName());
    }

    @Test
    public void testGetLanguage() {
        assertEquals(Ruby.INSTANCE, rubyPackage.getLanguage());
    }

    @Test
    public void testToString() {
        assertTrue(rubyPackage.toString().contains("key=path.to.key"));
    }

    @Test
    public void testGetScope() {
        assertEquals(Scopes.DIRECTORY, rubyPackage.getScope());
    }

    @Test
    public void testGetQualifier() {
        assertEquals(Qualifiers.MODULE, rubyPackage.getQualifier());
    }

}
