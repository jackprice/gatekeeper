package io.gatekeeper.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * A class that takes a configuration files and returns the parsed result.
 */
public class FileParser<T extends ConfigurationInterface> {

    private final Class<T> clazz;

    private final String path;

    private final ConfigurationReader reader = new ConfigurationReader();

    /**
     * Constructor.
     *
     * @param clazz The configuration class this parser instantiates
     * @param path  The path to the config file
     */
    public FileParser(Class<T> clazz, String path) {
        this.clazz = clazz;
        this.path = path;
    }

    /**
     * Parse the configuration file
     *
     * @return The parsed configuration
     */
    public T parse() throws IOException, InstantiationException, IllegalAccessException {
        String yaml = this.read();

        return this.parseYaml(yaml);
    }

    /**
     * Read the contents of this config file.
     *
     * @return The contents of the config file
     *
     * @throws IOException
     */
    private String read() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(this.path));

        return new String(encoded, StandardCharsets.UTF_8);
    }

    /**
     * Parse the given YAML string into a configuration object.
     *
     * @param string Raw YAML content
     *
     * @return The parsed configuration object
     */
    @SuppressWarnings("unchecked")
    private T parseYaml(String string) throws IllegalAccessException, InstantiationException {
        Yaml yaml = new Yaml();

        Map<String, Object> data = (Map<String, Object>) yaml.loadAs(string, Map.class);

        return this.reader.createConfigurationObjectFromData(this.clazz, data);
    }
}
