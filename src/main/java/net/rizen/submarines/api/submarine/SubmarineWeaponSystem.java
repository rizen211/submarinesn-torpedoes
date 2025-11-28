package net.rizen.submarines.api.submarine;

import net.rizen.submarines.api.item.TorpedoItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Manages torpedo firing mechanics for submarines. This tracks the cooldown between shots and the arming timer
 * that activates when new torpedoes are loaded. Torpedoes need to finish arming before they can be fired.
 */
public class SubmarineWeaponSystem {
    private int torpedoCooldown = 0;
    private int torpedoArmingTimer = 0;
    private int previousTorpedoCount = 0;

    private final int cooldownTicks;
    private final int armingTicks;

    public SubmarineWeaponSystem(int cooldownTicks, int armingTicks) {
        this.cooldownTicks = cooldownTicks;
        this.armingTicks = armingTicks;
    }

    public void tick() {
        if (torpedoCooldown > 0) {
            torpedoCooldown--;
        }
        if (torpedoArmingTimer > 0) {
            torpedoArmingTimer--;
        }
    }

    public int countTorpedoes(Inventory inventory) {
        int count = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof TorpedoItem) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public void updateTorpedoCount(Inventory inventory) {
        int currentCount = countTorpedoes(inventory);
        if (previousTorpedoCount == 0 && currentCount > 0) {
            torpedoArmingTimer = armingTicks;
        }
        previousTorpedoCount = currentCount;
    }

    public boolean findAndConsumeTorpedo(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof TorpedoItem && !stack.isEmpty()) {
                stack.decrement(1);
                inventory.markDirty();
                return true;
            }
        }
        return false;
    }

    public boolean canFire() {
        return torpedoCooldown == 0 && torpedoArmingTimer == 0;
    }

    public void setFired() {
        torpedoCooldown = cooldownTicks;
        torpedoArmingTimer = armingTicks;
    }

    public boolean isArmed() {
        return torpedoArmingTimer == 0;
    }

    public int getTorpedoCooldown() {
        return torpedoCooldown;
    }

    public int getTorpedoArmingTimer() {
        return torpedoArmingTimer;
    }

    public int getPreviousTorpedoCount() {
        return previousTorpedoCount;
    }

    public void setTorpedoCooldown(int cooldown) {
        this.torpedoCooldown = cooldown;
    }

    public void setPreviousTorpedoCount(int count) {
        this.previousTorpedoCount = count;
    }
}