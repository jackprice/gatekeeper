package io.gatekeeper.node.service;

import io.gatekeeper.GatekeeperTest;
import io.gatekeeper.configuration.Configuration;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ReplicationServiceTest extends GatekeeperTest {

    @Test
    public void testCanBootstrapSingleNodeCluster() throws IOException {
        Path dataDirectory = createTemporaryDirectory();

        Configuration configuration = new Configuration();

        configuration.replication.bindAddress = "127.0.0.1";
        configuration.replication.bindPort = 63512;
        configuration.replication.dataDirectory = dataDirectory.toString();
        configuration.replication.bootstrap = Boolean.TRUE;

        ReplicationService replication = new ReplicationService(configuration);

        replication.start().join();

        replication.close();
    }

    @Test
    public void testCanBootstrapThreeNodeCluster() throws IOException {
        Path dataDirectoryA = createTemporaryDirectory();
        Path dataDirectoryB = createTemporaryDirectory();
        Path dataDirectoryC = createTemporaryDirectory();

        Configuration configurationA = new Configuration();
        Configuration configurationB = new Configuration();
        Configuration configurationC = new Configuration();

        configurationA.replication.bindAddress
            = configurationB.replication.bindAddress
            = configurationC.replication.bindAddress
            = "127.0.0.1";
        configurationA.replication.bootstrap
            = configurationB.replication.bootstrap
            = configurationC.replication.bootstrap
            = Boolean.TRUE;

        configurationA.replication.dataDirectory = dataDirectoryA.toString();
        configurationB.replication.dataDirectory = dataDirectoryB.toString();
        configurationC.replication.dataDirectory = dataDirectoryC.toString();

        configurationA.replication.bindPort = 63513;
        configurationB.replication.bindPort = 63514;
        configurationC.replication.bindPort = 63515;

        ReplicationService replicationA = new ReplicationService(configurationA);
        ReplicationService replicationB = new ReplicationService(configurationB);
        ReplicationService replicationC = new ReplicationService(configurationC);

        CompletableFuture completableA = replicationA.start();
        CompletableFuture completableB = replicationB.start();
        CompletableFuture completableC = replicationC.start();

        completableA.join();
        completableB.join();
        completableC.join();

        replicationA.close();
        replicationB.close();
        replicationC.close();
    }
}
