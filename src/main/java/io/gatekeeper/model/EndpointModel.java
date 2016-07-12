package io.gatekeeper.model;

import io.gatekeeper.InvalidConfigurationException;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.*;

/**
 * An endpoint represents a single SSL-secured entry point.
 * It can contain multiple DNs, but has one common provider and one certificate at a time.
 */
public class EndpointModel extends AbstractModel {

    /**
     * The domains that this endpoint encapsulates.
     */
    protected List<String> domains = new ArrayList<>();

    /**
     * A collection of arbitrary tags on this endpoint.
     */
    protected List<String> tags = new ArrayList<>();

    /**
     * The ID of the provider that generates certificates for this endpoint.
     */
    protected String provider;

    /**
     * The DN for generating certificates.
     */
    protected String dn;

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public LdapName parseNd() throws InvalidNameException {
        return new LdapName(getDn());
    }

    /**
     * Add a domain to this endpoint.
     *
     * @param domain The FQDN to add
     */
    public void addDomain(String domain) {
        this.domains.add(domain);
    }

    /**
     * Add a tag to this endpoint.
     *
     * @param tag The tag to add
     */
    public void addTag(String tag) {
        this.tags.add(tag);
    }

    /**
     * Check if this endpoint contains the given domain.
     *
     * @param domain The FQDN to check
     *
     * @return True if this endpoint exactly contains the given domain
     */
    public Boolean containsDomain(String domain) {
        return this.domains.contains(domain);
    }

    /**
     * Check if this endpoint has the given tag.
     *
     * @param tag The tag to check
     *
     * @return True if this endpoing is tagged with the given tag
     */
    public Boolean containsTag(String tag) {
        for (String tagged : tags) {
            if (tagged.equals(tag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if this endpoint contains a domain that matches the given glob-like pattern.
     *
     * @param pattern The pattern to match, e.g. `*.example.com`
     *
     * @return True if the pattern matches
     */
    public Boolean matches(String pattern) {
        for (String domain : domains) {
            if (new Domain(domain).matches(pattern)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void validate() throws InvalidConfigurationException {
        new DNValidator().validate(dn);
    }

    /**
     * A helper class for manipulating domain names.
     */
    private class Domain {

        private String domain;

        /**
         * @param domain The FQDN that this domain represents
         */
        Domain(String domain) {
            this.domain = domain;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return domain;
        }

        /**
         * Returns true if this domain matches the given glob-like pattern.
         *
         * @param pattern The pattern to match, such as `*.example.com`
         *
         * @return True if the domain matches the pattern
         */
        Boolean matches(String pattern) {
            String[] domainParts = reverseAndSplitDomain(domain);
            String[] patternParts = reverseAndSplitDomain(pattern);

            if (patternParts.length > domainParts.length) {
                return false;
            }

            for (int i = 0; i < domainParts.length && i < patternParts.length; i++) {
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
        private String[] reverseAndSplitDomain(String domain) {
            String[] parts = domain.split("\\.");
            String[] reversed = new String[parts.length];

            List<String> partsList = Arrays.asList(parts);

            Collections.reverse(partsList);

            return partsList.toArray(reversed);
        }
    }

    /**
     * A helper class for validating DNs.
     */
    private class DNValidator {

        List<ValidationRule> rules = new ArrayList<>();

        DNValidator() {
            rules.add(new ValidationRule("C", true));
            rules.add(new ValidationRule("ST", true));
            rules.add(new ValidationRule("L", true));
            rules.add(new ValidationRule("O", true));
            rules.add(new ValidationRule("OU", true));
        }

        void validate(String dn) throws InvalidConfigurationException {
            try {
                validate(new LdapName(dn));
            } catch (InvalidNameException exception) {
                throw new InvalidConfigurationException(
                    String.format("Invalid DN: %s", exception.getExplanation()),
                    exception
                );
            }
        }

        private void validate(LdapName dn) throws InvalidConfigurationException {
            Map<String, String> rdns = new HashMap<>();

            for (Rdn rdn : dn.getRdns()) {
                ValidationRule rule = getRule(rdn.getType());

                if (rule == null) {
                    throw new InvalidConfigurationException(
                        String.format("Unknown RDN %s for %s", rdn.getType(), rdn.toString())
                    );
                }

                if (rdns.containsKey(rule.type)) {
                    throw new InvalidConfigurationException(
                        String.format("Duplicate RDN %s for %s", rdn.getType(), rdn.toString())
                    );
                }

                rdns.put(rule.type, rdn.getValue().toString());
            }

            for (ValidationRule rule : rules) {
                if (rule.required && !rdns.containsKey(rule.type)) {
                    throw new InvalidConfigurationException(
                        String.format("Required RDN %s missing", rule.type)
                    );
                }
            }
        }

        private ValidationRule getRule(String rdn) {
            for (ValidationRule rule : rules) {
                if (rule.type.equals(rdn)) {
                    return rule;
                }
            }

            return null;
        }

        private class ValidationRule {

            String type;

            Boolean required = false;

            public ValidationRule(String type, Boolean required) {
                this.type = type;
                this.required = required;
            }
        }


    }
}
