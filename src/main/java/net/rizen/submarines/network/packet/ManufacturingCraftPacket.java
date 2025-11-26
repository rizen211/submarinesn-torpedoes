package net.rizen.submarines.network.packet;

import net.rizen.submarines.Mod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ManufacturingCraftPacket(Identifier recipeId, BlockPos tablePos) implements CustomPayload {

    public static final CustomPayload.Id<ManufacturingCraftPacket> ID =
            new CustomPayload.Id<>(Identifier.of(Mod.MOD_ID, "manufacturing_craft"));

    public static final PacketCodec<RegistryByteBuf, ManufacturingCraftPacket> CODEC =
            new PacketCodec<RegistryByteBuf, ManufacturingCraftPacket>() {
                @Override
                public ManufacturingCraftPacket decode(RegistryByteBuf buf) {
                    Identifier recipeId = buf.readIdentifier();
                    BlockPos tablePos = buf.readBlockPos();
                    return new ManufacturingCraftPacket(recipeId, tablePos);
                }

                @Override
                public void encode(RegistryByteBuf buf, ManufacturingCraftPacket packet) {
                    buf.writeIdentifier(packet.recipeId);
                    buf.writeBlockPos(packet.tablePos);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
