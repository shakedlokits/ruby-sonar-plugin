package com.godaddy.sonar.ruby.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Scopes;

import java.io.File;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RubyFileTest {
    protected final static String SOURCE_FILE = "/path/to/source/file.rb";

    protected RubyFile rubyFile;

    @Before
    public void setUp() {
        Project project = new Project("test project");

        File file = new File(SOURCE_FILE);
        List<InputFile> sourceDirs = new ArrayList<InputFile>();

        DefaultInputFile difFile = new DefaultInputFile(project.getKey(), file.getParent());
        difFile.setModuleBaseDir(FileSystems.getDefault().getPath("/"));
        sourceDirs.add(difFile);

        rubyFile = new RubyFile(file, sourceDirs);
    }

    @After
    public void tearDown() {

    }


    @Test(expected = IllegalArgumentException.class)
    public void testRubyFileWithNullFile() {
        new RubyFile(null, new ArrayList<InputFile>());
    }

    @Test
    public void testRubyFileWithNullSourceDirs() {
        File file = new File(SOURCE_FILE);
        rubyFile = new RubyFile(file, null);
        assertEquals("[default].file", rubyFile.getKey());
    }

    @Test
    public void testGetParent() {
        RubyPackage parent = rubyFile.getParent();
        assertEquals("source", parent.getKey());
    }

    @Test
    public void testGetDescription() {
        assertNull(rubyFile.getDescription());
    }

    @Test
    public void testGetLanguage() {
        assertEquals(Ruby.INSTANCE, rubyFile.getLanguage());
    }

    @Test
    public void testGetName() {
        assertEquals("file", rubyFile.getName());
    }

    @Test
    public void testGetLongName() {
        assertEquals("source.file", rubyFile.getLongName());
    }

    @Test
    public void testGetScope() {
        assertEquals(Scopes.FILE, rubyFile.getScope());
    }

    @Test
    public void testGetQualifier() {
        assertEquals(Qualifiers.FILE, rubyFile.getQualifier());
    }

    @Test
    public void testMatchFilePatternString() {
        assertTrue(rubyFile.matchFilePattern("source.file.rb"));
    }

//	@Test
//	public void testToString() {
//		System.out.println(rubyFile.toString());
//		assertTrue(rubyFile.toString().contains("key=source.file,package=source,longName=source.file"));
//	}

}
