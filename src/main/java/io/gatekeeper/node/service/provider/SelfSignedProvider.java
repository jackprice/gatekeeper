package io.gatekeeper.node.service.provider;

import io.gatekeeper.model.CertificateModel;
import io.gatekeeper.model.EndpointModel;
import io.gatekeeper.model.ProviderModel;
import org.apache.commons.codec.binary.Base64;
import sun.security.provider.X509Factory;
import sun.security.x509.*;

import javax.naming.ldap.Rdn;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SelfSignedProvider extends AbstractProvider implements AbstractProvider.Renewable {

    /**
     * {@inheritDoc}
     */
    public SelfSignedProvider(Executor executor) {
        super(executor);
    }

    @Override
    public void validate(ProviderModel model) {

    }

    @Override
    public void configure(ProviderModel model) {
        super.configure(model);
    }

    @Override
    public CompletableFuture<Void> start() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<CertificateModel> renew(EndpointModel endpoint) {
        CompletableFuture<CertificateModel> future = new CompletableFuture<>();

        executor.execute(new RenewRunnable(future, endpoint, length));

        future.thenRunAsync(() -> {

        }, executor);

        return future;
    }

    /**
     * A helper class for encapsulating the renew command
     */
    private class RenewRunnable implements Runnable {

        CompletableFuture<CertificateModel> future;

        EndpointModel endpoint;

        Duration length;

        RenewRunnable(CompletableFuture<CertificateModel> future, EndpointModel endpoint, Duration length) {
            this.future = future;
            this.endpoint = endpoint;
            this.length = length;
        }

        @Override
        public void run() {
            try {
                future.complete(runInternal());
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        }

        CertificateModel runInternal() throws Exception {
            KeyPair key = AbstractProvider.generateRSA(512);

            String domain = endpoint.getDomains().get(0);
            String dn = endpoint.parseNd().add(new Rdn("CN", domain)).toString();
            Date from = new Date();
            Date to = new Date(from.getTime() + length.toMillis());
            BigInteger serial = new BigInteger(64, new SecureRandom());

            CertificateValidity validity = new CertificateValidity(from, to);

            X500Name owner = new X500Name(dn);

            X509CertInfo info = new X509CertInfo();

            info.set(X509CertInfo.VALIDITY, validity);
            info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(serial));
            info.set(X509CertInfo.SUBJECT, owner);
            info.set(X509CertInfo.ISSUER, owner);
            info.set(X509CertInfo.KEY, new CertificateX509Key(key.getPublic()));
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            info.set(
                X509CertInfo.ALGORITHM_ID,
                new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid))
            );

            CertificateExtensions extensions = new CertificateExtensions();

            GeneralNames names = new GeneralNames();

            for (String endpointDomain : endpoint.getDomains()) {
                names.add(new GeneralName(new DNSName(endpointDomain)));
            }

            SubjectAlternativeNameExtension san = new SubjectAlternativeNameExtension(names);

            extensions.set(san.getName(), san);

            info.set(
                X509CertInfo.EXTENSIONS,
                extensions
            );

            X509CertImpl cert = new X509CertImpl(info);

            cert.sign(key.getPrivate(), "SHA1withRSA");

            info.set(
                CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM,
                cert.get(X509CertImpl.SIG_ALG)
            );
            cert = new X509CertImpl(info);
            cert.sign(key.getPrivate(), "SHA1withRSA");

            CertificateModel model = new CertificateModel();

            model.setProvider(uuid);
            model.setCreated(from);
            model.setDn(dn);
            model.setKey(encodePrivateKey(key.getPrivate()));
            model.setCertificate(encodeCertificate(cert));
            model.setExpires(to);

            return model;
        }

        String encodePrivateKey(PrivateKey key) {
            Base64 encoder = new Base64(64);

            StringBuilder string = new StringBuilder();

            string.append("-----BEGIN RSA PRIVATE KEY-----\n");

            string.append(new String(encoder.encode(key.getEncoded())));

            string.append("-----END RSA PRIVATE KEY-----");

            return string.toString();
        }

        String encodeCertificate(X509Certificate certificate) throws Exception {
            Base64 encoder = new Base64(64);

            StringBuilder string = new StringBuilder();

            string.append(X509Factory.BEGIN_CERT + "\n");

            string.append(new String(encoder.encode(certificate.getEncoded())));

            string.append(X509Factory.END_CERT);

            return string.toString();
        }
    }
}
