package io.gatekeeper.node.service.provider;

import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.ProviderModel;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.time.temporal.ChronoUnit.DAYS;

public abstract class AbstractProvider implements AutoCloseable {

    /**
     * A shared executor for providers to work inside.
     */
    protected Executor executor;

    /**
     * A flag to mark this provider as stopped.
     */
    protected volatile Boolean stopped = false;

    /**
     * The length of certificate to issue.
     */
    protected Duration length = Duration.of(365, DAYS);

    /**
     * The UUID of this provider.
     */
    protected UUID uuid;

    /**
     * Default public constructor.
     *
     * @param executor An executor context for this providers threads
     */
    public AbstractProvider(Executor executor) {
        this.executor = executor;
    }

    /**
     * Generate a new RSA keypair.
     *
     * @param bits The size of the key in bits
     *
     * @return The generated keypair
     *
     * @throws Exception
     */
    static KeyPair generateRSA(Integer bits) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048, SecureRandom.getInstanceStrong());

        return generator.genKeyPair();
    }

    /**
     * Validate the provider in the given model.
     *
     * @param model
     */
    public abstract void validate(ProviderModel model);

    /**
     * Configure this provider from the given model.
     *
     * @param model
     */
    public void configure(ProviderModel model) {
        this.uuid = model.getUuid();
    }

    /**
     * Boot this provider.
     *
     * @return A future that completes when this provider is started
     */
    public abstract CompletableFuture<Void> start();

    /**
     * Stop this provider.
     */
    @Override
    public void close() throws Exception {
        stopped = true;
    }

    /**
     * An interface for providers to implement if they support renewing certificates automatically.
     */
    public interface Renewable {

        /**
         * Renew the certificate (or issue if first-issue) for the given endpoint.
         *
         * @param endpoint The endpoint to issue a certificate for
         *
         * @return The issued certificate
         */
        public CompletableFuture<CertificateModel> renew(EndpointModel endpoint);

    }
}
