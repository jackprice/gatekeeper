package io.gatekeeper.model;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class EndpointModelTest {

    @Test
    public void testCreate() {
        EndpointModel endpoint = new EndpointModel();

        assertNotNull(endpoint.id());

        DomainModel domain = new DomainModel("foo.com");

        endpoint.add(domain);

        assertEquals(Collections.singletonList(domain), endpoint.domains());
    }

    @Test
    public void testContains() {
        EndpointModel endpoint = new EndpointModel();

        assertNotNull(endpoint.id());

        DomainModel domain = new DomainModel("foo.com");

        endpoint.add(domain);

        assertTrue(endpoint.contains(domain));
        assertTrue(endpoint.contains(new DomainModel("foo.com")));
    }

    @Test
    public void testMatches() {
        EndpointModel endpoint = new EndpointModel();

        assertNotNull(endpoint.id());

        DomainModel domain = new DomainModel("foo.bar.com");

        endpoint.add(domain);

        assertTrue(endpoint.matches("bar.com"));
        assertTrue(endpoint.matches("*.bar.com"));
        assertTrue(endpoint.matches("*"));
        assertFalse(endpoint.matches("example.com"));
        assertFalse(endpoint.matches("example.foo.bar.com"));
    }
}
