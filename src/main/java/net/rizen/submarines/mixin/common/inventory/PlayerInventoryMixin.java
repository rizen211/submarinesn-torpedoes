package net.rizen.submarines.mixin.common.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.rizen.submarines.api.client.input.SubmarineInputHandler;
import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow
    public int selectedSlot;

    private int lastValidSlot = 0;

    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void preventHotbarScroll(double scrollAmount, CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        if (inventory.player != null && inventory.player.getVehicle() instanceof BaseSubmarine) {
            if (inventory.player.getWorld().isClient && SubmarineInputHandler.isSubmarineHudMode()) {
                ci.cancel();
            } else if (!inventory.player.getWorld().isClient) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "updateItems", at = @At("TAIL"))
    private void lockHotbarSlot(CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        if (inventory.player != null && inventory.player.getVehicle() instanceof BaseSubmarine) {
            boolean shouldLock = !inventory.player.getWorld().isClient || SubmarineInputHandler.isSubmarineHudMode();
            if (shouldLock) {
                if (this.selectedSlot != this.lastValidSlot) {
                    this.selectedSlot = this.lastValidSlot;
                }
            } else {
                this.lastValidSlot = this.selectedSlot;
            }
        } else {
            this.lastValidSlot = this.selectedSlot;
        }
    }

    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void preventItemPickupInSubmarine(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        if (inventory.player != null && inventory.player.getVehicle() instanceof BaseSubmarine) {
            boolean shouldPrevent = !inventory.player.getWorld().isClient || SubmarineInputHandler.isSubmarineHudMode();
            if (shouldPrevent) {
                cir.setReturnValue(false);
            }
        }
    }
}