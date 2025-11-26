package net.rizen.submarines.api.network.packet;

import net.rizen.submarines.Mod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Tells the server to fire a torpedo from a submarine. Sent when the player presses the fire button.
 */
public record TorpedoFirePacket(int submarineId) implements CustomPayload {

    public static final CustomPayload.Id<TorpedoFirePacket> ID =
            new CustomPayload.Id<>(Identifier.of(Mod.MOD_ID, "torpedo_fire"));

    public static final PacketCodec<RegistryByteBuf, TorpedoFirePacket> CODEC =
            new PacketCodec<RegistryByteBuf, TorpedoFirePacket>() {
                @Override
                public TorpedoFirePacket decode(RegistryByteBuf buf) {
                    return new TorpedoFirePacket(buf.readInt());
                }

                @Override
                public void encode(RegistryByteBuf buf, TorpedoFirePacket packet) {
                    buf.writeInt(packet.submarineId);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}