package io.gatekeeper.node.service;

import io.gatekeeper.GatekeeperTest;
import io.gatekeeper.configuration.Configuration;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;

public class ReplicationServiceTest extends GatekeeperTest {

    @Test
    public void testCanBootstrapSingleNodeCluster() throws IOException {
        Path dataDirectory = createTemporaryDirectory();

        Configuration configuration = new Configuration();

        configuration.replication.bindAddress = "127.0.0.1";
        configuration.replication.bindPort = 63512;
        configuration.replication.dataDirectory = dataDirectory.toString();
        configuration.replication.bootstrap = Boolean.TRUE;
        configuration.replication.server = Boolean.TRUE;

        ReplicationService replication = new ReplicationService(configuration);

        replication.start().join();

        replication.close();
    }

    @Test
    public void testCanBootstrapThreeNodeCluster() throws IOException, InterruptedException {
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
        configurationA.replication.bootstrap = Boolean.TRUE;
        configurationB.replication.bootstrap
            = configurationC.replication.bootstrap
            = Boolean.FALSE;
        configurationA.replication.server
            = configurationB.replication.server
            = configurationC.replication.server
            = Boolean.TRUE;

        configurationA.replication.dataDirectory = dataDirectoryA.toString();
        configurationB.replication.dataDirectory = dataDirectoryB.toString();
        configurationC.replication.dataDirectory = dataDirectoryC.toString();

        configurationA.replication.bindPort = 63513;
        configurationB.replication.bindPort = 63514;
        configurationC.replication.bindPort = 63515;

        List<String> nodes = Arrays.asList("127.0.0.1:63513", "127.0.0.1:63514", "127.0.0.1:63515");

        configurationA.replication.nodes.addAll(nodes);
        configurationB.replication.nodes.addAll(nodes);
        configurationC.replication.nodes.addAll(nodes);

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
