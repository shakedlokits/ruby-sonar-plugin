package com.godaddy.sonar.ruby.simplecovrcov.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by sergio on 2/8/17.
 */
public class MarkTest {
    @Test
    public void testGetters() throws Exception {
        Mark mark = new Mark("1", true);

        assertEquals(mark.getIsNull(), true);
        assertEquals(mark.getRawValue(), "1");
    }

    @Test
    public void testGetAsLongWhenRawValueIsOK() throws Exception {
        Mark mark = new Mark("1", false);

        assert mark.getAsLong() == 1;
    }
}