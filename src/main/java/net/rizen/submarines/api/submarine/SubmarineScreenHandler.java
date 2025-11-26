package net.rizen.submarines.api.submarine;

import net.rizen.submarines.Mod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
 * Manages the submarine inventory GUI slots. This sets up all submarine inventory slots + the player
 * inventory slots and handles item transfers between them.
 */
public class SubmarineScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public SubmarineScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27));
    }

    public SubmarineScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(Mod.SUBMARINE_SCREEN_HANDLER, syncId);
        this.inventory = inventory;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(slot);

        if (clickedSlot != null && clickedSlot.hasStack()) {
            ItemStack clickedStack = clickedSlot.getStack();
            stack = clickedStack.copy();

            if (slot < 27) {
                if (!this.insertItem(clickedStack, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.insertItem(clickedStack, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (clickedStack.isEmpty()) {
                clickedSlot.setStack(ItemStack.EMPTY);
            } else {
                clickedSlot.markDirty();
            }
        }

        return stack;
    }
}