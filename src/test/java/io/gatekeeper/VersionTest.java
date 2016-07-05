package io.gatekeeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionTest {

    @Test
    public void testVersionConstantPresent() {
        assertEquals(Version.CURRENT, Version.fromId(Version.CURRENT.id));
    }

    @Test
    public void testMinimumCompatibleVersionIsCurrentForBelow1() {
        assertEquals(Version.CURRENT, Version.CURRENT.minimumCompatibibleVersion());
    }

    @Test
    public void testFromIdDefaultsToCurrent() {
        assertEquals(Version.CURRENT, Version.fromId(92121));
        assertEquals(Version.CURRENT, Version.fromId(290231));
        assertEquals(Version.CURRENT, Version.fromId(378901247));
    }

    @Test
    public void testToString() {
        assertEquals("0.0.0", Version.V0_0_0.toString());
        assertEquals("1.3.2", (new Version(1030200)).toString());
        assertEquals("4.59.12", (new Version(4591200)).toString());
    }

    @Test
    public void testEquals() {
        Version a = new Version(1084910);
        Version b = new Version(1084910);
        Version c = new Version(3829193);

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertFalse(a.equals(c));
        assertFalse(c.equals(b));
        assertFalse(c.equals(null));
    }
}
