package io.gatekeeper.node;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.Service;
import io.gatekeeper.node.service.replication.LocalReplicationService;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class ServiceContainerTest {

    @Test
    public void testAddService() {
        Configuration configuration = new Configuration();

        ServiceContainer container = new ServiceContainer();

        ReplicationService replication = new LocalReplicationService(configuration);

        container.service(ReplicationService.class, replication);

        assertEquals(replication, container.service(ReplicationService.class));
    }

    @Test
    public void testAddServiceGeneric() {
        Configuration configuration = new Configuration();

        ServiceContainer container = new ServiceContainer();

        Service service = new Service() {
            @Override
            public CompletableFuture start() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void close() throws IOException {

            }
        };

        container.service(Service.class, service);

        assertEquals(service, container.service(Service.class));
    }
}
