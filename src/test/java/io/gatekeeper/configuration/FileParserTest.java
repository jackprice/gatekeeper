package io.gatekeeper.configuration;

import io.gatekeeper.configuration.annotation.Config;
import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FileParserTest {

    @Test
    public void testValidOne() throws IOException, IllegalAccessException, InstantiationException {
        FileParser<Configuration> parser = new FileParser<>(
            Configuration.class,
            "src/test/data/configuration/valid/one.yml"
        );

        Configuration configuration = parser.parse();

        assertEquals(true, configuration.replication.server);
        assertEquals("127.0.0.1", configuration.replication.rpcAddress);
        assertEquals((long) 1234, (long) configuration.replication.rpcPort);
        assertEquals(configuration.replication.getClass(), LocalReplicationConfiguration.class);
    }

    @Test
    public void testValidTwo() throws IOException, IllegalAccessException, InstantiationException {
        FileParser<Configuration> parser = new FileParser<>(
            Configuration.class,
            "src/test/data/configuration/valid/two.yml"
        );

        Configuration configuration = parser.parse();

        assertEquals(true, configuration.replication.server);
        assertEquals("10.6.1.1", configuration.replication.rpcAddress);
        assertEquals((long) 4321, (long) configuration.replication.rpcPort);
        assertEquals(configuration.replication.getClass(), LocalReplicationConfiguration.class);
    }
}
