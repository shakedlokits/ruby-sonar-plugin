package com.godaddy.sonar.ruby.rubocop.data;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class FileTest {
    @Test
    public void testSettersAndGetters() throws Exception {
        List<Offense> offenses = new ArrayList<>();
        offenses.add(new Offense());

        File file = new File();
        file.setPath("some/path/file.rb");
        file.setOffenses(offenses);

        assert file.getPath().equals("some/path/file.rb");
        assert file.getOffenses() == offenses;
    }
}