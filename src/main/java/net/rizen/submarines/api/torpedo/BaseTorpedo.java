package net.rizen.submarines.api.torpedo;

import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Base torpedo entity that submarines fire as their main weapon. Torpedoes are self-propelled projectiles
 * that travel underwater and lock onto targets using homing guidance.
 *
 * Torpedoes have a short arming delay after firing before they can track targets. They explode on impact
 * with blocks or entities, when leaving the water, or when exceeding maximum range. The targeting system
 * acquires and tracks different entity types based on the current targeting mode.
 *
 * Subclasses define torpedo variants with different speeds, ranges, and damage profiles.
 */
public abstract class BaseTorpedo extends ProjectileEntity {
    protected final TorpedoPhysics physics;
    protected final TorpedoCollision collision;
    protected int ticksAlive = 0;

    protected float width;
    protected float height;
    protected float length;

    private static final int ARMING_DELAY = 10;
    private static final int WATER_CHECK_DELAY = 5;
    private Entity currentTarget = null;
    private TargetingMode targetingMode = TargetingMode.ALL;

    /**
     * Creates a new torpedo with the given stats. Sets up the physics system for movement and homing,
     * and the collision system for damage and explosions.
     *
     * @param entityType the Minecraft entity type for this torpedo
     * @param world the world this torpedo exists in
     * @param maxSpeed torpedo travel speed
     * @param maxRange maximum distance in blocks before self-destructing
     * @param explosionPower size of the explosion on impact
     * @param directDamage additional damage dealt to the directly hit entity
     * @param width torpedo width in blocks
     * @param height torpedo height in blocks
     * @param length torpedo length in blocks
     */
    public BaseTorpedo(EntityType<? extends BaseTorpedo> entityType, World world,
                       float maxSpeed, int maxRange, float explosionPower, float directDamage,
                       float width, float height, float length) {
        super(entityType, world);
        this.physics = new TorpedoPhysics(maxSpeed, maxRange);
        this.collision = new TorpedoCollision(explosionPower, directDamage);
        this.width = width;
        this.height = height;
        this.length = length;
        this.noClip = false;
    }

    /**
     * Initializes the torpedo when it first spawns. Positions it in the world and applies initial velocity
     * based on the firing angle. The physics system records the starting position to track total distance traveled.
     *
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param z starting z coordinate
     * @param yaw horizontal firing angle
     * @param pitch vertical firing angle
     */
    public void initialize(double x, double y, double z, float yaw, float pitch) {
        this.setPosition(x, y, z);
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.physics.setStartPosition(new Vec3d(x, y, z));

        Vec3d velocity = physics.calculateVelocity(yaw, pitch);
        this.setVelocity(velocity.x, velocity.y, velocity.z);
    }

    public void setTargetingMode(TargetingMode mode) {
        this.targetingMode = mode;
    }

    public TargetingMode getTargetingMode() {
        return targetingMode;
    }

    /**
     * Sets the submarine that fired this torpedo. Prevents the torpedo from immediately exploding on
     * the firing submarine and excludes it from target acquisition.
     *
     * @param submarine the submarine that fired this torpedo
     */
    public void setOwnerSubmarine(BaseSubmarine submarine) {
        collision.setOwnerSubmarine(submarine);
    }

    @Override
    public net.minecraft.entity.EntityDimensions getDimensions(net.minecraft.entity.EntityPose pose) {
        return net.minecraft.entity.EntityDimensions.changing(width, height);
    }

    @Override
    protected Box calculateBoundingBox() {
        float halfWidth = width / 2.0f;
        float halfHeight = height / 2.0f;
        float halfLength = length / 2.0f;

        double yawRad = Math.toRadians(this.getYaw());
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);

        double maxX = Math.abs(halfWidth * cos) + Math.abs(halfLength * sin);
        double maxZ = Math.abs(halfWidth * sin) + Math.abs(halfLength * cos);

        return new Box(
                this.getX() - maxX, this.getY() - halfHeight, this.getZ() - maxZ,
                this.getX() + maxX, this.getY() + halfHeight, this.getZ() + maxZ
        );
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        ticksAlive++;

        if (this.getWorld().isClient) {
            physics.spawnTrailParticles(this.getWorld(), this.getX(), this.getY(), this.getZ());
        }

        if (ticksAlive > WATER_CHECK_DELAY && !this.isSubmergedInWater()) {
            if (!this.getWorld().isClient) {
                collision.explode(this.getWorld(), this);
            }
            return;
        }

        if (physics.hasExceededRange(this.getPos())) {
            collision.explode(this.getWorld(), this);
            return;
        }

        if (physics.getTotalDistanceTraveled() >= 128.0) {
            collision.explode(this.getWorld(), this);
            return;
        }

        HitResult blockHit = collision.checkBlockCollision(this.getWorld(), this, this.getVelocity());
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.onCollision(blockHit);
            return;
        }

        if (!this.getWorld().isClient) {
            Entity entityHit = collision.checkEntityCollision(this.getWorld(), this, this.getBoundingBox());
            if (entityHit != null) {
                collision.handleEntityHit(this.getWorld(), this, entityHit);
                collision.explode(this.getWorld(), this);
                return;
            }
        }

        if (!this.getWorld().isClient && ticksAlive > ARMING_DELAY) {
            if (!physics.isTargetValid(currentTarget, this.getPos(), this.getYaw())) {
                currentTarget = physics.findNearestTarget(
                    this.getWorld(),
                    this,
                    this.getPos(),
                    this.getYaw(),
                    targetingMode,
                    collision.getOwnerSubmarineId()
                );
            }

            if (currentTarget != null) {
                Vec3d newVelocity = physics.calculateHomingVelocity(
                    this.getVelocity(),
                    this.getPos(),
                    currentTarget.getPos(),
                    this.getYaw(),
                    this.getPitch()
                );
                this.setVelocity(newVelocity);
            }
        }

        Vec3d movement = this.getVelocity();
        this.setPosition(
                this.getX() + movement.x,
                this.getY() + movement.y,
                this.getZ() + movement.z
        );

        physics.updateDistanceTraveled(movement);
        physics.updateRotation(this, this.getVelocity());
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getWorld().isClient) {
            collision.explode(this.getWorld(), this);
        }
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("StartPosX")) {
            Vec3d startPos = new Vec3d(
                    nbt.getDouble("StartPosX"),
                    nbt.getDouble("StartPosY"),
                    nbt.getDouble("StartPosZ")
            );
            this.physics.setStartPosition(startPos);
        }
        this.ticksAlive = nbt.getInt("TicksAlive");
        this.collision.setOwnerSubmarineId(nbt.getInt("OwnerSubmarineId"));
        this.physics.setTotalDistanceTraveled(nbt.getDouble("DistanceTraveled"));
        if (nbt.contains("TargetingMode")) {
            this.targetingMode = TargetingMode.values()[nbt.getInt("TargetingMode")];
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        Vec3d startPos = this.physics.getStartPos();
        if (startPos != null) {
            nbt.putDouble("StartPosX", startPos.x);
            nbt.putDouble("StartPosY", startPos.y);
            nbt.putDouble("StartPosZ", startPos.z);
        }
        nbt.putInt("TicksAlive", this.ticksAlive);
        nbt.putInt("OwnerSubmarineId", this.collision.getOwnerSubmarineId());
        nbt.putDouble("DistanceTraveled", this.physics.getTotalDistanceTraveled());
        nbt.putInt("TargetingMode", this.targetingMode.ordinal());
    }
}