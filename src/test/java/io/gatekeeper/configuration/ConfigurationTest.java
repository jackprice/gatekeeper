package io.gatekeeper.configuration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.*;

public class ConfigurationTest {

    @Test
    public void testParseValidConfiguration() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> replicationData = new HashMap<>();

        replicationData.put("address", "127.0.0.1");
        replicationData.put("port", 5000);
        replicationData.put("directory", "foo");
        replicationData.put("nodes", Arrays.asList("10.1.1.1", "10.1.1.2"));

        data.put("replication", replicationData);

        Configuration configuration = new Configuration();

        configuration.fromMap(data);

        assertEquals("127.0.0.1", configuration.replication.bindAddress);
        assertEquals((long) 5000, (long) configuration.replication.bindPort);
        assertEquals("foo", configuration.replication.dataDirectory);
        assertEquals(Arrays.asList("10.1.1.1", "10.1.1.2"), configuration.replication.nodes);
    }

    @Test
    public void testMergeConfigurations() {
        Configuration configurationOne = new Configuration();
        Configuration configurationTwo = new Configuration();

        configurationOne.replication.bindAddress = "127.0.0.1";
        configurationOne.replication.bindPort = 5000;
        configurationOne.replication.dataDirectory = "foo";
        configurationTwo.replication.nodes.add("10.1.1.2");

        configurationTwo.replication.bindAddress = "10.6.1.1";
        configurationTwo.replication.bindPort = 5002;
        configurationTwo.replication.nodes.add("10.1.1.1");

        configurationOne.merge(configurationTwo);

        assertEquals("10.6.1.1", configurationOne.replication.bindAddress);
        assertEquals((long) 5002, (long) configurationOne.replication.bindPort);
        assertEquals("foo", configurationOne.replication.dataDirectory);
        assertEquals(Arrays.asList("10.1.1.2", "10.1.1.1"), configurationOne.replication.nodes);
    }

}
