package io.gatekeeper.configuration;

import io.gatekeeper.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

interface ConfigurationInterface {
    public void merge(ConfigurationInterface configuration);

    public void fromMap(Map<String, Object> map) throws InvalidConfigurationException;
}

/**
 * A container for holding runtime configuration.
 */
public class Configuration implements ConfigurationInterface {

    public ReplicationConfiguration replication = new ReplicationConfiguration();

    @Override
    public void merge(ConfigurationInterface configuration) {
        this.replication.merge(((Configuration) configuration).replication);
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        if (map.containsKey("replication")) {
            this.replication.fromMap((Map<String, Object>) map.get("replication"));
        }
    }

    public class ReplicationConfiguration implements ConfigurationInterface {
        public String bindAddress;
        public Integer bindPort;
        public String dataDirectory;
        public List<String> nodes = new ArrayList<String>(0);

        @Override
        public void merge(ConfigurationInterface configuration) {
            ReplicationConfiguration config = (ReplicationConfiguration) configuration;

            if (config.bindAddress != null) {
                this.bindAddress = config.bindAddress;
            }
            if (config.bindPort != null) {
                this.bindPort = config.bindPort;
            }
            if (config.dataDirectory != null) {
                this.dataDirectory = config.dataDirectory;
            }

            this.nodes.addAll(config.nodes);
        }

        @Override
        public void fromMap(Map<String, Object> map) throws InvalidConfigurationException {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                switch (key) {
                    case "address":
                        this.bindAddress = value.toString();
                        break;
                    case "port":
                        this.bindPort = Integer.parseInt(value.toString());
                        break;
                    case "directory":
                        this.dataDirectory = value.toString();
                        break;
                    case "nodes":
                        this.nodes = (List<String>) value;
                        break;
                    default:
                        throw new InvalidConfigurationException(
                                String.format("Unknown configuration value %s", key)
                        );
                }
            }
        }
    }
}
