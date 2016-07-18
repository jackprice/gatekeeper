package io.gatekeeper.model;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import io.gatekeeper.api.model.Certificate;
import org.json.JSONObject;

import java.util.UUID;

public class CertificateModelBuilder extends AbstractModelBuilder<Certificate, CertificateModel> {

    @Override
    public Certificate toApiModel(CertificateModel model) {
        Certificate certificate = new Certificate();

        certificate.setUuid(model.getUuid().toString());
        certificate.setDn(model.getDn());
        certificate.setExpires(model.getExpires());
        certificate.setProvider(model.getProvider().toString());
        certificate.setCreated(model.getCreated());
        certificate.setCertificate(model.getCertificate());
        certificate.setKey(model.getKey());
        certificate.setChain(model.getChain());

        return certificate;
    }

    @Override
    public CertificateModel fromApiModel(Certificate certificate) {
        CertificateModel model = new CertificateModel();

        model.setUuid(UUID.fromString(certificate.getUuid()));
        model.setDn(certificate.getDn());
        model.setExpires(certificate.getExpires());
        model.setProvider(UUID.fromString(certificate.getProvider()));
        model.setCreated(certificate.getCreated());
        model.setCertificate(certificate.getCertificate());
        model.setKey(certificate.getKey());
        model.setChain(certificate.getChain());

        return model;
    }
}
