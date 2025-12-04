package net.rizen.submarines.mixin.common.entity;

import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getVehicle() instanceof BaseSubmarine) {
            if (player.getAir() < player.getMaxAir()) {
                player.setAir(player.getMaxAir());
            }
            if (player.isSwimming()) {
                player.setSwimming(false);
            }
        }
    }
}