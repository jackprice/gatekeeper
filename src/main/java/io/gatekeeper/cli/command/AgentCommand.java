package io.gatekeeper.cli.command;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.cli.AbstractCommandWithConfiguration;
import io.gatekeeper.cli.Input;
import io.gatekeeper.cli.Output;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.node.Node;

public class AgentCommand extends AbstractCommandWithConfiguration {

    @Override
    public void run(Input input, Output output) {
        Configuration configuration;

        try {
            configuration = getConfiguration(input);
        } catch (InvalidConfigurationException e) {
            output.println("Could not load configuration");
            output.println(e.getMessage());

            return;
        } catch (Exception e) {
            output.println("Could not load configuration");

            return;
        }

        Node node = new Node(configuration);

        node.start().join();
    }

    @Override
    public void configure() {
        super.configure();

        this.setName("agent");
        this.setDescription("starts and runs the gatekeeper agent");
    }
}
