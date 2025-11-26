package net.rizen.submarines.api.submarine;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Handles how submarines move through the water. This manages acceleration, deceleration, turning, and both
 * horizontal and vertical movement.
 */
public class SubmarineMovement {
    private final float maxSpeed;
    private final float acceleration;
    private final float deceleration;
    private final float rotationSpeed;
    private final float verticalSpeedMultiplier;
    private final float backwardSpeedMultiplier;

    private float currentForwardSpeed = 0f;
    private float currentVerticalSpeed = 0f;

    public SubmarineMovement(float maxSpeed, float acceleration, float deceleration,
                             float rotationSpeed, float verticalSpeedMultiplier, float backwardSpeedMultiplier) {
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.deceleration = deceleration;
        this.rotationSpeed = rotationSpeed;
        this.verticalSpeedMultiplier = verticalSpeedMultiplier;
        this.backwardSpeedMultiplier = backwardSpeedMultiplier;
    }

    public void update(SubmarineControls controls, Entity entity, float currentMaxSpeed) {
        float targetForwardSpeed = 0f;
        if (controls.isMoveForward()) targetForwardSpeed += currentMaxSpeed;
        if (controls.isMoveBackward()) targetForwardSpeed -= currentMaxSpeed * backwardSpeedMultiplier;

        if (Math.abs(targetForwardSpeed - currentForwardSpeed) > acceleration) {
            if (currentForwardSpeed < targetForwardSpeed) {
                currentForwardSpeed += acceleration;
            } else {
                currentForwardSpeed -= acceleration;
            }
        } else {
            currentForwardSpeed = targetForwardSpeed;
        }

        if (!controls.isMoveForward() && !controls.isMoveBackward() && Math.abs(currentForwardSpeed) > 0) {
            if (currentForwardSpeed > 0) {
                currentForwardSpeed = Math.max(0, currentForwardSpeed - deceleration);
            } else {
                currentForwardSpeed = Math.min(0, currentForwardSpeed + deceleration);
            }
        }

        if (controls.isRotateLeft()) {
            entity.setYaw(entity.getYaw() - rotationSpeed);
        }
        if (controls.isRotateRight()) {
            entity.setYaw(entity.getYaw() + rotationSpeed);
        }

        float targetVerticalSpeed = 0f;
        if (controls.isMoveUp()) targetVerticalSpeed += currentMaxSpeed * verticalSpeedMultiplier;
        if (controls.isMoveDown()) targetVerticalSpeed -= currentMaxSpeed * verticalSpeedMultiplier;

        if (Math.abs(targetVerticalSpeed - currentVerticalSpeed) > acceleration) {
            if (currentVerticalSpeed < targetVerticalSpeed) {
                currentVerticalSpeed += acceleration;
            } else {
                currentVerticalSpeed -= acceleration;
            }
        } else {
            currentVerticalSpeed = targetVerticalSpeed;
        }

        if (!controls.isMoveUp() && !controls.isMoveDown() && Math.abs(currentVerticalSpeed) > 0) {
            if (currentVerticalSpeed > 0) {
                currentVerticalSpeed = Math.max(0, currentVerticalSpeed - deceleration);
            } else {
                currentVerticalSpeed = Math.min(0, currentVerticalSpeed + deceleration);
            }
        }
    }

    public Vec3d calculateMovement(float yaw) {
        float yawRad = (float) Math.toRadians(yaw);
        Vec3d forward = new Vec3d(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3d up = new Vec3d(0, 1, 0);
        return forward.multiply(currentForwardSpeed).add(up.multiply(currentVerticalSpeed));
    }

    public float getSignedSpeed() {
        float magnitude = (float) Math.sqrt(currentForwardSpeed * currentForwardSpeed + currentVerticalSpeed * currentVerticalSpeed);
        return currentForwardSpeed < 0 ? -magnitude : magnitude;
    }

    public void stop() {
        this.currentForwardSpeed = 0f;
        this.currentVerticalSpeed = 0f;
    }

    public float getCurrentForwardSpeed() {
        return currentForwardSpeed;
    }

    public float getCurrentVerticalSpeed() {
        return currentVerticalSpeed;
    }

    public void setCurrentForwardSpeed(float speed) {
        this.currentForwardSpeed = speed;
    }

    public void setCurrentVerticalSpeed(float speed) {
        this.currentVerticalSpeed = speed;
    }
}