package io.gatekeeper.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class DomainTest {

    @Test
    public void testCreate() {
        Domain domain = new Domain("foo.com");

        assertNotNull(domain.id());
        assertEquals("foo.com", domain.toString());
    }

    @Test
    public void testEquals() {
        Domain domainA = new Domain("foo.com");
        Domain domainB = new Domain("foo.com");

        assertTrue(domainA.equals(domainB));
    }

    @Test
    public void testNotEquals() {
        Domain domainA = new Domain("foo.com");
        Domain domainB = new Domain("bar.com");

        assertFalse(domainA.equals(domainB));
    }

    @Test
    public void testMatcher() {
        Domain domain = new Domain("foo.bar.com");

        // An exact domain match is always true
        assertTrue(domain.matches("foo.bar.com"));

        // Anything below the matching domain is also true
        assertTrue(domain.matches("com"));
        assertTrue(domain.matches("bar.com"));

        // Wildcards allow matching on any domain component
        assertTrue(domain.matches("*.bar.com"));

        // ...as well as following the subdomain matching rule
        assertTrue(domain.matches("*.com"));

        // There can be multiple wildcards at any place
        assertTrue(domain.matches("*.*.com"));

        // However anything above the matched domain is not a match
        assertFalse(domain.matches("below.foo.bar.com"));

        assertTrue(domain.matches("*.bar.com"));
        assertTrue(domain.matches("*.com"));
        assertTrue(domain.matches("*"));
        assertTrue(domain.matches("bar.com"));
        assertFalse(domain.matches("foo.com"));
        assertFalse(domain.matches("*.foo.foo.com"));
        assertFalse(domain.matches("*.foo.bar.com"));
    }
}
