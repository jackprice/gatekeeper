package io.gatekeeper.configuration;

import java.io.File;
import java.io.IOException;

/**
 * A class that takes a directory of configuration files, parses them and returns the merged result.
 */
public class DirectoryParser {

    /**
     * The path to the directory containing config files.
     */
    private String path;

    /**
     * Constructor.
     *
     * @param path The path to the directory containing config files.
     */
    public DirectoryParser(String path) {
        this.path = path;
    }

    /**
     * Parse all the configuration files inside this directory.
     *
     * @return The parsed configuration
     */
    public Configuration parse() throws IOException {
        Configuration configuration = new Configuration();
        File[] files = new File(this.path).listFiles(); // TODO: Only list files with a likely extension

        assert files != null;

        for (File file : files) {
            FileParser fileParser = new FileParser(file.getPath());
            Configuration fileConfiguration = fileParser.parse();

            configuration.merge(fileConfiguration);
        }

        return configuration;
    }
}
