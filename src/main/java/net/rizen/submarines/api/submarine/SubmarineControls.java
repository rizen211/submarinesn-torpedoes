package net.rizen.submarines.api.submarine;

/**
 * Tracks which controls are currently being pressed by the player. This stores the state of all movement inputs
 * like forward, backward, turning, and vertical movement. The submarine movement system reads these flags to
 * determine how to move each tick.
 */
public class SubmarineControls {
    private boolean moveForward = false;
    private boolean moveBackward = false;
    private boolean rotateLeft = false;
    private boolean rotateRight = false;
    private boolean moveUp = false;
    private boolean moveDown = false;

    public void updateInput(boolean forward, boolean backward, boolean left, boolean right, boolean up, boolean down) {
        this.moveForward = forward;
        this.moveBackward = backward;
        this.rotateLeft = left;
        this.rotateRight = right;
        this.moveUp = up;
        this.moveDown = down;
    }

    public void reset() {
        this.moveForward = false;
        this.moveBackward = false;
        this.rotateLeft = false;
        this.rotateRight = false;
        this.moveUp = false;
        this.moveDown = false;
    }

    public boolean isMoving() {
        return moveForward || moveBackward || rotateLeft || rotateRight || moveUp || moveDown;
    }

    public boolean isMoveForward() {
        return moveForward;
    }

    public boolean isMoveBackward() {
        return moveBackward;
    }

    public boolean isRotateLeft() {
        return rotateLeft;
    }

    public boolean isRotateRight() {
        return rotateRight;
    }

    public boolean isMoveUp() {
        return moveUp;
    }

    public boolean isMoveDown() {
        return moveDown;
    }
}