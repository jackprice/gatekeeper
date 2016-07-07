package io.gatekeeper.model;

import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A domain is a single domain name contained within an endpoint.
 */
public class Domain extends AbstractModel implements Serializable {

    private String domain;

    public Domain(String domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return domain;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!obj.getClass().equals(Domain.class)) {
            return false;
        }

        return toString().equals(obj.toString());
    }

    /**
     * Returns true if this domain matches the given glob-like pattern.
     *
     * @param pattern The pattern to match, such as `*.example.com`
     *
     * @return True if the domain matches the pattern
     */
    public Boolean matches(String pattern) {
        String[] domainParts = reverseAndSplitDomain(domain);
        String[] patternParts = reverseAndSplitDomain(pattern);

        if (patternParts.length > domainParts.length) {
            return false;
        }

        for (int i = 0; i < domainParts.length && i < patternParts.length; i ++) {
            String domainCompare = domainParts[i];
            String patternCompare = patternParts[i];

            if (domainCompare.equals(patternCompare)) {
                continue;
            }

            if (patternCompare.equals("*")) {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     * Taking a domain, split it on its components (the period) and return in reverse order.
     *
     * @param domain The domain OR pattern to split
     */
    private static String[] reverseAndSplitDomain(String domain) {
        String[] parts = domain.split("\\.");
        String[] reversed = new String[parts.length];

        List<String> partsList = Arrays.asList(parts);

        Collections.reverse(partsList);

        return partsList.toArray(reversed);
    }
}
