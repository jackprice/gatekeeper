package io.gatekeeper.model;

import io.gatekeeper.api.model.Endpoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An endpoint represents a single SSL-secured entry point.
 * It can contain multiple DNs, but has one common provider and one certificate at a time.
 */
public class EndpointModel extends AbstractModel<Endpoint> implements Serializable {

    private List<DomainModel> domains = new ArrayList<>();

    public List<DomainModel> domains() {
        return this.domains;
    }

    public void add(DomainModel domain) {
        this.domains.add(domain);
    }

    public Boolean contains(DomainModel domain) {
        return this.domains.contains(domain);
    }

    /**
     * Returns true if this endpoint contains a domain that matches the given glob-like pattern.
     *
     * @param pattern The pattern to match, e.g. `*.example.com`
     *
     * @return True if the pattern matches
     */
    public Boolean matches(String pattern) {
        for (DomainModel domain : domains) {
            if (domain.matches(pattern)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Endpoint toApiModel() {
        io.gatekeeper.api.model.Endpoint endpoint = new io.gatekeeper.api.model.Endpoint();

        endpoint.id(id().toString());
        endpoint.domains(domains
            .stream()
            .map(((DomainModel::toApiModel)))
            .collect(Collectors.toList()));

        return endpoint;
    }

    @Override
    public void fromApiModel(Endpoint endpoint) {
        this.domains = endpoint
            .getDomains()
            .stream()
            .map((domain) -> new DomainModel(domain.getDomain()))
            .collect(Collectors.toList());
    }
}
