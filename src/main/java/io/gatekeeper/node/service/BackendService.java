package io.gatekeeper.node.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.BackendConfiguration;
import io.gatekeeper.logging.Loggers;
import io.gatekeeper.node.service.backend.common.crypto.EncryptionProvider;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class BackendService<BackendConfigurationType extends BackendConfiguration> implements Service {

    private final static String LOCK_GLOBAL = ".global";

    protected final BackendConfigurationType backendConfiguration;

    protected final Configuration configuration;

    protected final EncryptionProvider encryption;

    protected final Logger logger;

    protected final ThreadPoolExecutor executor;

    protected final ReplicationService replication;

    public BackendService(Configuration configuration, ReplicationService replication) throws Exception {
        assert null != configuration;
        assert null != replication;

        this.configuration = configuration;
        this.backendConfiguration = (BackendConfigurationType) configuration.backend;
        this.replication = replication;
        this.logger = Loggers.getBackendLogger();
        this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(
            (new ThreadFactoryBuilder())
                .setNameFormat("Backend Service %d")
                .build()
        );

        this.executor.prestartCoreThread();
        this.executor.prestartAllCoreThreads();

        try {
            this.encryption = new EncryptionProvider(configuration.backend.key);
        } catch (Exception exception) {
            throw new Exception("Could not start encryption provider");
        }
    }

    @Override
    public void close() throws IOException {
        this.logger.info(String.format("Shutting down %d backend threads", this.executor.getActiveCount()));

        this.executor.shutdown();

        try {
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            this.logger.warning(String.format(
                "Killing %d backend threads that did not shut down in time",
                this.executor.getActiveCount()
            ));
        }

        this.logger.info("backend threads halted");
    }

    public void obtainGlobalLock() throws InterruptedException {
        replication.lock(LOCK_GLOBAL);
    }

    public void releaseGlobalLock() throws InterruptedException {
        replication.unlock(LOCK_GLOBAL);
    }
}
