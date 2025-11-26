package net.rizen.submarines.api.submarine;

/**
 * The three speed settings available to submarines. Each mode is a tradeoff between speed, stealth, and power usage.
 *
 * Silent mode moves slowly but uses minimal power and makes less noise. Cruise has balanced performance.
 * Flank speed is for when you need maximum speed and do not care about being heard
 * or burning through your power reserves.
 */
public enum MovementMode {
    /**
     * Slow and quiet.
     */
    SILENT(0.15f, 1.0f),    // 3 b/s (0.15 blocks/tick), 1x power consumption
    /**
     * Balanced operating speed.
     */
    CRUISE(0.3f, 1.5f),     // 6 b/s (0.3 blocks/tick), 1.5x power consumption
    /**
     * Maximum speed, burns power fast and makes a lot of noise.
     */
    FLANK(0.45f, 2.5f);     // 9 b/s (0.45 blocks/tick), 2.5x power consumption

    private final float maxSpeed;
    private final float powerMultiplier;

    MovementMode(float maxSpeed, float powerMultiplier) {
        this.maxSpeed = maxSpeed;
        this.powerMultiplier = powerMultiplier;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getPowerMultiplier() {
        return powerMultiplier;
    }

    /**
     * Gets the next mode in the cycle. This loops from silent to cruise to flank and back to silent.
     *
     * @return the next movement mode
     */
    public MovementMode next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }

    public String getDisplayName() {
        return switch (this) {
            case SILENT -> "Silent";
            case CRUISE -> "Cruise";
            case FLANK -> "Flank";
        };
    }
}
