package net.rizen.submarines.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SubmarineRepairToolItem extends Item {
    public SubmarineRepairToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
