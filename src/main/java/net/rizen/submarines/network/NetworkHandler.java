package net.rizen.submarines.network;

import net.rizen.submarines.Mod;
import net.rizen.submarines.api.crafting.ManufacturingRecipe;
import net.rizen.submarines.api.crafting.ManufacturingRecipeRegistry;
import net.rizen.submarines.api.network.packet.SubmarineInputPacket;
import net.rizen.submarines.api.network.packet.TorpedoFirePacket;
import net.rizen.submarines.api.network.packet.DismountPacket;
import net.rizen.submarines.api.network.packet.SonarPingPacket;
import net.rizen.submarines.api.network.packet.MovementModeTogglePacket;
import net.rizen.submarines.api.network.packet.TargetingModeTogglePacket;
import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.rizen.submarines.api.network.packet.ManufacturingCraftPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class NetworkHandler {
    public static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(SubmarineInputPacket.ID, SubmarineInputPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TorpedoFirePacket.ID, TorpedoFirePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(DismountPacket.ID, DismountPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SonarPingPacket.ID, SonarPingPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(MovementModeTogglePacket.ID, MovementModeTogglePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(TargetingModeTogglePacket.ID, TargetingModeTogglePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ManufacturingCraftPacket.ID, ManufacturingCraftPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SubmarineInputPacket.ID, (payload, context) -> {
            SubmarinePacketHelper.withValidatedSubmarine(context, payload.entityId(), "input", submarine -> {
                submarine.updateInput(
                        payload.forward(),
                        payload.backward(),
                        payload.left(),
                        payload.right(),
                        payload.up(),
                        payload.down()
                );
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TorpedoFirePacket.ID, (payload, context) -> {
            SubmarinePacketHelper.withValidatedSubmarine(context, payload.submarineId(), "torpedo_fire", BaseSubmarine::fireTorpedo);
        });

        ServerPlayNetworking.registerGlobalReceiver(DismountPacket.ID, (payload, context) -> {
            SubmarinePacketHelper.withValidatedSubmarine(context, payload.submarineId(), "dismount", submarine -> {
                context.player().setSneaking(false);
                context.player().stopRiding();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SonarPingPacket.ID, (payload, context) -> {
            SubmarinePacketHelper.withValidatedSubmarine(context, payload.submarineId(), "sonar_ping", BaseSubmarine::performSonarPing);
        });

        ServerPlayNetworking.registerGlobalReceiver(MovementModeTogglePacket.ID, (payload, context) -> {
            SubmarinePacketHelper.withValidatedSubmarine(context, payload.submarineId(), "mode_toggle", BaseSubmarine::cycleMovementMode);
        });

        ServerPlayNetworking.registerGlobalReceiver(TargetingModeTogglePacket.ID, (payload, context) -> {
            SubmarinePacketHelper.withValidatedSubmarine(context, payload.submarineId(), "mode_toggle", BaseSubmarine::cycleTargetingMode);
        });

        ServerPlayNetworking.registerGlobalReceiver(ManufacturingCraftPacket.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                PlayerInventory inventory = context.player().getInventory();

                if (!context.player().getWorld().getBlockState(payload.tablePos()).isOf(Mod.MANUFACTURING_TABLE)) {
                    return;
                }

                double distanceSquared = context.player().squaredDistanceTo(
                    payload.tablePos().getX() + 0.5,
                    payload.tablePos().getY() + 0.5,
                    payload.tablePos().getZ() + 0.5
                );

                if (distanceSquared > 64.0) {
                    return;
                }

                ManufacturingRecipe recipe = ManufacturingRecipeRegistry.getRecipe(payload.recipeId());

                if (recipe == null) {
                    return;
                }

                ItemStack result = ManufacturingRecipeRegistry.tryCraft(inventory, recipe);

                if (!result.isEmpty()) {
                    if (!inventory.insertStack(result)) {
                        context.player().dropItem(result, false);
                    }
                    inventory.markDirty();
                    context.player().currentScreenHandler.sendContentUpdates();
                }
            });
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            SubmarinePacketHelper.clearPlayerData(handler.player.getUuid());
        });
    }
}