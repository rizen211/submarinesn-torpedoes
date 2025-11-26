package net.rizen.submarines.mixin.client;

import net.rizen.submarines.api.client.input.SubmarineInputHandler;
import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    public ClientPlayerEntity player;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void preventAttackInSubmarine(CallbackInfoReturnable<Boolean> cir) {
        if (this.player != null && this.player.hasVehicle() && this.player.getVehicle() instanceof BaseSubmarine) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void preventBlockBreakingInSubmarine(boolean breaking, CallbackInfo ci) {
        if (this.player != null && this.player.hasVehicle() && this.player.getVehicle() instanceof BaseSubmarine) {
            ci.cancel();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void blockPlayerInventoryInSubmarine(net.minecraft.client.gui.screen.Screen screen, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.player != null && client.player.hasVehicle() && client.player.getVehicle() instanceof BaseSubmarine submarine) {
            if (screen instanceof InventoryScreen) {
                if (SubmarineInputHandler.isSubmarineHudMode()) {
                    ci.cancel();
                    if (client.interactionManager != null) {
                        client.interactionManager.interactEntity(client.player, submarine, net.minecraft.util.Hand.MAIN_HAND);
                    }
                }
            }
        }
    }
}