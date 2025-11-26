package net.rizen.submarines.mixin.client.hud;

import net.rizen.submarines.api.client.input.SubmarineInputHandler;
import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hideHotbarInSubmarine(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (client.player != null && client.player.getVehicle() instanceof BaseSubmarine && SubmarineInputHandler.isSubmarineHudMode()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void hideStatusBarsInSubmarine(CallbackInfo ci) {
        if (client.player != null && client.player.getVehicle() instanceof BaseSubmarine && SubmarineInputHandler.isSubmarineHudMode()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void hideExperienceBarInSubmarine(CallbackInfo ci) {
        if (client.player != null && client.player.getVehicle() instanceof BaseSubmarine && SubmarineInputHandler.isSubmarineHudMode()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void hideCrosshairInSubmarine(CallbackInfo ci) {
        if (client.player != null && client.player.getVehicle() instanceof BaseSubmarine && SubmarineInputHandler.isSubmarineHudMode()) {
            ci.cancel();
        }
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void overrideSubmarineMountTooltip(Text message, boolean tinted, CallbackInfo ci) {
        if (client.player != null && client.player.getVehicle() instanceof BaseSubmarine) {
            String messageString = message.getString();
            if (messageString.contains(client.options.sneakKey.getBoundKeyLocalizedText().getString())) {
                Text keyText = Text.keybind("key.submarines.dismount");
                Text customText = Text.translatable("submarines.mount.onboard", keyText);

                ci.cancel();
                ((InGameHud)(Object)this).setOverlayMessage(customText, tinted);
            }
        }
    }
}