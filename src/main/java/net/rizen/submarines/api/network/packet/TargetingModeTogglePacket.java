package net.rizen.submarines.api.network.packet;

import net.rizen.submarines.Mod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Tells the server to cycle the torpedo targeting mode.
 */
public record TargetingModeTogglePacket(int submarineId) implements CustomPayload {

    public static final CustomPayload.Id<TargetingModeTogglePacket> ID =
            new CustomPayload.Id<>(Identifier.of(Mod.MOD_ID, "targeting_mode_toggle"));

    public static final PacketCodec<RegistryByteBuf, TargetingModeTogglePacket> CODEC =
            new PacketCodec<RegistryByteBuf, TargetingModeTogglePacket>() {
                @Override
                public TargetingModeTogglePacket decode(RegistryByteBuf buf) {
                    return new TargetingModeTogglePacket(buf.readInt());
                }

                @Override
                public void encode(RegistryByteBuf buf, TargetingModeTogglePacket packet) {
                    buf.writeInt(packet.submarineId);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
