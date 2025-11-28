package net.rizen.submarines.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.rizen.submarines.api.submarine.BaseSubmarine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SubmarinePacketHelper {
    private static final Map<UUID, Map<String, Long>> lastPacketTimes = new ConcurrentHashMap<>();

    private static final long DEFAULT_COOLDOWN_MS = 50;

    private static final Map<String, Long> PACKET_COOLDOWNS = Map.of(
        "input", 50L,
        "torpedo_fire", 500L,
        "sonar_ping", 1000L,
        "mode_toggle", 200L,
        "dismount", 100L
    );

    public static void withValidatedSubmarine(
            ServerPlayNetworking.Context context,
            int entityId,
            String packetType,
            Consumer<BaseSubmarine> action
    ) {
        UUID playerId = context.player().getUuid();

        if (!checkCooldown(playerId, packetType)) {
            return;
        }

        context.player().server.execute(() -> {
            Entity entity = context.player().getWorld().getEntityById(entityId);
            if (entity instanceof BaseSubmarine submarine) {
                if (submarine.getPassengerList().contains(context.player())) {
                    action.accept(submarine);
                }
            }
        });
    }

    private static boolean checkCooldown(UUID playerId, String packetType) {
        long currentTime = System.currentTimeMillis();
        long cooldown = PACKET_COOLDOWNS.getOrDefault(packetType, DEFAULT_COOLDOWN_MS);

        Map<String, Long> playerPacketTimes = lastPacketTimes.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());

        Long lastTime = playerPacketTimes.get(packetType);
        if (lastTime != null && currentTime - lastTime < cooldown) {
            return false;
        }

        playerPacketTimes.put(packetType, currentTime);

        if (Math.random() < 0.001) {
            cleanup(currentTime);
        }

        return true;
    }

    private static void cleanup(long currentTime) {
        lastPacketTimes.values().forEach(playerTimes ->
            playerTimes.entrySet().removeIf(entry -> currentTime - entry.getValue() > 5000)
        );
        lastPacketTimes.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public static void clearPlayerData(UUID playerId) {
        lastPacketTimes.remove(playerId);
    }
}
