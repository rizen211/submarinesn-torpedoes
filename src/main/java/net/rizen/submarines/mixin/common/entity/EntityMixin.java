package net.rizen.submarines.mixin.common.entity;

import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract Entity getVehicle();

    @Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true)
    private void preventSubmarineSneakDismount(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        Entity vehicle = this.getVehicle();

        if (vehicle instanceof BaseSubmarine) {
            if (entity instanceof net.minecraft.entity.player.PlayerEntity player)
                if (player.isSneaking()) {
                    ci.cancel();
                }
            }
        }
    }