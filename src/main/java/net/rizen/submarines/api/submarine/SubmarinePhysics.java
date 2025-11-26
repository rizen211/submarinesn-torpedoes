package net.rizen.submarines.api.submarine;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Applies physics rules to keep submarines behaving correctly in water. This makes sure submarines sink when
 * they are out of water, prevents them from floating above the surface, and calculates their depth below sea level.
 */
public class SubmarinePhysics {

    public boolean isInWater(Entity entity) {
        return entity.isSubmergedIn(net.minecraft.registry.tag.FluidTags.WATER) || entity.isTouchingWater();
    }

    public Vec3d applyWaterPhysics(Entity entity, Vec3d movement) {
        if (!isInWater(entity)) {
            double fallSpeed = -0.4;
            return new Vec3d(0, fallSpeed, 0);
        }
        return movement;
    }

    public Vec3d constrainToWaterSurface(World world, Entity entity, Vec3d movement) {
        int waterLevel = world.getSeaLevel();
        double nextY = entity.getY() + movement.y;

        float entityHeight = entity.getHeight();
        double halfHeight = entityHeight / 2.0;

        double maxY = waterLevel - halfHeight;

        if (nextY > maxY && movement.y > 0) {
            double adjustedY = maxY - entity.getY();
            return new Vec3d(movement.x, adjustedY, movement.z);
        }

        return movement;
    }

    public int calculateDepth(World world, Entity entity) {
        int waterSurface = world.getSeaLevel();
        return Math.max(0, waterSurface - (int) entity.getY());
    }
}