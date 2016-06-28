package io.gatekeeper;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class VersionTest {

    @Test
    public void testVersionConstantPresent() {
        assertEquals(Version.CURRENT, Version.fromId(Version.CURRENT.id));
    }

    @Test
    public void testMinimumCompatilbeVersionIsCurrentForBelow1() {
        assertEquals(Version.CURRENT, Version.CURRENT.minimumCompatibibleVersion());
    }
}
