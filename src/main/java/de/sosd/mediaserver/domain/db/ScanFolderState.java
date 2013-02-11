package de.sosd.mediaserver.domain.db;

public enum ScanFolderState {

    IDLE("IDLE"),
    SCANNING("SCANNING"),
    NOT_FOUND("NOT_FOUND");

    private final String value;

    ScanFolderState(final String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static ScanFolderState fromValue(final String v) {
        for (final ScanFolderState c : ScanFolderState.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
