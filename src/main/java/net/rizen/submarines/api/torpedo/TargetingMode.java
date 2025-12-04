package net.rizen.submarines.api.torpedo;

/**
 * Controls what types of entities torpedoes will lock onto and track. This allows targeting specific
 * threat types instead of locking onto non-threatening entities.
 */
public enum TargetingMode {
    /**
     * Target anything that moves. Torpedoes will go after whatever is closest.
     */
    ALL("All"),
    /**
     * Only track and hit players. Useful for PvP situations.
     */
    PLAYERS("Players"),
    /**
     * Only target other submarines. Good for sub versus sub combat.
     */
    SUBMARINES("Submarines"),
    /**
     * Target mobs and other entities, but ignore players and submarines.
     */
    ENTITIES("Entities");

    private final String displayName;

    TargetingMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Cycles to the next targeting mode. This loops through all the available modes and wraps back to the start.
     *
     * @return the next targeting mode
     */
    public TargetingMode next() {
        int nextIndex = (this.ordinal() + 1) % values().length;
        return values()[nextIndex];
    }
}
