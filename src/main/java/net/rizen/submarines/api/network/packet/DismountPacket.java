package net.rizen.submarines.api.network.packet;

import net.rizen.submarines.Mod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Tells the server the player wants to exit the submarine. Sent when the dismount key is pressed.
 */
public record DismountPacket(int submarineId) implements CustomPayload {

    public static final CustomPayload.Id<DismountPacket> ID =
            new CustomPayload.Id<>(Identifier.of(Mod.MOD_ID, "submarine_dismount"));

    public static final PacketCodec<RegistryByteBuf, DismountPacket> CODEC =
            new PacketCodec<RegistryByteBuf, DismountPacket>() {
                @Override
                public DismountPacket decode(RegistryByteBuf buf) {
                    return new DismountPacket(buf.readInt());
                }

                @Override
                public void encode(RegistryByteBuf buf, DismountPacket packet) {
                    buf.writeInt(packet.submarineId);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}