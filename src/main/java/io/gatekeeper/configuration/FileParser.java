package io.gatekeeper.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * A class that takes a configuration files and returns the parsed result.
 */
public class FileParser {

    /**
     * The path to the config file.
     */
    private String path;

    /**
     * Constructor.
     *
     * @param path The path to the config file.
     */
    public FileParser(String path) {
        this.path = path;
    }

    /**
     * Parse the configuration file.
     *
     * @return The parsed configuration
     */
    public Configuration parse() throws IOException {
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
    private Configuration parseYaml(String string) {
        Yaml yaml = new Yaml();
        Configuration configuration = new Configuration();

        Object data = yaml.load(string);
        Map<String, Object> map = (Map<String, Object>) data;

        configuration.fromMap(map);

        return configuration;
    }
}
