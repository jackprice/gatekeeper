package io.gatekeeper.cli;

import org.apache.commons.cli.CommandLine;

public class Input {

    private final String[] arguments;

    private final CommandLine line;

    public Input(String[] arguments, CommandLine line) {
        assert null != arguments;
        assert null != line;

        this.arguments = arguments;
        this.line = line;
    }

    public String getArgument(String name) {
        assert null != name;
        assert name.length() > 0;

        return line.getOptionValue(name);
    }

    public boolean getFlag(String name) {
        assert null != name;
        assert name.length() > 0;

        return line.hasOption(name);
    }

    public String[] getOriginalArguments() {
        return arguments;
    }

}