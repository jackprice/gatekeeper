package io.gatekeeper.cli;

import org.apache.commons.cli.CommandLine;

public class Input {

    private final String[] arguments;

    private final CommandLine line;

    public Input(String[] arguments, CommandLine line) {
        this.arguments = arguments;
        this.line = line;
    }

    public String getArgument(String name) {
        return line.getOptionValue(name);
    }

    public boolean getFlag(String name) {
        return line.hasOption(name);
    }

    public String[] getOriginalArguments() {
        return arguments;
    }

}