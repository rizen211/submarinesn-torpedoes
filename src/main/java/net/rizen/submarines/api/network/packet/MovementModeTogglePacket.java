package net.rizen.submarines.api.network.packet;

import net.rizen.submarines.Mod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Tells the server to cycle the submarine movement mode.
 */
public record MovementModeTogglePacket(int submarineId) implements CustomPayload {

    public static final CustomPayload.Id<MovementModeTogglePacket> ID =
            new CustomPayload.Id<>(Identifier.of(Mod.MOD_ID, "movement_mode_toggle"));

    public static final PacketCodec<RegistryByteBuf, MovementModeTogglePacket> CODEC =
            new PacketCodec<RegistryByteBuf, MovementModeTogglePacket>() {
                @Override
                public MovementModeTogglePacket decode(RegistryByteBuf buf) {
                    return new MovementModeTogglePacket(buf.readInt());
                }

                @Override
                public void encode(RegistryByteBuf buf, MovementModeTogglePacket packet) {
                    buf.writeInt(packet.submarineId);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
