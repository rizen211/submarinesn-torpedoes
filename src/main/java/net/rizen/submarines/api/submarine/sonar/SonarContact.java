package net.rizen.submarines.api.submarine.sonar;

import net.minecraft.util.math.Vec3d;

/**
 * Represents a single detection on the sonar display. Each contact stores information about what was detected,
 * where it is relative to the submarine, and when it was found. Contacts start hidden and only show up when
 * the sweep line passes over them.
 */
public class SonarContact {
    private final Vec3d relativePosition;
    private final ContactType type;
    private final double distance;
    private final float angle;
    private final long detectionTime;
    private boolean revealed;
    private long revealTime;

    public SonarContact(Vec3d relativePosition, ContactType type, double distance, float angle, long detectionTime) {
        this.relativePosition = relativePosition;
        this.type = type;
        this.distance = distance;
        this.angle = angle;
        this.detectionTime = detectionTime;
        this.revealed = false;
        this.revealTime = 0;
    }

    public ContactType getType() {
        return type;
    }

    public double getDistance() {
        return distance;
    }

    public float getAngle() {
        return angle;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void reveal(long currentTime) {
        this.revealed = true;
        this.revealTime = currentTime;
    }

    public boolean shouldRemove(long currentTime) {
        if (!revealed) {
            return currentTime - detectionTime > 10000;
        }
        return currentTime - revealTime > 2000;
    }

    public float getFadeAlpha(long currentTime) {
        if (!revealed) {
            return 0.0f;
        }
        long timeSinceReveal = currentTime - revealTime;
        if (timeSinceReveal > 2000) {
            return 0.0f;
        }
        return 1.0f - (timeSinceReveal / 2000.0f);
    }
}
