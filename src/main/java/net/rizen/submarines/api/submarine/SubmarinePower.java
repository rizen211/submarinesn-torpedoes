package net.rizen.submarines.api.submarine;

import net.rizen.submarines.api.item.TorpedoItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

/**
 * Manages the power system for submarines. Power gets consumed when moving and gets replenished by
 * burning fuel items from the submarine inventory. Different fuel items provide different amounts of power.
 */
public class SubmarinePower {
    private final float maxPower;
    private final float movementConsumption;
    private float currentPower;

    public SubmarinePower(float maxPower, float movementConsumption) {
        this.maxPower = maxPower;
        this.movementConsumption = movementConsumption;
        this.currentPower = maxPower;
    }

    public void consumePower(boolean isActive, float powerMultiplier) {
        if (isActive) {
            currentPower = Math.max(0, currentPower - (movementConsumption * powerMultiplier));
        }
    }

    public boolean tryConsumeFuel(Inventory inventory) {
        if (currentPower >= maxPower) {
            return false;
        }

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty() || stack.getItem() instanceof TorpedoItem) {
                continue;
            }

            float fuelValue = getFuelPowerValue(stack);
            if (fuelValue > 0) {
                float powerNeeded = maxPower - currentPower;
                float wastedPower = fuelValue - powerNeeded;
                boolean shouldUseFuel = wastedPower <= 0 || currentPower <= getUsageThreshold(stack);

                if (shouldUseFuel) {
                    currentPower = Math.min(maxPower, currentPower + fuelValue);
                    boolean isLavaBucket = stack.getItem() == Items.LAVA_BUCKET;
                    stack.decrement(1);

                    if (isLavaBucket) {
                        if (stack.isEmpty()) {
                            inventory.setStack(i, new ItemStack(Items.BUCKET));
                        } else {
                            addItemToInventory(inventory, new ItemStack(Items.BUCKET));
                        }
                    }

                    inventory.markDirty();
                    return true;
                }
            }
        }
        return false;
    }

    private float getFuelPowerValue(ItemStack stack) {
        if (stack.getItem() == Items.LAVA_BUCKET) return 50.0f;
        if (stack.getItem() == Items.COAL_BLOCK) return 30.0f;
        if (stack.getItem() == Items.DRIED_KELP_BLOCK) return 10.0f;
        if (stack.getItem() == Items.BLAZE_ROD) return 5.0f;
        if (stack.getItem() == Items.COAL) return 3.0f;
        if (stack.getItem() == Items.CHARCOAL) return 3.0f;
        if (stack.isIn(ItemTags.LOGS)) return 1.5f;
        if (stack.isIn(ItemTags.PLANKS)) return 1.0f;
        if (stack.getItem() == Items.STICK) return 0.5f;
        if (stack.getItem() == Items.BAMBOO) return 1.0f;
        return 0.0f;
    }

    private float getUsageThreshold(ItemStack stack) {
        if (stack.getItem() == Items.LAVA_BUCKET) return 50.0f;
        if (stack.getItem() == Items.COAL_BLOCK) return 70.0f;
        if (stack.getItem() == Items.DRIED_KELP_BLOCK) return 90.0f;
        if (stack.getItem() == Items.BLAZE_ROD) return 95.0f;
        if (stack.getItem() == Items.COAL) return 97.0f;
        if (stack.getItem() == Items.CHARCOAL) return 97.0f;
        return 100.0f;
    }

    private void addItemToInventory(Inventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slotStack = inventory.getStack(i);
            if (slotStack.isEmpty()) {
                inventory.setStack(i, stack);
                return;
            } else if (ItemStack.areItemsAndComponentsEqual(slotStack, stack)) {
                int maxCount = Math.min(stack.getMaxCount(), inventory.getMaxCountPerStack());
                int transferAmount = Math.min(stack.getCount(), maxCount - slotStack.getCount());
                if (transferAmount > 0) {
                    slotStack.increment(transferAmount);
                    stack.decrement(transferAmount);
                    if (stack.isEmpty()) {
                        return;
                    }
                }
            }
        }
    }

    public boolean consumePowerAmount(float amount) {
        if (currentPower >= amount) {
            currentPower -= amount;
            return true;
        }
        return false;
    }

    public boolean hasPower() {
        return currentPower > 0;
    }

    public float getCurrentPower() {
        return currentPower;
    }

    public void setCurrentPower(float power) {
        this.currentPower = Math.max(0, Math.min(maxPower, power));
    }
}