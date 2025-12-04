package net.rizen.submarines.api.torpedo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.rizen.submarines.api.submarine.BaseSubmarine;

import java.util.List;

/**
 * Handles torpedo movement, homing behavior, and target acquisition. This system makes torpedoes track and chase
 * targets using a cone based detection system. Torpedoes can only lock onto targets within their forward detection
 * cone and will smoothly turn to follow them.
 *
 * The homing system uses vector math to calculate how much the torpedo needs to turn each tick to close in on its
 * target without exceeding the maximum turn rate.
 */
public class TorpedoPhysics {
    private final float maxSpeed;
    private final int maxRange;

    private static final float DETECTION_RANGE = 30.0f;
    private static final float DETECTION_ANGLE = 90.0f;
    private static final float MAX_TURN_RATE = 5.0f;
    private static final float CONSTANT_SPEED = 1.0f;

    private Vec3d startPos;
    private double totalDistanceTraveled = 0.0;

    public TorpedoPhysics(float maxSpeed, int maxRange) {
        this.maxSpeed = maxSpeed;
        this.maxRange = maxRange;
    }

    public void setStartPosition(Vec3d pos) {
        this.startPos = pos;
    }

    public Vec3d calculateVelocity(float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        double vx = -Math.sin(yawRad) * Math.cos(pitchRad) * maxSpeed;
        double vy = -Math.sin(pitchRad) * maxSpeed;
        double vz = Math.cos(yawRad) * Math.cos(pitchRad) * maxSpeed;

        return new Vec3d(vx, vy, vz);
    }

    public boolean hasExceededRange(Vec3d currentPos) {
        if (startPos == null) return false;
        return currentPos.distanceTo(startPos) > maxRange;
    }

    public void updateRotation(Entity entity, Vec3d velocity) {
        if (velocity.lengthSquared() > 0.0001) {
            entity.setYaw((float) (Math.atan2(-velocity.x, velocity.z) * (180.0 / Math.PI)));
            entity.setPitch((float) (Math.atan2(velocity.y, Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)) * (180.0 / Math.PI)));
        }
    }

    public void spawnTrailParticles(World world, double x, double y, double z) {
        world.addParticle(ParticleTypes.BUBBLE, x, y, z, 0, 0, 0);
    }

    public Vec3d getStartPos() {
        return startPos;
    }

    public Entity findNearestTarget(World world, Entity torpedo, Vec3d torpedoPos, float torpedoYaw,
                                    TargetingMode targetingMode, int ownerSubmarineId) {
        if (world == null) return null;

        List<Entity> nearbyEntities = world.getOtherEntities(torpedo,
            torpedo.getBoundingBox().expand(DETECTION_RANGE));

        Entity closestTarget = null;
        double closestDistance = DETECTION_RANGE;

        for (Entity entity : nearbyEntities) {
            if (entity instanceof BaseSubmarine && entity.getId() == ownerSubmarineId) {
                continue;
            }

            if (!matchesTargetingMode(entity, targetingMode)) {
                continue;
            }

            if (!entity.isSubmergedInWater()) {
                continue;
            }

            if (entity instanceof LivingEntity livingEntity && livingEntity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                continue;
            }

            Vec3d entityPos = entity.getPos();
            double distance = torpedoPos.distanceTo(entityPos);

            if (distance > DETECTION_RANGE) {
                continue;
            }

            if (!isInDetectionCone(torpedoPos, torpedoYaw, entityPos)) {
                continue;
            }

            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = entity;
            }
        }

        return closestTarget;
    }

    private boolean matchesTargetingMode(Entity entity, TargetingMode mode) {
        return switch (mode) {
            case ALL -> entity instanceof LivingEntity || entity instanceof BaseSubmarine;
            case PLAYERS -> entity instanceof PlayerEntity;
            case SUBMARINES -> entity instanceof BaseSubmarine;
            case ENTITIES -> entity instanceof LivingEntity && !(entity instanceof PlayerEntity);
        };
    }

    private boolean isInDetectionCone(Vec3d torpedoPos, float torpedoYaw, Vec3d targetPos) {
        Vec3d toTarget = targetPos.subtract(torpedoPos).normalize();

        float yawRad = (float) Math.toRadians(torpedoYaw);
        Vec3d forward = new Vec3d(-Math.sin(yawRad), 0, Math.cos(yawRad));

        double dotProduct = forward.x * toTarget.x + forward.z * toTarget.z;
        double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dotProduct))));

        return angle <= DETECTION_ANGLE / 2.0f;
    }

    public boolean isTargetValid(Entity target, Vec3d torpedoPos, float torpedoYaw) {
        if (target == null || target.isRemoved()) {
            return false;
        }

        if (!target.isSubmergedInWater()) {
            return false;
        }

        if (target instanceof LivingEntity livingEntity && livingEntity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            return false;
        }

        Vec3d targetPos = target.getPos();
        double distance = torpedoPos.distanceTo(targetPos);

        if (distance > DETECTION_RANGE) {
            return false;
        }

        if (!isInDetectionCone(torpedoPos, torpedoYaw, targetPos)) {
            return false;
        }

        return true;
    }

    public Vec3d calculateHomingVelocity(Vec3d currentVelocity, Vec3d torpedoPos, Vec3d targetPos, float currentYaw, float currentPitch) {
        Vec3d toTarget = targetPos.subtract(torpedoPos);
        double distanceToTarget = toTarget.length();

        if (distanceToTarget < 0.001) {
            return currentVelocity;
        }

        toTarget = toTarget.normalize();

        Vec3d currentDirection = currentVelocity.length() > 0.001
            ? currentVelocity.normalize()
            : new Vec3d(0, 0, 1);

        double dotProduct = currentDirection.dotProduct(toTarget);
        dotProduct = Math.max(-1.0, Math.min(1.0, dotProduct));
        double angleBetween = Math.acos(dotProduct);

        double maxTurnRad = Math.toRadians(MAX_TURN_RATE);

        Vec3d newDirection;
        if (angleBetween <= maxTurnRad) {
            newDirection = toTarget;
        } else {
            Vec3d axis = currentDirection.crossProduct(toTarget);
            double axisLength = axis.length();

            if (axisLength < 0.001) {
                newDirection = currentDirection;
            } else {
                axis = axis.normalize();

                double cos = Math.cos(maxTurnRad);
                double sin = Math.sin(maxTurnRad);

                newDirection = currentDirection.multiply(cos)
                    .add(axis.crossProduct(currentDirection).multiply(sin))
                    .add(axis.multiply(axis.dotProduct(currentDirection) * (1 - cos)));
            }
        }

        return newDirection.normalize().multiply(CONSTANT_SPEED);
    }

    public void updateDistanceTraveled(Vec3d movement) {
        totalDistanceTraveled += movement.length();
    }

    public double getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    public void setTotalDistanceTraveled(double distance) {
        this.totalDistanceTraveled = distance;
    }
}