package io.gatekeeper.node.service.replication.consul;

import io.gatekeeper.node.service.replication.common.Node;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClientTest {

    @Test
    public void testCreate() throws ExecutionException, InterruptedException {
        Client client = new Client("127.0.0.1", 8500, "foo", "127.0.0.1", 8501, null);

        client.registerService().join();

        Lock lock = client.lock("Foo").get();

        List<Node> nodes = client.getNodes().get();

        Integer foo = 1;
    }

}
