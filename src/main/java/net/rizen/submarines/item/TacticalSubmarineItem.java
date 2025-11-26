package net.rizen.submarines.item;

import net.rizen.submarines.entity.TacticalSubmarineEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class TacticalSubmarineItem extends Item {
    public TacticalSubmarineItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(5.0));

        BlockHitResult hitResult = world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.ANY,
                player
        ));

        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(stack);
        }

        BlockPos targetPos = hitResult.getBlockPos();

        if (!isValidPlacement(world, targetPos, player)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            TacticalSubmarineEntity submarine = new TacticalSubmarineEntity(world);
            submarine.setPosition(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            submarine.setYaw(player.getYaw());
            world.spawnEntity(submarine);

            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    private boolean isValidPlacement(World world, BlockPos pos, PlayerEntity player) {
        boolean isWater = world.getBlockState(pos).isOf(Blocks.WATER);

        if (!isWater) {
            BlockPos abovePos = pos.up();
            isWater = world.getBlockState(abovePos).isOf(Blocks.WATER);

            if (!isWater) {
                if (world.isClient) {
                    player.sendMessage(
                            Text.translatable("submarines.placement.water_only")
                                    .formatted(Formatting.RED),
                            true
                    );
                }
                return false;
            }
        }

        if (world.getBlockState(pos).isOf(Blocks.LAVA)) {
            if (world.isClient) {
                player.sendMessage(
                        Text.translatable("submarines.placement.water_only")
                                .formatted(Formatting.RED),
                        true
                );
            }
            return false;
        }

        int seaLevel = world.getSeaLevel();
        if (pos.getY() >= seaLevel) {
            boolean waterFound = false;
            for (int y = pos.getY(); y >= pos.getY() - 3 && y >= 0; y--) {
                if (world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).isOf(Blocks.WATER)) {
                    waterFound = true;
                    break;
                }
            }

            if (!waterFound) {
                if (world.isClient) {
                    player.sendMessage(
                            Text.translatable("submarines.placement.water_only")
                                    .formatted(Formatting.RED),
                            true
                    );
                }
                return false;
            }
        }

        return true;
    }
}