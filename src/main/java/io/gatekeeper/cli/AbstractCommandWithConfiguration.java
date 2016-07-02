package io.gatekeeper.cli;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.DirectoryParser;
import io.gatekeeper.configuration.FileParser;
import org.apache.commons.cli.*;

import java.io.IOException;

public abstract class AbstractCommandWithConfiguration extends AbstractCommand {

    private Configuration configuration;

    protected Configuration getConfiguration(Input input) throws Exception {
        if (configuration == null) {
            try {
                buildConfiguration(input);
            } catch (InvalidConfigurationException invalid) {
                throw invalid;
            } catch (Exception e) {
                throw new Exception(e.getMessage(), e);
            }
        }

        return configuration;
    }

    private void buildConfiguration(Input input) throws
        ParseException,
        IllegalAccessException,
        IOException,
        InstantiationException {
        String[] args = input.getOriginalArguments();
        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        Options options = new Options();

        options.addOption(null, "bootstrap", false, null);
        options.addOption(null, "server", false, null);
        options.addOption(null, "config-dir", true, null);
        options.addOption(null, "config-file", true, null);

        line = parser.parse(options, args);

        configuration = new Configuration();

        for (Option option : line.getOptions()) {
            switch (option.getLongOpt()) {
                case "config-dir":
                    DirectoryParser<Configuration> configParser = new DirectoryParser<>(Configuration.class, option.getValue());
                    Configuration directoryConfiguration = configParser.parse();

                    configuration.merge(directoryConfiguration);
                    break;
                case "config-file":
                    FileParser<Configuration> fileParser = new FileParser<>(Configuration.class, option.getValue());
                    Configuration fileConfiguration = fileParser.parse();

                    configuration.merge(fileConfiguration);

                    break;
            }
        }

        if (options.hasLongOption("bootstrap")) configuration.replication.bootstrap = Boolean.TRUE;
        if (options.hasLongOption("server")) configuration.replication.server = Boolean.TRUE;
    }
}