package net.rizen.submarines.mixin.client.input;

import net.rizen.submarines.api.client.input.SubmarineInputHandler;
import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void preventHotbarScrollInSubmarine(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (this.client.player != null && this.client.player.getVehicle() instanceof BaseSubmarine && SubmarineInputHandler.isSubmarineHudMode()) {
            ci.cancel();
        }
    }
}