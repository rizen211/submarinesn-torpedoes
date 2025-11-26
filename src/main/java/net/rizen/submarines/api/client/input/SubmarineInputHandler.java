package net.rizen.submarines.api.client.input;

import net.minecraft.text.Text;
import net.rizen.submarines.api.network.packet.SubmarineInputPacket;
import net.rizen.submarines.api.network.packet.TorpedoFirePacket;
import net.rizen.submarines.api.network.packet.DismountPacket;
import net.rizen.submarines.api.network.packet.SonarPingPacket;
import net.rizen.submarines.api.network.packet.MovementModeTogglePacket;
import net.rizen.submarines.api.network.packet.TargetingModeTogglePacket;
import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Captures player input when piloting a submarine and sends it to the server. This registers all the submarine
 * keybindings and monitors when they are pressed, then sends network packets to tell the server what the player
 * is doing.
 */
public class SubmarineInputHandler {
    private static KeyBinding dismountKey;
    private static KeyBinding sonarToggleKey;
    private static KeyBinding sonarPingKey;
    private static KeyBinding hudModeToggleKey;
    private static KeyBinding movementModeToggleKey;
    private static KeyBinding targetingModeToggleKey;

    private static boolean wasForward = false;
    private static boolean wasBackward = false;
    private static boolean wasLeft = false;
    private static boolean wasRight = false;
    private static boolean wasUp = false;
    private static boolean wasDown = false;
    private static boolean wasLeftMousePressed = false;

    private static boolean sonarEnabled = false;
    private static long lastSonarPingTime = 0;
    private static final long SONAR_PING_COOLDOWN = 3000;

    private static boolean submarineHudMode = true;

    public static void register() {
        dismountKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.submarines.dismount",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category.submarines"
        ));

        sonarToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.submarines.sonar_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category.submarines"
        ));

        sonarPingKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.submarines.sonar_ping",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.submarines"
        ));

        hudModeToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.submarines.hud_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,
                "category.submarines"
        ));

        movementModeToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.submarines.movement_mode_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.submarines"
        ));

        targetingModeToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.submarines.targeting_mode_toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.submarines"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.hasVehicle() && client.player.getVehicle() instanceof BaseSubmarine submarine) {
                handleSubmarineInput(client, submarine);
            } else {
                wasLeftMousePressed = false;
                submarineHudMode = true;
            }
        });
    }

    private static void handleSubmarineInput(MinecraftClient client, BaseSubmarine submarine) {
        if (hudModeToggleKey.wasPressed()) {
            submarineHudMode = !submarineHudMode;
            client.player.sendMessage(
                Text.translatable(submarineHudMode ? "submarines.hud.submarine_mode" : "submarines.hud.player_mode"),
                true
            );
        }

        if (dismountKey.wasPressed()) {
            DismountPacket packet = new DismountPacket(submarine.getId());
            ClientPlayNetworking.send(packet);
            return;
        }

        if (sonarToggleKey.wasPressed()) {
            if (submarineHudMode) {
                sonarEnabled = !sonarEnabled;
                client.player.sendMessage(
                    Text.translatable(sonarEnabled ? "submarines.sonar.enabled" : "submarines.sonar.disabled"),
                    true
                );
            }
        }

        if (movementModeToggleKey.wasPressed()) {
            if (submarineHudMode) {
                MovementModeTogglePacket packet = new MovementModeTogglePacket(submarine.getId());
                ClientPlayNetworking.send(packet);
                client.player.sendMessage(
                    Text.translatable("submarines.movement_mode.changed", submarine.getMovementMode().next().getDisplayName()),
                    true
                );
            }
        }

        if (targetingModeToggleKey.wasPressed()) {
            if (submarineHudMode) {
                TargetingModeTogglePacket packet = new TargetingModeTogglePacket(submarine.getId());
                ClientPlayNetworking.send(packet);
                client.player.sendMessage(
                    Text.translatable("submarines.targeting_mode.changed", submarine.getTargetingMode().next().getDisplayName()),
                    true
                );
            }
        }

        if (sonarPingKey.wasPressed()) {
            if (submarineHudMode && sonarEnabled) {
                long currentTime = System.currentTimeMillis();
                long timeSinceLastPing = currentTime - lastSonarPingTime;

                if (timeSinceLastPing >= SONAR_PING_COOLDOWN) {
                    if (submarine.getPower() >= 2.0f) {
                        submarine.performSonarPing();

                        SonarPingPacket packet = new SonarPingPacket(submarine.getId());
                        ClientPlayNetworking.send(packet);

                        client.player.playSound(net.rizen.submarines.Mod.SONAR_PING, 1.0f, 1.0f);
                        lastSonarPingTime = currentTime;
                    } else {
                        client.player.sendMessage(
                            Text.translatable("submarines.sonar.insufficient_power"),
                            true
                        );
                    }
                } else {
                    long remainingCooldown = (SONAR_PING_COOLDOWN - timeSinceLastPing) / 1000;
                    client.player.sendMessage(
                        Text.translatable("submarines.sonar.recharging", (remainingCooldown + 1)),
                        true
                    );
                }
            }
        }

        if (!submarineHudMode) {
            return;
        }

        if (client.currentScreen == null) {
            boolean leftMousePressed = GLFW.glfwGetMouseButton(
                    client.getWindow().getHandle(),
                    GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) == GLFW.GLFW_PRESS;

            if (leftMousePressed && !wasLeftMousePressed) {
                TorpedoFirePacket packet = new TorpedoFirePacket(submarine.getId());
                ClientPlayNetworking.send(packet);
            }
            wasLeftMousePressed = leftMousePressed;
        } else {
            wasLeftMousePressed = false;
        }

        boolean forward = client.options.forwardKey.isPressed();
        boolean backward = client.options.backKey.isPressed();
        boolean left = client.options.leftKey.isPressed();
        boolean right = client.options.rightKey.isPressed();

        boolean up = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT);
        boolean down = InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL);

        boolean inputChanged = forward != wasForward || backward != wasBackward ||
                left != wasLeft || right != wasRight ||
                up != wasUp || down != wasDown;

        if (inputChanged || client.player.age % 5 == 0) {
            SubmarineInputPacket packet = new SubmarineInputPacket(
                    submarine.getId(), forward, backward, left, right, up, down
            );
            ClientPlayNetworking.send(packet);

            wasForward = forward;
            wasBackward = backward;
            wasLeft = left;
            wasRight = right;
            wasUp = up;
            wasDown = down;
        }
    }

    public static boolean isSonarEnabled() {
        return sonarEnabled;
    }

    public static boolean isSubmarineHudMode() {
        return submarineHudMode;
    }
}