package io.gatekeeper.configuration;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.data.ReplicationConfiguration;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ConfigurationReaderTest {

    @Test
    public void testValid() throws IllegalAccessException, InstantiationException {
        ConfigurationReader reader  = new ConfigurationReader();

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> replicationData = new HashMap<>();

        replicationData.put("type", "local");
        replicationData.put("server", true);
        replicationData.put("bootstrap", false);
        replicationData.put("address", "127.0.0.1");
        replicationData.put("port", 1234);

        data.put("replication", replicationData);

        Configuration configuration = reader.createConfigurationObjectFromData(Configuration.class, data);

        assertEquals(true, configuration.replication.server);
        assertEquals(false, configuration.replication.bootstrap);
        assertEquals("127.0.0.1", configuration.replication.address);
        assertEquals((long) 1234, (long) configuration.replication.port);
        assertEquals(configuration.replication.getClass(), LocalReplicationConfiguration.class);
    }
}
