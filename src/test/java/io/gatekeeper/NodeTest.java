package io.gatekeeper;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.node.Node;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.Service;
import io.gatekeeper.node.service.replication.LocalReplicationService;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class NodeTest {

    @Test
    public void testAddService() {
        Configuration configuration = new Configuration();

        Node node = new Node(configuration);

        ReplicationService replication = new LocalReplicationService(configuration);

        node.service(ReplicationService.class, replication);

        assertEquals(replication, node.service(ReplicationService.class));
    }

    @Test
    public void testAddServiceGeneric() {
        Configuration configuration = new Configuration();

        Node node = new Node(configuration);

        Service service = new Service() {
            @Override
            public CompletableFuture start() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void close() throws IOException {

            }
        };

        node.service(Service.class, service);

        assertEquals(service, node.service(Service.class));
    }

    @Test
    public void testStart() {
        Configuration configuration = new Configuration();

        Node node = new Node(configuration);

        Service service = new Service() {
            @Override
            public CompletableFuture start() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void close() throws IOException {

            }
        };

        node.service(Service.class, service);

        assertEquals(service, node.service(Service.class));
    }
}
