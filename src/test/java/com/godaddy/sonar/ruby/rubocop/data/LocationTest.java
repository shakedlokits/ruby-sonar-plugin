package com.godaddy.sonar.ruby.rubocop.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by sergio on 3/13/17.
 */
public class LocationTest {
    @Test
    public void testSettersAndGetters() throws Exception {
        Location location = new Location();
        location.setColumn(10);
        location.setLine(21);
        location.setLength(40);

        assert location.getColumn() == 10;
        assert location.getLine() == 21;
        assert location.getLength() == 40;
    }
}