package io.gatekeeper.model;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import io.gatekeeper.api.model.Certificate;
import org.json.JSONObject;

import java.util.UUID;

public class CertificateModelBuilder extends AbstractModelBuilder<Certificate, CertificateModel> {

    @Override
    public CertificateModel unserialise(String data) throws Exception {
        JSONObject object = new JSONObject(data);
        CertificateModel model = new CertificateModel();

        model.setUuid(UUID.fromString(object.getString("uuid")));
        model.setDn(object.getString("dn"));
        model.setExpires(new ISO8601DateFormat().parse(object.getString("expires")));
        model.setProvider(UUID.fromString(object.getString("provider")));
        model.setCreated(new ISO8601DateFormat().parse(object.getString("created")));
        model.setCertificate(object.getString("certificate"));
        model.setKey(object.getString("key"));
        model.setChain(object.optString("chain"));

        return model;
    }

    @Override
    public String serialise(CertificateModel model) {
        JSONObject object = new JSONObject();

        object.put("uuid", model.getUuid().toString());
        object.put("dn", model.getDn());
        object.put("expires", new ISO8601DateFormat().format(model.getExpires()));
        object.put("provider", model.getProvider().toString());
        object.put("created", new ISO8601DateFormat().format(model.getCreated()));
        object.put("certificate", model.getCertificate());
        object.put("key", model.getKey());
        object.put("chain", model.getChain());

        return object.toString();
    }

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
