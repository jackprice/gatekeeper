package io.gatekeeper.node.service.output;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.api.model.DefaultApi;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.OutputConfiguration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.node.service.ReplicationService;
import io.gatekeeper.node.service.Service;
import io.gatekeeper.node.service.replication.common.Node;
import io.swagger.client.ApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class AbstractOutputService<OutputConfigurationType extends OutputConfiguration> implements Service {

    protected final Configuration configuration;

    protected final OutputConfigurationType outputConfiguration;

    protected final ReplicationService replication;

    protected final Logger logger;

    protected final ExecutorService executor;

    protected final ScheduledExecutorService scheduler;

    protected List<Node> nodes;

    public AbstractOutputService(
        Configuration configuration,
        OutputConfigurationType outputConfiguration,
        ReplicationService replication
    ) {
        this.configuration = configuration;
        this.outputConfiguration = outputConfiguration;
        this.replication = replication;
        this.logger = Loggers.getOutputLogger();
        this.executor = Executors.newSingleThreadExecutor(
            (new ThreadFactoryBuilder())
                .setNameFormat("Output Service " + this.getClass().getSimpleName() + " %d")
                .build()
        );
        this.nodes = new ArrayList<>();

        this.scheduler = Executors.newSingleThreadScheduledExecutor(
            (new ThreadFactoryBuilder())
                .setNameFormat("Output Service Poller " + this.getClass().getSimpleName() + " %d")
                .build()
        );
    }

    @Override
    public void close() throws Exception {
        this.logger.info(String.format("Shutting down output service %s", this.getClass().getCanonicalName()));

        this.executor.shutdown();
        this.scheduler.shutdown();

        try {
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
            this.scheduler.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            this.logger.warning("Output service did not shut down in time");
        }

        this.logger.info(String.format("Output service %s shutdown complete", this.getClass().getCanonicalName()));
    }

    /**
     * Fetch nodes synchronously from the replication service, failing silently if we can't get any.
     */
    @SuppressWarnings("unchecked")
    synchronized void fetchNodesSilently() {
        try {
            nodes = (List<Node>) replication.fetchNodes()
                .get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            logger.info("Failed to update nodes");
        }
    }

    /**
     * Create an API client from the given node.
     *
     * @param node The remote node
     *
     * @return A configured API client
     */
    private ApiClient createApiClientFromNode(Node node) {
        ApiClient client = io.swagger.client.Configuration.getDefaultApiClient();

        client.setBasePath(
            String.format("http://%s:%d/api", node.host, node.port)
        );

        return client;
    }

    /**
     * Create an API client from any available nodes.
     *
     * @return A configured API client
     */
    private synchronized ApiClient getNextClient() {
        fetchNodesSilently();

        if (nodes.size() == 0) {
            return null;
        }

        return createApiClientFromNode(nodes.get(new Random().nextInt(nodes.size())));
    }

    /**
     * Create an API implementation from any available nodes.
     *
     * @return A configured API implementation
     */
    protected synchronized DefaultApi getApi() {
        ApiClient client = getNextClient();

        if (client == null) {
            return null;
        }

        return new DefaultApi(client);
    }

    /**
     * Execute the after_update task of this output.
     *
     * This will not wait for termination!
     */
    protected void executeAfterUpdate() {
        if (outputConfiguration.afterUpdate == null) {
            return;
        }

        logger.info("Running after_update");

        try {
            Runtime.getRuntime().exec(outputConfiguration.afterUpdate);
        } catch (IOException e) {
            logger.info("after_update task failed");
        }
    }
}
