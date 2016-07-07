package io.gatekeeper.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An endpoint represents a single SSL-secured entry point.
 * It can contain multiple DNs, but has one common provider and one certificate at a time.
 */
public class Endpoint extends AbstractModel implements Serializable {

    private List<Domain> domains = new ArrayList<>();

    public List<Domain> domains() {
        return this.domains;
    }

    public void add(Domain domain) {
        this.domains.add(domain);
    }

    public Boolean contains(Domain domain) {
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
        for (Domain domain : domains) {
            if (domain.matches(pattern)) {
                return true;
            }
        }

        return false;
    }
}
