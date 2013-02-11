package de.sosd.mediaserver.domain.ws;

public enum DLNA_NAMESPACES {

    CONTENT_DIRECTORY("urn:schemas-upnp-org:service:ContentDirectory:1", "u"),
    DUBLIN_CORE("http://purl.org/dc/elements/1.1/", "dc"),
    DIDL_LITE("urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/", ""),
    MICROSOFT_DATATYPES("urn:schemas-microsoft-com:datatypes", "dt"),
    UPNP_METADATA("urn:schemas-upnp-org:metadata-1-0/upnp/", "upnp"),
    SOAP_ENVELOPE("http://schemas.xmlsoap.org/soap/envelope/", "s");

    private final String value;
    private final String prefix;
    private final int    hash;

    DLNA_NAMESPACES(final String v, final String p) {
        this.value = v;
        this.prefix = p;
        this.hash = value().hashCode();
    }

    public String value() {
        return this.value;
    }

    public String prefix() {
        return this.prefix;
    }

    /**
     * @param v
     * @return enum
     * @throws IllegalArgumentException
     *             if v is no valid representation of DLNA_NAMESPACES
     */
    public static DLNA_NAMESPACES fromValue(final String v) {
        if (v != null) {
            final int hc = v.hashCode();
            for (final DLNA_NAMESPACES c : DLNA_NAMESPACES.values()) {
                if (c.hash == hc) {
                    return c;
                }
            }
        }
        throw new IllegalArgumentException(v);
    }
}
