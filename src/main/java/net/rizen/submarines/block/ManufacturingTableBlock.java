package net.rizen.submarines.block;

import net.rizen.submarines.screen.ManufacturingTableScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ManufacturingTableBlock extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 1, 1.0);
    private static final Text TITLE = Text.translatable("container.manufacturing_table");

    public ManufacturingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            player.openHandledScreen(new ExtendedScreenHandlerFactory<BlockPos>() {
                @Override
                public BlockPos getScreenOpeningData(ServerPlayerEntity serverPlayer) {
                    return pos;
                }

                @Override
                public Text getDisplayName() {
                    return TITLE;
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    return new ManufacturingTableScreenHandler(
                        syncId,
                        playerInventory,
                        ScreenHandlerContext.create(world, pos)
                    );
                }
            });
        }
        return ActionResult.SUCCESS;
    }
}
