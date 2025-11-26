package net.rizen.submarines.api.torpedo;

import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/**
 * Handles what happens when torpedoes hit things. This checks for collisions with blocks and entities, deals
 * damage, and creates explosions.
 */
public class TorpedoCollision {
    private final float explosionPower;
    private final float directDamage;
    private int ownerSubmarineId = -1;

    public TorpedoCollision(float explosionPower, float directDamage) {
        this.explosionPower = explosionPower;
        this.directDamage = directDamage;
    }

    public void setOwnerSubmarine(BaseSubmarine submarine) {
        if (submarine != null) {
            this.ownerSubmarineId = submarine.getId();
        }
    }

    public HitResult checkBlockCollision(World world, Entity torpedo, Vec3d velocity) {
        return world.raycast(new RaycastContext(
                torpedo.getPos(),
                torpedo.getPos().add(velocity),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                torpedo
        ));
    }

    public Entity checkEntityCollision(World world, Entity torpedo, Box boundingBox) {
        List<Entity> entities = world.getOtherEntities(torpedo, boundingBox, entity -> {
            if (entity instanceof BaseSubmarine && entity.getId() == ownerSubmarineId) {
                return false;
            }
            return entity instanceof LivingEntity || entity instanceof BaseSubmarine;
        });

        return entities.isEmpty() ? null : entities.get(0);
    }

    public void handleEntityHit(World world, Entity torpedo, Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            DamageSource damageSource = torpedo.getDamageSources().explosion(
                    world.createExplosion(
                            torpedo,
                            torpedo.getX(),
                            torpedo.getY(),
                            torpedo.getZ(),
                            0,
                            World.ExplosionSourceType.NONE
                    )
            );
            livingTarget.damage(damageSource, directDamage);
        } else if (target instanceof BaseSubmarine submarine) {
            submarine.damage(torpedo.getDamageSources().explosion(null), directDamage);
        }
    }

    public void explode(World world, Entity torpedo) {
        if (!world.isClient) {
            world.createExplosion(
                    torpedo,
                    torpedo.getX(),
                    torpedo.getY(),
                    torpedo.getZ(),
                    explosionPower,
                    World.ExplosionSourceType.TNT
            );
            torpedo.discard();
        }
    }

    public int getOwnerSubmarineId() {
        return ownerSubmarineId;
    }

    public void setOwnerSubmarineId(int id) {
        this.ownerSubmarineId = id;
    }
}