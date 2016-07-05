package io.gatekeeper.configuration;

import io.gatekeeper.configuration.data.replication.LocalReplicationConfiguration;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DirectoryParserTest {

    @Test
    public void testValid() throws IOException, InstantiationException, IllegalAccessException {
        DirectoryParser<Configuration> parser = new DirectoryParser<>(
            Configuration.class,
            "src/test/data/configuration/valid"
        );

        Configuration configuration = parser.parse();

        assertEquals(true, configuration.replication.server);
        assertEquals("10.6.1.1", configuration.replication.rpcAddress);
        assertEquals((long) 4321, (long) configuration.replication.rpcPort);
        assertEquals(configuration.replication.getClass(), LocalReplicationConfiguration.class);
    }

}
