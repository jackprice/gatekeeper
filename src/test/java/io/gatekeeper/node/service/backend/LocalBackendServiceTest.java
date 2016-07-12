package io.gatekeeper.node.service.backend;

import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.backend.LocalBackendConfiguration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import io.gatekeeper.node.service.ProviderService;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.replication.common.Node;
import io.gatekeeper.node.service.replication.common.ReplicationInformation;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LocalBackendServiceTest {

    @Test
    public void testStart() throws Exception {
        Configuration configuration = new Configuration();

        configuration.backend = new LocalBackendConfiguration();

        LocalBackendService service = new LocalBackendService(configuration, mockReplicationService(configuration), new ProviderService());

        service.start().get();
    }

    private ReplicationService mockReplicationService(Configuration configuration) {
        return new ReplicationService(configuration) {
            @Override
            public CompletableFuture<Void> start() {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletableFuture<Integer> countNodes() {
                return CompletableFuture.completedFuture(1);
            }

            @Override
            public CompletableFuture<List<Node>> fetchNodes() {
                return CompletableFuture.completedFuture(Collections.singletonList(new Node(
                    "localhost",
                    "localhost",
                    1234
                )));
            }

            @Override
            public void lock(String name) throws InterruptedException {
                // NOP
            }

            @Override
            public void unlock(String name) throws InterruptedException {
                // NOP
            }

            @Override
            public CompletableFuture<ReplicationInformation> getInformation() {
                return CompletableFuture.completedFuture(new ReplicationInformation(
                    "testing",
                    1,
                    null
                ));
            }
        };
    }
}
