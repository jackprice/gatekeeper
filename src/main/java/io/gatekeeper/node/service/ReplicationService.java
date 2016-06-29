package io.gatekeeper.node.service;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.NettyTransport;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.util.Listener;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.group.DistributedGroup;
import io.atomix.group.GroupMember;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.logging.Loggers;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ReplicationService implements Closeable {

    private Configuration configuration;

    private DistributedGroup servers;

    private Logger logger;

    private AtomixReplica replica;

    private Groups groups = new Groups();

    public ReplicationService(Configuration configuration) {
        this.configuration = configuration;
        this.logger = Loggers.getReplicationLogger();
    }

    public CompletableFuture start() {
        return CompletableFuture.runAsync(() -> {
            Address address = new Address(
                this.configuration.replication.bindAddress,
                this.configuration.replication.bindPort
            );
            Transport transport = new NettyTransport();
            Storage storage = new Storage(this.configuration.replication.dataDirectory);

            this.replica = AtomixReplica.builder(address)
                .withTransport(transport)
                .withStorage(storage)
                .build();

            if (this.configuration.replication.bootstrap) {
                this.bootstrap().join();
            } else {
                this.joinCluster().join();
            }

            this.setUpGroups().join();
        });
    }

    private CompletableFuture setUpGroups() {
        return CompletableFuture.runAsync(() -> {
            this.groups.servers = this.replica.getGroup(Groups.GROUP_SERVERS).join();

            if (this.configuration.replication.bootstrap) {
                this.logger.info(String.format("Joining %s group", Groups.GROUP_SERVERS));

                this.groups.servers.join().join();
            }

            this.groups.servers.onJoin((GroupMember member) -> {
                this.logger.info(String.format("%s joined %s", member.id(), Groups.GROUP_SERVERS));
            });
        });
    }

    private CompletableFuture bootstrap() {
        return CompletableFuture.runAsync(() -> {
            this.logger.info("Bootstrapping cluster");

            List<Address> nodes = this.getNodes();

            if (nodes.size() == 1) {
                this.logger.warning("Bootstrapping cluster with one node");

                this.replica.bootstrap().join();
            } else {
                this.replica.bootstrap(nodes).join();
            }

            this.logger.info("Cluster bootstrapped");
        });
    }

    private CompletableFuture joinCluster() {
        return CompletableFuture.runAsync(() -> {
            this.logger.info("Joining cluster");

            List<Address> nodes = this.getNodes();

            if (nodes.size() == 1) {
                this.logger.warning("Joining cluster with one node - will not be able to bootstrap");

                this.replica.join();
            } else {
                this.replica.join(nodes);
            }

            this.logger.info("Joined cluster");
        });
    }

    private List<Address> getNodes() {
        List<Address> nodes = new ArrayList<>(this.configuration.replication.nodes.size() + 1);

        for (String node : this.configuration.replication.nodes) {
            Address address = new Address(node);

            nodes.add(address);
        }

        nodes.add(new Address(this.configuration.replication.bindAddress, this.configuration.replication.bindPort));

        return nodes;
    }

    @Override
    public void close() {
        this.logger.info("Leaving cluster");

        this.replica.leave().join();

        this.logger.info("Cluster shut down successfully");
    }

    private class Groups {
        public static final String GROUP_SERVERS = "servers";

        public DistributedGroup servers;
    }
}
