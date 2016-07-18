package io.gatekeeper.node.service.provider;

import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.ProviderModel;
import io.gatekeeper.node.ServiceContainerAware;
import io.gatekeeper.node.service.BackendService;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.codec.binary.Base64;
import sun.security.provider.X509Factory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.time.temporal.ChronoUnit.DAYS;

public abstract class AbstractProvider extends ServiceContainerAware implements AutoCloseable {

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
     * The ID of this provider.
     */
    protected String id;

    /**
     * The UUID of this provider.
     */
    protected UUID uuid;

    /**
     * Internally stored data that is persisted by this provider.
     */
    protected Map<String, Object> data = new HashMap<>();

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
    public static KeyPair generateRSA(Integer bits) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048, SecureRandom.getInstanceStrong());

        return generator.genKeyPair();
    }

    public static String encodePrivateKey(PrivateKey key) {
        Base64 encoder = new Base64(64);

        StringBuilder string = new StringBuilder();

        string.append("-----BEGIN RSA PRIVATE KEY-----\n");

        string.append(new String(encoder.encode(key.getEncoded())));

        string.append("-----END RSA PRIVATE KEY-----");

        return string.toString();
    }

    public static String encodeCertificate(X509Certificate certificate) throws Exception {
        Base64 encoder = new Base64(64);

        StringBuilder string = new StringBuilder();

        string.append(X509Factory.BEGIN_CERT + "\n");

        string.append(new String(encoder.encode(certificate.getEncoded())));

        string.append(X509Factory.END_CERT);

        return string.toString();
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
        this.id = model.getId();
        this.uuid = model.getUuid();
        this.data = model.getData();
    }

    /**
     * Save the data inside this provider.
     */
    protected void saveData() throws Exception {
        service(BackendService.class)
            .saveProviderDataUnsafe(uuid, data)
            .get();
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
        CompletableFuture<CertificateModel> renew(EndpointModel endpoint);

    }

    /**
     * An interface for providers that provide API endpoints, such as the ACME .well-known URL.
     */
    public interface SubRoutable {

        /**
         * Handle a sub-route to this provider.
         *
         * @param context The routing context
         */
        void handle(RoutingContext context);
    }
}
