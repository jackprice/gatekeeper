package io.gatekeeper;

import io.gatekeeper.cli.Router;
import io.gatekeeper.cli.command.AgentCommand;
import io.gatekeeper.cli.command.DevelopCommand;
import io.gatekeeper.cli.command.VersionCommand;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.DirectoryParser;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import io.gatekeeper.node.Node;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Main {

    /**
     * This is the main CLI entry point.
     *
     * @param args
     */
    public static void main(String[] args) throws
        ExecutionException,
        InterruptedException,
        InstantiationException,
        IllegalAccessException {

        Router.create()
            .addCommand(AgentCommand.class)
            .addCommand(DevelopCommand.class)
            .addCommand(VersionCommand.class)
            .execute(args);
    }
}
