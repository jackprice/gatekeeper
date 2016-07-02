package io.gatekeeper.cli;

import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private Map<String, AbstractCommand> commands;

    public static Router create() {
        return new Router();
    }

    private Router() {
        this.commands = new HashMap<>();
    }

    public <T extends AbstractCommand> Router addCommand(Class<T> clazz) throws
        IllegalAccessException,
        InstantiationException {
        AbstractCommand instance = clazz.newInstance();

        instance.configure();

        this.commands.put(instance.getName(), instance);

        return this;
    }

    public void execute(String[] args) {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();
        CommandLine line;

        try {
            line = parser.parse(options, args, false);
        } catch (ParseException e) {
            System.err.println("Could not parse command-line options - exiting");

            e.printStackTrace();

            return;
        }

        if (line.getArgList().size() != 1) {
            printUsage();

            return;
        }

        if (!commands.containsKey(line.getArgList().get(0))) {
            printUsage();

            return;
        }

        AbstractCommand command = commands.get(line.getArgList().get(0));

        try {
            line = parser.parse(command.getOptions(), args, false);
        } catch (ParseException e) {
            System.err.println("Could not parse command-line options - exiting");

            e.printStackTrace();

            return;
        }

        Output output = new Output();
        Input input = new Input(args, line);

        try {
            command.run(input, output);
        } catch (Exception exception) {
            System.err.println("Command threw an exception - exiting");

            exception.printStackTrace();

            return;
        }
    }

    private void printUsage() {
        System.out.println("usage: gatekeeper <command> [<args>]");
        System.out.println("");
        System.out.println("Available commands are:");

        commands.entrySet().forEach((set) -> {
            String name = set.getKey();
            String description = set.getValue().getDescription();

            System.out.println(
                String.format("    %-10s %s", name, description)
            );
        });
    }
}
