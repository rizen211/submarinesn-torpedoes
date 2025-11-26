package net.rizen.submarines.api.network.packet;

import net.rizen.submarines.Mod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Sends player input from client to server for submarine control. This packet gets sent every tick while
 * a player is piloting a submarine, telling the server which movement keys are being pressed.
 */
public record SubmarineInputPacket(
        int entityId,
        boolean forward,
        boolean backward,
        boolean left,
        boolean right,
        boolean up,
        boolean down
) implements CustomPayload {

    public static final CustomPayload.Id<SubmarineInputPacket> ID =
            new CustomPayload.Id<>(Identifier.of(Mod.MOD_ID, "submarine_input"));

    public static final PacketCodec<RegistryByteBuf, SubmarineInputPacket> CODEC =
            new PacketCodec<RegistryByteBuf, SubmarineInputPacket>() {
                @Override
                public SubmarineInputPacket decode(RegistryByteBuf buf) {
                    return new SubmarineInputPacket(
                            buf.readInt(),
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readBoolean(),
                            buf.readBoolean()
                    );
                }

                @Override
                public void encode(RegistryByteBuf buf, SubmarineInputPacket packet) {
                    buf.writeInt(packet.entityId);
                    buf.writeBoolean(packet.forward);
                    buf.writeBoolean(packet.backward);
                    buf.writeBoolean(packet.left);
                    buf.writeBoolean(packet.right);
                    buf.writeBoolean(packet.up);
                    buf.writeBoolean(packet.down);
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}