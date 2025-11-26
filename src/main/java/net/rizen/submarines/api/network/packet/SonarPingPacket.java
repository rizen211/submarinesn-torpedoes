package net.rizen.submarines.api.network.packet;

import net.rizen.submarines.Mod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Requests a sonar ping from the server. Sent when the player activates sonar.
 */
public record SonarPingPacket(int submarineId) implements CustomPayload {

    public static final CustomPayload.Id<SonarPingPacket> ID =
            new CustomPayload.Id<>(Identifier.of(Mod.MOD_ID, "sonar_ping"));

    public static final PacketCodec<RegistryByteBuf, SonarPingPacket> CODEC =
            new PacketCodec<RegistryByteBuf, SonarPingPacket>() {
                @Override
                public SonarPingPacket decode(RegistryByteBuf buf) {
                    return new SonarPingPacket(buf.readInt());
                }

                @Override
                public void encode(RegistryByteBuf buf, SonarPingPacket packet) {
                    buf.writeInt(packet.submarineId);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}