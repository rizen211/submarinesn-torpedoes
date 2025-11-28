package net.rizen.submarines.entity;

import net.rizen.submarines.Mod;
import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TacticalSubmarineEntity extends BaseSubmarine {
    private static final float MAX_SPEED = 0.15f;
    private static final float ACCELERATION = 0.005f;
    private static final float DECELERATION = 0.003f;
    private static final float ROTATION_SPEED = 2.0f;
    private static final float ROTATION_ACCELERATION = 0.5f;
    private static final float ROTATION_DECELERATION = 0.7f;
    private static final float VERTICAL_SPEED_MULT = 0.4f;
    private static final float BACKWARD_SPEED_MULT = 0.6f;

    private static final float MAX_POWER = 100.0f;
    private static final float POWER_CONSUMPTION = 0.01f;

    private static final int TORPEDO_COOLDOWN = 40;
    private static final int TORPEDO_ARMING = 60;
    private static final float TORPEDO_FIRE_COST = 2.0f;
    private static final Vec3d TORPEDO_SPAWN_OFFSET = new Vec3d(4.0, 0, 0);

    private static final float WIDTH = 2.5f;
    private static final float HEIGHT = 2.5f;
    private static final float LENGTH = 6.5f;

    public TacticalSubmarineEntity(EntityType<? extends TacticalSubmarineEntity> entityType, World world) {
        super(entityType, world,
                MAX_SPEED, ACCELERATION, DECELERATION, ROTATION_SPEED,
                ROTATION_ACCELERATION, ROTATION_DECELERATION,
                VERTICAL_SPEED_MULT, BACKWARD_SPEED_MULT,
                MAX_POWER, POWER_CONSUMPTION,
                TORPEDO_COOLDOWN, TORPEDO_ARMING,
                LightweightTorpedoEntity::new, TORPEDO_FIRE_COST, TORPEDO_SPAWN_OFFSET,
                WIDTH, HEIGHT, LENGTH);
    }

    public TacticalSubmarineEntity(World world) {
        this(Mod.TACTICAL_SUBMARINE_ENTITY, world);
    }

    @Override
    public net.minecraft.util.math.Vec3d getPassengerRidingPos(Entity passenger) {
        float yawRad = (float) Math.toRadians(this.getYaw());

        double forwardOffset = 0.0;
        double upOffset = 4.0;

        double x = this.getX() - Math.sin(yawRad) * forwardOffset;
        double y = this.getY() + upOffset;
        double z = this.getZ() + Math.cos(yawRad) * forwardOffset;

        return new net.minecraft.util.math.Vec3d(x, y, z);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("entity.submarines.tactical_submarine");
    }
}