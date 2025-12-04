package net.rizen.submarines.mixin.common.entity;

import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void preventStatusEffectsInSubmarine(StatusEffectInstance effect, Entity source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof net.minecraft.entity.player.PlayerEntity player) {
            if (player.getVehicle() instanceof BaseSubmarine) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    if (element.getClassName().contains("CommandManager") ||
                        element.getClassName().contains("EffectCommand") ||
                        element.getMethodName().contains("execute")) {
                        if (element.getClassName().contains("net.minecraft.server.command")) {
                            return;
                        }
                    }
                }
                cir.setReturnValue(false);
            }
        }
    }
}
