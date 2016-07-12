package io.gatekeeper.model;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class EndpointModelTest {

    @Test
    public void testCreate() {
        EndpointModel endpoint = new EndpointModel();

        assertNotNull(endpoint.getUuid());

        String domain = "foo.com";

        endpoint.addDomain(domain);

        assertEquals(Collections.singletonList(domain), endpoint.getDomains());
    }

    @Test
    public void testContains() {
        EndpointModel endpoint = new EndpointModel();

        assertNotNull(endpoint.getUuid());

        String domain = "foo.com";

        endpoint.addDomain(domain);

        assertTrue(endpoint.containsDomain(domain));
        assertTrue(endpoint.containsDomain("foo.com"));
    }

    @Test
    public void testMatches() {
        EndpointModel endpoint = new EndpointModel();

        assertNotNull(endpoint.getUuid());

        String domain = "foo.bar.com";

        endpoint.addDomain(domain);

        // An exact domain match is always true
        assertTrue(endpoint.matches("foo.bar.com"));

        // Anything below the matching domain is also true
        assertTrue(endpoint.matches("com"));
        assertTrue(endpoint.matches("bar.com"));

        // Wildcards allow matching on any domain component
        assertTrue(endpoint.matches("*.bar.com"));

        // ...as well as following the subdomain matching rule
        assertTrue(endpoint.matches("*.com"));

        // There can be multiple wildcards at any place
        assertTrue(endpoint.matches("*.*.com"));

        // However anything above the matched domain is not a match
        assertFalse(endpoint.matches("below.foo.bar.com"));

        assertTrue(endpoint.matches("*.bar.com"));
        assertTrue(endpoint.matches("*.com"));
        assertTrue(endpoint.matches("*"));
        assertTrue(endpoint.matches("bar.com"));
        assertFalse(endpoint.matches("foo.com"));
        assertFalse(endpoint.matches("*.foo.foo.com"));
        assertFalse(endpoint.matches("*.foo.bar.com"));
    }
}
