package io.gatekeeper.cli.command;

import io.gatekeeper.cli.AbstractCommand;
import io.gatekeeper.cli.AbstractCommandWithConfiguration;
import io.gatekeeper.cli.Input;
import io.gatekeeper.cli.Output;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import io.gatekeeper.node.Node;

public class DevelopCommand extends AbstractCommand {

    @Override
    public void run(Input input, Output output) {
        Configuration configuration = getConfiguration();

        Node node = new Node(configuration);

        node.start().join();
    }

    @Override
    public void configure() {
        this.setName("develop");
        this.setDescription("starts and runs the gatekeeper agent in development mode");
    }

    private Configuration getConfiguration() {
        Configuration configuration = new Configuration();

        configuration.replication = new LocalReplicationConfiguration();

        configuration.replication.server = Boolean.TRUE;

        return configuration;
    }
}
