package io.gatekeeper.node.service.output;

import io.gatekeeper.InvalidConfigurationException;
import io.gatekeeper.api.model.Certificate;
import io.gatekeeper.api.model.Endpoint;
import io.gatekeeper.configuration.Configuration;
import io.gatekeeper.configuration.data.output.DirectoryOutput;
import io.gatekeeper.node.service.ReplicationService;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;

public class DirectoryOutputService extends AbstractPollingOutputService<DirectoryOutput> {

    protected Path path;

    public DirectoryOutputService(
        Configuration configuration,
        DirectoryOutput outputConfiguration,
        ReplicationService replication
    ) {
        super(configuration, outputConfiguration, replication);

        path = FileSystems.getDefault().getPath(outputConfiguration.path);
    }

    /**
     * Start this service.
     * Called internally ({@link #start()}).
     */
    @Override
    void startSync() {
        super.startSync();

        if (path.toFile().exists() && path.toFile().isFile()) {
            throw new InvalidConfigurationException(
                String.format("%s is not a directory", path.toString())
            );
        }

        if (!path.toFile().exists() && !path.toFile().mkdir()) {
            throw new InvalidConfigurationException(
                String.format("Could not create %s", path.toString())
            );
        }
    }

    @Override
    void poll(Map<Endpoint, Certificate> certificates) {
        Boolean changed = false;

        for (Map.Entry<Endpoint, Certificate> entry : certificates.entrySet()) {
            Endpoint endpoint = entry.getKey();
            Certificate certificate = entry.getValue();

            try {
                changed |= outputCertificate(endpoint, certificate);
            } catch (Exception exception) {
                logger.warning(
                    String.format(
                        "Failed to update certificate for endpoint %s (%s)",
                        endpoint.getUuid(),
                        exception.getClass().getCanonicalName()
                    )
                );
            }
        }

        if (changed) {
            executeAfterUpdate();
        }
    }

    /**
     * Output this certificate to the configured directory.
     *
     * @return True if we made a change
     */
    private Boolean outputCertificate(Endpoint endpoint, Certificate certificate) throws Exception {
        if (outputConfiguration.concatenate) {
            return outputConcatenatedCertificateFile(endpoint, certificate);
        } else {
            return outputCertificateFile(endpoint, certificate) | outputKeyFile(endpoint, certificate);
        }
    }

    /**
     * Output a PEM file for this certificated into the configured directory.
     *
     * @return True if we made a change
     */
    private Boolean outputConcatenatedCertificateFile(Endpoint endpoint, Certificate certificate) throws Exception {
        String data = certificate.getCertificate();

        if (certificate.getChain() != null) {
            data = data
                .concat("\n")
                .concat(certificate.getChain());
        }

        data = data.concat("\n")
            .concat(certificate.getKey());

        return putFileContents(
            getCertificatePEMPathForEndpoint(endpoint),
            data
        );
    }

    /**
     * Output this certificate's certificate file to the configured directory.
     *
     * @return True if we made a change
     */
    private Boolean outputCertificateFile(Endpoint endpoint, Certificate certificate) throws Exception {
        String data = certificate.getCertificate();

        if (certificate.getChain() != null) {
            data = data
                .concat("\n")
                .concat(certificate.getChain());
        }

        return putFileContents(
            getCertificatePathForEndpoint(endpoint),
            data
        );
    }

    /**
     * Output this certificate's key file to the configured directory.
     *
     * @return True if we made a change
     */
    private Boolean outputKeyFile(Endpoint endpoint, Certificate certificate) throws Exception {
        return putFileContents(
            getKeyPathForEndpoint(endpoint),
            certificate.getKey()
        );
    }

    /**
     * Set the given file's contents, returning true if a change was made.
     *
     * @param path     The path to the file
     * @param contents The contents of the file
     *
     * @return True if we made a change
     */
    private synchronized Boolean putFileContents(Path path, String contents) throws Exception {
        File file = path.toFile();
        String hash = new String(MessageDigest.getInstance("SHA-256").digest(contents.getBytes()));

        if (!file.exists()) {
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(contents.getBytes());

            fos.close();

            return true;
        }

        String hashExisting = new String(MessageDigest.getInstance("SHA-256").digest(readAllBytes(path)));

        if (hashExisting.equals(hash)) {
            return false;
        }

        FileOutputStream fos = new FileOutputStream(file);

        fos.write(contents.getBytes());

        fos.close();

        return true;
    }

    /**
     * Returns the path to store the given endpoints certificate at.
     *
     * @param endpoint The endpoint to store
     *
     * @return The path to store the endpoint's certificate
     */
    private Path getCertificatePathForEndpoint(Endpoint endpoint) {
        return path.resolve(
            String.format("%s.crt", endpoint.getUuid())
        );
    }

    /**
     * Returns the path to store the given endpoints key at.
     *
     * @param endpoint The endpoint to store
     *
     * @return The path to store the endpoint's key
     */
    private Path getKeyPathForEndpoint(Endpoint endpoint) {
        return path.resolve(
            String.format("%s.key", endpoint.getUuid())
        );
    }

    /**
     * Returns the path to store the given endpoints certificate in PEM format at.
     *
     * @param endpoint The endpoint to store
     *
     * @return The path to store the endpoint's certificate
     */
    private Path getCertificatePEMPathForEndpoint(Endpoint endpoint) {
        return path.resolve(
            String.format("%s.pem", endpoint.getUuid())
        );
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
