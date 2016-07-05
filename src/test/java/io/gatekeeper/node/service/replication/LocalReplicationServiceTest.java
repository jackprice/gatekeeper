package io.gatekeeper.node.service.replication;

import io.gatekeeper.GatekeeperTest;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import io.gatekeeper.node.service.ReplicationService;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class LocalReplicationServiceTest extends GatekeeperTest {

    @Test
    public void testStart() throws IOException, ExecutionException, InterruptedException {
        Configuration configuration = new Configuration();

        configuration.replication = new LocalReplicationConfiguration();

        ReplicationService service = new LocalReplicationService(configuration);

        service.start().join();

        service.close();
    }

    @Test
    public void testCountNodes() throws IOException, ExecutionException, InterruptedException {
        Configuration configuration = new Configuration();

        configuration.replication = new LocalReplicationConfiguration();

        LocalReplicationService service = new LocalReplicationService(configuration);

        service.start().join();

        Integer count = service.countNodes().get();

        assertEquals((long) 1, (long) count);

        service.close();
    }
}
