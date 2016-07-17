package io.gatekeeper.node.service.provider.acme;

import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.node.service.provider.AbstractProvider;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.util.CSRBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * This runnable is a wrapper around the command to renew a certificate.
 */
public class RenewRunnable implements Runnable {

    /**
     * The configuration for the ACME provider that should renew this certificate.
     */
    private Configuration configuration;

    /**
     * The endpoint we are renewing.
     */
    private EndpointModel endpoint;

    /**
     * A future to be completed when the certificate has been renewed.
     */
    private CompletableFuture<CertificateModel> future;

    /**
     * Public constructor.
     *
     * @param configuration The ACME provider configuration
     * @param endpoint      The endpoint to renew
     * @param future        A future to be completed when the certificate has been renewed
     */
    public RenewRunnable(
        Configuration configuration,
        EndpointModel endpoint,
        CompletableFuture<CertificateModel> future
    ) {
        this.configuration = configuration;
        this.endpoint = endpoint;
        this.future = future;
    }

    /**
     * Perform the actual renewal.
     *
     * This is a wrapper around the actual logic - {@link #runInternal()}
     */
    @Override
    public void run() {
        try {
            future.complete(runInternal());
        } catch (Throwable exception) {
            future.completeExceptionally(exception);
        }
    }

    /**
     * Perform the renewal.
     *
     * @return The renewed certificate
     */
    private CertificateModel runInternal() throws Exception {
        AcmeClient client = client();
        Registration registration = registration();

        client.newRegistration(registration);

        registration.setAgreement(registration.getAgreement());

        client.modifyRegistration(registration);

        for (String domain : endpoint.getDomains()) {
            doAuthorization(client, registration, domain);
        }

        CSRBuilder builder = new CSRBuilder();

        builder.addDomains(endpoint.getDomains());
        builder.sign(registration.getKeyPair());

        byte[] csr = builder.getEncoded();

        URI certUri = client.requestCertificate(registration, csr);
        X509Certificate cert = client.downloadCertificate(certUri);

        CertificateModel model = new CertificateModel();

        model.setKey(AbstractProvider.encodePrivateKey(registration.getKeyPair().getPrivate()));
        model.setCreated(new Date());
        model.setExpires(cert.getNotAfter());
        model.setCertificate(AbstractProvider.encodeCertificate(cert));

        return model;
    }

    /**
     * Perform the authorization for the given domain.
     *
     * @param client       The ACME client to use
     * @param registration The current registration object
     * @param domain       The domain to authorize
     */
    private void doAuthorization(AcmeClient client, Registration registration, String domain) throws Exception {
        Authorization authorization = new Authorization();

        authorization.setDomain(domain);

        client.newAuthorization(registration, authorization);

        Collection<Challenge> challenges = authorization.findCombination(Http01Challenge.TYPE);

        if (challenges.size() != 1) {
            throw new Exception("Invalid challenges returned from ACME server");
        }

        Http01Challenge challenge = authorization.findChallenge(Http01Challenge.TYPE);

        assert null != challenge;

        challenge.authorize(registration);

        String token = challenge.getToken();
        String content = challenge.getAuthorization();

        // TODO: Store this!

        client.triggerChallenge(registration, challenge);

        for (int i = 0; i < 3; i++) {
            Thread.sleep(3000);

            client.updateChallenge(challenge);

            if (challenge.getStatus() == Status.VALID) {
                return;
            }
        }

        throw new Exception("Challenge was not validated by the server in time");
    }

    /**
     * Get a registration object from the endpoint.
     *
     * @return The completed registration object
     */
    private Registration registration() throws Exception {
        KeyPair key = AbstractProvider.generateRSA(2048);

        return new Registration(key);
    }

    /**
     * Get an ACME client for this provider.
     *
     * @return A completed ACME client
     */
    private AcmeClient client() throws URISyntaxException {
        if (configuration.directory.toURI().getHost().equals("acme-staging.api.letsencrypt.org")) {
            return AcmeClientFactory.connect("acme://letsencrypt.org/staging");
        } else if (configuration.directory.toURI().getHost().equals("acme-v01.api.letsencrypt.org")) {
            return AcmeClientFactory.connect("acme://letsencrypt.org");
        } else {
            return AcmeClientFactory.connect(configuration.directory.toURI());
        }
    }
}
