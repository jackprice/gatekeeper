package io.gatekeeper.configuration;

import java.io.File;
import java.io.IOException;

/**
 * A class that takes a directory of configuration files, parses them and returns the merged result.
 */
public class DirectoryParser<T extends ConfigurationInterface> {

    private final Class<T> clazz;

    private String path;

    /**
     * Constructor.
     *
     * @param clazz The configuration class this parser instantiates
     * @param path  The path to the directory containing config files.
     */
    public DirectoryParser(Class<T> clazz, String path) {
        this.clazz = clazz;
        this.path = path;
    }

    /**
     * Parse all the configuration files inside this directory.
     *
     * @return The parsed configuration
     */
    @SuppressWarnings("unchecked")
    public T parse() throws IOException, IllegalAccessException, InstantiationException {
        T configuration = clazz.newInstance();
        File[] files = new File(this.path).listFiles(); // TODO: Only list files with a likely extension

        assert files != null;

        for (File file : files) {
            FileParser<T> fileParser = new FileParser<T>(clazz, file.getPath());
            T fileConfiguration = fileParser.parse();

            configuration.merge(fileConfiguration);
        }

        return configuration;
    }
}
