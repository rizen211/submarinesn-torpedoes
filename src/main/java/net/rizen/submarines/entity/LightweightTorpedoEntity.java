package net.rizen.submarines.entity;

import net.rizen.submarines.Mod;
import net.rizen.submarines.api.torpedo.BaseTorpedo;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class LightweightTorpedoEntity extends BaseTorpedo {
    private static final float MAX_SPEED = 55f / 20f;
    private static final int MAX_RANGE = 128;
    private static final float EXPLOSION_POWER = 4.0f;
    private static final float DIRECT_DAMAGE = 50.0f;

    private static final float WIDTH = 0.5f;
    private static final float HEIGHT = 0.5f;
    private static final float LENGTH = 2.0f;

    public LightweightTorpedoEntity(EntityType<? extends LightweightTorpedoEntity> entityType, World world) {
        super(entityType, world, MAX_SPEED, MAX_RANGE, EXPLOSION_POWER, DIRECT_DAMAGE, WIDTH, HEIGHT, LENGTH);
    }

    public LightweightTorpedoEntity(World world, double x, double y, double z, float yaw, float pitch) {
        this(Mod.LIGHTWEIGHT_TORPEDO_ENTITY, world);
        initialize(x, y, z, yaw, pitch);
    }
}