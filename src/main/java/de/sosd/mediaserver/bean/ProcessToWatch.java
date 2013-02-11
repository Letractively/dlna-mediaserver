package de.sosd.mediaserver.bean;

import java.util.Arrays;

import de.sosd.mediaserver.process.ProcessKilledNotifier;

public class ProcessToWatch {

    private String                pid;

    private String[]              identifiers;

    private String                fullCommand;

    private long                  creationDate;

    private int                   maxRuntimeInSeconds;

    private String                id;

    private boolean               survivor;

    private ProcessKilledNotifier notifier;

    public ProcessToWatch(final String id, final String[] identifiers,
            final int maxRuntimeInSeconds, final ProcessKilledNotifier notifier) {
        this.id = id;
        this.creationDate = System.currentTimeMillis();
        this.identifiers = identifiers;
        this.maxRuntimeInSeconds = maxRuntimeInSeconds;
        this.survivor = false;
        this.notifier = notifier;
    }

    public ProcessToWatch(final String pid, final String fullCommand) {

        this.pid = pid;
        this.fullCommand = fullCommand;

    }

    public void processKilled() {
        this.notifier.notifyProcessKilled();
    }

    public void setNotifier(final ProcessKilledNotifier notifier) {
        this.notifier = notifier;
    }

    public String getId() {
        return this.id;
    }

    public boolean hasPid() {
        return getPid() != null;
    }

    public String getFullCommand() {
        return this.fullCommand;
    }

    public String getPid() {
        return this.pid;
    }

    public boolean fits(final String fc) {

        if (this.identifiers == null || this.identifiers.length == 0) {
            return false;
        }

        for (final String identifier : this.identifiers) {
            if (!fc.contains(identifier)) {
                return false;
            }
        }

        return true;
    }

    public void setPid(final String pid) {
        this.pid = pid;
    }

    public boolean isSurvivor(final long timestamp) {
        return timestamp > this.creationDate + this.maxRuntimeInSeconds * 1000
                || this.survivor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (hasPid()) {
            return "ProcessToWatch [pid=" + this.pid + ", fullCommand="
                    + this.fullCommand + ", creationDate=" + this.creationDate
                    + ", maxRuntimeInSeconds=" + this.maxRuntimeInSeconds
                    + ", survivor=" + this.survivor + "]";
        } else {
            return "ProcessToWatch [identifiers="
                    + Arrays.toString(this.identifiers) + ", creationDate="
                    + this.creationDate + ", maxRuntimeInSeconds="
                    + this.maxRuntimeInSeconds + ", survivor=" + this.survivor
                    + "]";
        }
    }

    public void setFullCommand(final String fullCommand) {
        this.fullCommand = fullCommand;

    }

    public void markAsSurvivor() {
        this.survivor = true;

    }

    /**
     * @return the identifiers
     */
    public String[] getIdentifiers() {
        return this.identifiers;
    }

    /**
     * @param identifiers
     *            the identifiers to set
     */
    public void setIdentifiers(final String[] identifiers) {
        this.identifiers = identifiers;
    }

    /**
     * @return the creationDate
     */
    public long getCreationDate() {
        return this.creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the maxRuntimeInSeconds
     */
    public int getMaxRuntimeInSeconds() {
        return this.maxRuntimeInSeconds;
    }

    /**
     * @param maxRuntimeInSeconds
     *            the maxRuntimeInSeconds to set
     */
    public void setMaxRuntimeInSeconds(final int maxRuntimeInSeconds) {
        this.maxRuntimeInSeconds = maxRuntimeInSeconds;
    }

    /**
     * @return the survivor
     */
    public boolean isSurvivor() {
        return this.survivor;
    }

    /**
     * @param survivor
     *            the survivor to set
     */
    public void setSurvivor(final boolean survivor) {
        this.survivor = survivor;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    public ProcessKilledNotifier getNotifier() {
        return this.notifier;
    }

}
