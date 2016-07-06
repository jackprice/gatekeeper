package io.gatekeeper;

/**
 * A helper class for working with gatekeeper version numbers.
 * <p>
 * Inspiration take from Elasticsearch.
 *
 * @link https://github.com/elastic/elasticsearch/blob/master/core/src/main/java/org/elasticsearch/Version.java
 * <p>
 * The logic for ID is: XXYYZZ where XX is major version, YY is minor version, ZZ is revision.
 */
public class Version {

    /*
     * Existing version identifiers.
     *
     * New versions are created by adding their identifier here.
     */
    public static final int V0_0_0_ID = 0;
    public static final Version V0_0_0 = new Version(V0_0_0_ID);

    public static final Version CURRENT = V0_0_0;

    public final int id;
    public final byte major;
    public final byte minor;
    public final byte revision;

    public static Version fromId(int id) {
        switch (id) {
            case V0_0_0_ID:
                return V0_0_0;
            default:
                return CURRENT;
        }
    }

    /**
     * Returns the smallest version of the two.
     */
    public static Version smallest(Version version1, Version version2) {
        assert null != version1;
        assert null != version2;

        return version1.id < version2.id ? version1 : version2;
    }

    Version(int id) {
        this.id = id;
        this.major = (byte) ((id / 1000000) % 100);
        this.minor = (byte) ((id / 10000) % 100);
        this.revision = (byte) ((id / 100) % 100);
    }

    /**
     * Returns the minimum compatible version based on the current version.
     */
    public Version minimumCompatibibleVersion() {
        // Before v1, we only support communication with the current version or above.
        if (this.major < 1) {
            return CURRENT;
        }

        return fromId(this.major * 1000000);
    }

    @Override
    public String toString() {
        return String.valueOf(major) +
                '.' +
                minor +
                '.' +
                revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;

        return id == version.id;
    }
}
