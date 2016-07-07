package io.gatekeeper.model;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class EndpointTest {

    @Test
    public void testCreate() {
        Endpoint endpoint = new Endpoint();

        assertNotNull(endpoint.id());

        Domain domain = new Domain("foo.com");

        endpoint.add(domain);

        assertEquals(Collections.singletonList(domain), endpoint.domains());
    }

    @Test
    public void testContains() {
        Endpoint endpoint = new Endpoint();

        assertNotNull(endpoint.id());

        Domain domain = new Domain("foo.com");

        endpoint.add(domain);

        assertTrue(endpoint.contains(domain));
        assertTrue(endpoint.contains(new Domain("foo.com")));
    }

    @Test
    public void testMatches() {
        Endpoint endpoint = new Endpoint();

        assertNotNull(endpoint.id());

        Domain domain = new Domain("foo.bar.com");

        endpoint.add(domain);

        assertTrue(endpoint.matches("bar.com"));
        assertTrue(endpoint.matches("*.bar.com"));
        assertTrue(endpoint.matches("*"));
        assertFalse(endpoint.matches("example.com"));
        assertFalse(endpoint.matches("example.foo.bar.com"));
    }
}
