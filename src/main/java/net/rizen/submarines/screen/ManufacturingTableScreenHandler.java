package net.rizen.submarines.screen;

import net.rizen.submarines.Mod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.atomic.AtomicReference;

public class ManufacturingTableScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final BlockPos tablePos;

    public ManufacturingTableScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(Mod.MANUFACTURING_TABLE_SCREEN_HANDLER, syncId);
        this.context = ScreenHandlerContext.EMPTY;
        this.tablePos = pos;
    }

    public ManufacturingTableScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(Mod.MANUFACTURING_TABLE_SCREEN_HANDLER, syncId);
        this.context = context;

        AtomicReference<BlockPos> posRef = new AtomicReference<>(BlockPos.ORIGIN);
        context.run((world, pos) -> posRef.set(pos));
        this.tablePos = posRef.get();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, Mod.MANUFACTURING_TABLE);
    }

    public BlockPos getTablePos() {
        return this.tablePos;
    }
}
