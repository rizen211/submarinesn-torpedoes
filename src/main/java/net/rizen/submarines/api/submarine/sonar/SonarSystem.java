package net.rizen.submarines.api.submarine.sonar;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * The sonar detection system for submarines. With a single key press, sonar sends out a ping that scans the surrounding
 * area for entities and terrain.
 * Contacts fade out after a few seconds so the display stays clean. The system scans in all directions and can
 * detect submarines, players, mobs, items, and underwater terrain within range.
 */
public class SonarSystem {
    private final List<SonarContact> contacts;
    private float sweepAngle;
    private long lastPingTime;
    private static final float SWEEP_SPEED = 1.5f;
    private static final double MAX_RANGE = 64.0;

    public SonarSystem() {
        this.contacts = new ArrayList<>();
        this.sweepAngle = 0.0f;
        this.lastPingTime = 0;
    }

    public void tick() {
        sweepAngle += SWEEP_SPEED;
        if (sweepAngle >= 360.0f) {
            sweepAngle -= 360.0f;
        }

        long currentTime = System.currentTimeMillis();
        contacts.removeIf(contact -> contact.shouldRemove(currentTime));

        for (SonarContact contact : contacts) {
            if (!contact.isRevealed() && isAngleInSweep(contact.getAngle())) {
                contact.reveal(currentTime);
            }
        }
    }

    private boolean isAngleInSweep(float contactAngle) {
        float tolerance = SWEEP_SPEED + 2.0f;
        float diff = Math.abs(normalizeAngle(sweepAngle - contactAngle));
        return diff <= tolerance;
    }

    private float normalizeAngle(float angle) {
        while (angle > 180.0f) angle -= 360.0f;
        while (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    public void performPing(World world, Vec3d submarinePos, float submarineYaw, Entity submarine) {
        lastPingTime = System.currentTimeMillis();
        contacts.clear();

        detectEntities(world, submarinePos, submarineYaw);
        detectTerrain(world, submarinePos, submarineYaw, submarine);
    }

    private void detectEntities(World world, Vec3d submarinePos, float submarineYaw) {
        List<Entity> nearbyEntities = world.getOtherEntities(null,
                new net.minecraft.util.math.Box(
                        submarinePos.x - MAX_RANGE, submarinePos.y - MAX_RANGE, submarinePos.z - MAX_RANGE,
                        submarinePos.x + MAX_RANGE, submarinePos.y + MAX_RANGE, submarinePos.z + MAX_RANGE
                ));

        for (Entity entity : nearbyEntities) {
            Vec3d entityPos = entity.getPos();
            Vec3d relativePos = entityPos.subtract(submarinePos);
            double distance = relativePos.length();

            if (distance > MAX_RANGE || distance < 5.0) {
                continue;
            }

            if (!isEntityInWater(world, entity)) {
                continue;
            }

            float angle = calculateAngle(relativePos, submarineYaw);
            ContactType type = classifyEntity(entity);

            contacts.add(new SonarContact(relativePos, type, distance, angle, lastPingTime));
        }
    }

    private void detectTerrain(World world, Vec3d submarinePos, float submarineYaw, Entity submarine) {
        for (int angleDeg = 0; angleDeg < 360; angleDeg += 3) {
            float worldAngle = submarineYaw + angleDeg;
            float yawRad = (float) Math.toRadians(worldAngle);
            Vec3d direction = new Vec3d(-Math.sin(yawRad), 0, Math.cos(yawRad));

            Vec3d start = submarinePos.add(direction.multiply(5.0));
            Vec3d end = submarinePos.add(direction.multiply(MAX_RANGE));

            BlockHitResult hitResult = world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                submarine
            ));

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = hitResult.getBlockPos();
                if (isBlockUnderwater(world, blockPos)) {
                    Vec3d hitPos = hitResult.getPos();
                    Vec3d relativePos = hitPos.subtract(submarinePos);
                    double dist = relativePos.length();
                    float angle = calculateAngle(relativePos, submarineYaw);
                    contacts.add(new SonarContact(relativePos, ContactType.TERRAIN, dist, angle, lastPingTime));
                }
            }
        }
    }

    private boolean isEntityInWater(World world, Entity entity) {
        return entity.isSubmergedInWater() || entity.isTouchingWater();
    }

    private boolean isBlockUnderwater(World world, BlockPos blockPos) {
        return !world.getFluidState(blockPos.up()).isEmpty() ||
               !world.getFluidState(blockPos).isEmpty();
    }

    private float calculateAngle(Vec3d relativePos, float submarineYaw) {
        float angle = (float) Math.toDegrees(Math.atan2(-relativePos.x, relativePos.z));
        angle = angle - submarineYaw;
        while (angle < 0) angle += 360;
        while (angle >= 360) angle -= 360;
        return angle;
    }

    private ContactType classifyEntity(Entity entity) {
        if (entity instanceof net.rizen.submarines.api.submarine.BaseSubmarine) {
            return ContactType.SUBMARINE;
        }

        if (entity instanceof net.minecraft.entity.player.PlayerEntity) {
            return ContactType.PLAYER;
        }

        if (entity instanceof net.minecraft.entity.ItemEntity) {
            return ContactType.ITEM;
        }

        String entityName = entity.getType().toString().toLowerCase();
        if (entityName.contains("squid") || entityName.contains("cod") ||
            entityName.contains("salmon") || entityName.contains("tropical")) {
            return ContactType.SMALL_MOB;
        }

        return ContactType.MEDIUM_ENTITY;
    }

    public List<SonarContact> getContacts() {
        return contacts;
    }

    public float getSweepAngle() {
        return sweepAngle;
    }

    public long getLastPingTime() {
        return lastPingTime;
    }

    public double getMaxRange() {
        return MAX_RANGE;
    }
}
