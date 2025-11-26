package net.rizen.submarines.api.submarine;

import net.minecraft.world.World;
import net.rizen.submarines.api.torpedo.BaseTorpedo;

/**
 * Functional interface for creating torpedoes. Used by submarines to define torpedo type
 * without implementing firing boilerplate.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * TorpedoSpawner spawner = (world, x, y, z, yaw, pitch) ->
 *     new CustomTorpedoEntity(world, x, y, z, yaw, pitch);
 * }</pre>
 */
@FunctionalInterface
public interface TorpedoSpawner {
    /**
     * Creates a new torpedo at the specified position and rotation.
     *
     * @param world the world to spawn the torpedo in
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param yaw the horizontal rotation
     * @param pitch the vertical rotation
     * @return the created torpedo entity
     */
    BaseTorpedo create(World world, double x, double y, double z, float yaw, float pitch);
}
