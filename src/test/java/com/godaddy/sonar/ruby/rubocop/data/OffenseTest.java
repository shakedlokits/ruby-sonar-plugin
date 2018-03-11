package com.godaddy.sonar.ruby.rubocop.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class OffenseTest {
    @Test
    public void testSettersAndGetters() throws Exception {
        Offense offense = new Offense();
        offense.setSeverity("warning");
        offense.setMessage("message");
        offense.setCopName("Style/Abc");
        offense.setCorrected(false);
        offense.setLocation(new Location());

        assert offense.getSeverity().equals("warning");
        assert offense.getMessage().equals("message");
        assert offense.getCopName().equals("Style/Abc");
        assert offense.getCorrected() == false;
        assertNotNull(offense.getLocation());
    }
}