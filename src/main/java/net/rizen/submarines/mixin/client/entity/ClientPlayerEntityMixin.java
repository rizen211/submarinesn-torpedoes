package net.rizen.submarines.mixin.client.entity;

import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "tickRiding", at = @At("HEAD"), cancellable = true)
    private void preventVanillaDismount(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.getVehicle() instanceof BaseSubmarine) {
            if (player.input.sneaking) {
                player.input.sneaking = false;
            }
        }
    }
}