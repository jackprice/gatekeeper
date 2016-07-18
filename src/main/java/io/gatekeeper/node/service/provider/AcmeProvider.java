package io.gatekeeper.node.service.provider;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.node.service.BackendService;
import io.gatekeeper.node.service.backend.common.ReplicatedMap;
import io.gatekeeper.node.service.provider.acme.Configuration;
import io.gatekeeper.node.service.provider.acme.RenewRunnable;
import io.gatekeeper.util.UUIDs;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AcmeProvider extends AbstractProvider implements AbstractProvider.Renewable, AbstractProvider.SubRoutable {

    /**
     * ACME-specific configuration for this provider.
     */
    private Configuration configuration;

    /**
     * Somewhere to store valid challenges.
     */
    private ReplicatedMap<Challenge> challenges;

    /**
     * {@inheritDoc}
     */
    public AcmeProvider(Executor executor) {
        super(executor);
    }

    @Override
    public void validate(ProviderModel model) {
        //
    }

    @Override
    public void configure(ProviderModel model) {
        super.configure(model);

        if (!Map.class.isAssignableFrom(model.getConfiguration().getClass())) {
            throw new InvalidConfigurationException("Could not read configuration");
        }

        Map data = (Map) model.getConfiguration();
        URL directory;

        if (!data.containsKey("url")) {
            throw new InvalidConfigurationException("No ACME directory URL provided");
        }

        try {
            directory = new URL(data.get("url").toString());
        } catch (MalformedURLException e) {
            throw new InvalidConfigurationException("Invalid ACME directory URL");
        }

        configuration = new Configuration(directory);
    }

    @Override
    public CompletableFuture<Void> start() {
        logger.info("Starting ACME provider " + id);

        CompletableFuture<Void> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                startInternal();

                future.complete(null);
            } catch (Throwable exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    /**
     * Perform the actual startup routine.
     */
    @SuppressWarnings("unchecked")
    private void startInternal() throws Exception {
        challenges = (ReplicatedMap<Challenge>) service(BackendService.class)
            .getReplicatedMap(UUIDs.mutatePredictably(uuid, "challenges"));
    }

    @Override
    public CompletableFuture<CertificateModel> renew(EndpointModel endpoint) {
        CompletableFuture<CertificateModel> future = new CompletableFuture<>();

        executor.execute(new RenewRunnable(configuration, endpoint, future, challenges));

        return future;
    }

    @Override
    public void handle(RoutingContext context) {
        if (context.request().method().equals(HttpMethod.GET)) {
            String[] parts = context.request().uri().split("/");
            String key = parts[parts.length - 1];

            try {
                Http01Challenge challenge = (Http01Challenge) challenges.get(key);

                context.response()
                    .putHeader("Content-Type", "text/plain")
                    .setStatusCode(200)
                    .end(challenge.getAuthorization());

            } catch (Throwable exception) {
                // NOP - Log?
            }

            try {
                ProviderModel model = (ProviderModel) service(BackendService.class)
                    .fetchProviderUnsafe(id)
                    .get();

                Map<String, Object> data = model.getData();

                if (data.containsKey(key)) {
                    context.response()
                        .putHeader("Content-Type", "text/plain")
                        .setStatusCode(200)
                        .end(data.get(key).toString());
                }
            } catch (Throwable exception) {
                // NOP - Log?
            }
        }
    }
}
