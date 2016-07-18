package io.gatekeeper.node.service.provider;

import io.gatekeeper.model.ProviderModel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ManualProvider extends AbstractProvider {

    /**
     * {@inheritDoc}
     */
    public ManualProvider(Executor executor) {
        super(executor);
    }

    @Override
    public void validate(ProviderModel model) {
        // Everything is valid here!
    }

    @Override
    public CompletableFuture<Void> start() {
        logger.info("Starting manual provider " + id);

        return CompletableFuture.completedFuture(null);
    }
}
