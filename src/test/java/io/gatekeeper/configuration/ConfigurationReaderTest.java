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
        replicationData.put("rpc_address", "127.0.0.1");
        replicationData.put("rpc_port", 1234);

        data.put("replication", replicationData);

        Configuration configuration = reader.createConfigurationObjectFromData(Configuration.class, data);

        assertEquals(true, configuration.replication.server);
        assertEquals("127.0.0.1", configuration.replication.rpcAddress);
        assertEquals((long) 1234, (long) configuration.replication.rpcPort);
        assertEquals(configuration.replication.getClass(), LocalReplicationConfiguration.class);
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testInvalidDiscriminator() throws IllegalAccessException, InstantiationException {
        ConfigurationReader reader  = new ConfigurationReader();

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> replicationData = new HashMap<>();

        replicationData.put("type", "foo");
        replicationData.put("server", true);
        replicationData.put("bootstrap", false);
        replicationData.put("rpc_address", "127.0.0.1");
        replicationData.put("rpc_port", 1234);

        data.put("replication", replicationData);

        reader.createConfigurationObjectFromData(Configuration.class, data);
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testInvalidListExpected() throws IllegalAccessException, InstantiationException {
        ConfigurationReader reader  = new ConfigurationReader();

        Map<String, Object> data = new HashMap<>();

        data.put("backend", "foo");

        reader.createConfigurationObjectFromData(Configuration.class, data);
    }

    @Test(expected = InvalidConfigurationException.class)
    public void testInvalidObjectExpected() throws IllegalAccessException, InstantiationException {
        ConfigurationReader reader  = new ConfigurationReader();

        Map<String, Object> data = new HashMap<>();

        data.put("replication", "foo");

        reader.createConfigurationObjectFromData(Configuration.class, data);
    }
}
