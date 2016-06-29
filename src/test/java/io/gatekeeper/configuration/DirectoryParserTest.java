package io.gatekeeper.configuration;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DirectoryParserTest {

    @Test
    public void testValid() throws IOException {
        DirectoryParser parser = new DirectoryParser("src/test/data/configuration/valid");

        Configuration configuration = parser.parse();

        assertEquals("127.0.0.1", configuration.replication.bindAddress);
        assertEquals((long) 5001, (long) configuration.replication.bindPort);
        assertEquals("/etc/gatekeeper", configuration.replication.dataDirectory);
        assertEquals(Arrays.asList("10.1.1.1:1000", "10.1.1.2:1000"), configuration.replication.nodes);
    }

}
