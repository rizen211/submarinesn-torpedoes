package net.rizen.submarines.api.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.rizen.submarines.api.client.input.SubmarineInputHandler;
import net.rizen.submarines.api.submarine.sonar.ContactType;
import net.rizen.submarines.api.submarine.sonar.SonarContact;
import net.rizen.submarines.api.submarine.sonar.SonarSystem;
import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.util.*;

/**
 * Renders the submarine HUD when a player is piloting one. This draws submarine stats like health and power,
 * torpedo information, and the circular sonar display with sweeping line and contact markers.
 */
public class SubmarineHud implements HudRenderCallback {
    private float clientSweepAngle = 0.0f;
    private static final float SWEEP_SPEED = 1.5f;

    public static void register() {
        HudRenderCallback.EVENT.register(new SubmarineHud());
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;

        Entity vehicle = client.player.getVehicle();
        if (!(vehicle instanceof BaseSubmarine submarine)) return;

        if (!SubmarineInputHandler.isSubmarineHudMode()) {
            return;
        }

        if (client.options.hudHidden) return;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        renderTorpedoInfo(drawContext, client, submarine, screenWidth, screenHeight);
        renderSubmarineStats(drawContext, client, submarine, screenWidth, screenHeight);

        if (SubmarineInputHandler.isSonarEnabled()) {
            renderSonarDisplay(drawContext, submarine, screenWidth, screenHeight, tickCounter.getTickDelta(true));
        }
    }

    private void renderSubmarineStats(DrawContext drawContext, MinecraftClient client,
                                      BaseSubmarine submarine, int screenWidth, int screenHeight) {
        int submarineWidth = Math.max(
                client.textRenderer.getWidth("SUBMARINE"),
                Math.max(
                        client.textRenderer.getWidth("Health: 100%"),
                        Math.max(
                                client.textRenderer.getWidth("Power: 100%"),
                                Math.max(
                                        client.textRenderer.getWidth("Depth: 999 b"),
                                        Math.max(
                                                client.textRenderer.getWidth("Speed: -99.9 b/s"),
                                                client.textRenderer.getWidth("Mode: Cruise")
                                        )
                                )
                        )
                )
        );

        int torpedoWidth = 120;

        int gap = 20;

        int startX = screenWidth - submarineWidth - gap;

        int startY = screenHeight - 125;

        float health = submarine.getHealth();
        float power = submarine.getPower();
        int depth = submarine.getDepth();
        float speed = submarine.getSpeed() * 20;

        int textY = startY;

        drawContext.drawText(client.textRenderer,
                Text.translatable("submarines.hud.submarine"),
                startX, textY, 0xFF00AAFF, true);

        drawContext.drawText(client.textRenderer,
                Text.translatable("submarines.hud.health", String.format("%.0f", health)),
                startX, textY + 12, getHealthColor(health), false);

        drawContext.drawText(client.textRenderer,
                Text.translatable("submarines.hud.power", String.format("%.0f", power)),
                startX, textY + 24, getPowerColor(power), false);

        drawContext.drawText(client.textRenderer,
                Text.translatable("submarines.hud.depth", depth),
                startX, textY + 36, 0xFFFFFFFF, false);

        drawContext.drawText(client.textRenderer,
                Text.translatable("submarines.hud.speed", String.format("%.1f", speed)),
                startX, textY + 48, 0xFFFFFFFF, false);

        drawContext.drawText(client.textRenderer,
                Text.translatable("submarines.hud.mode", submarine.getMovementMode().getDisplayName()),
                startX, textY + 60, 0xFF00FFFF, false);

        boolean sonarEnabled = SubmarineInputHandler.isSonarEnabled();
        int sonarColor = sonarEnabled ? 0xFF00FF00 : 0xFF888888;
        drawContext.drawText(client.textRenderer,
                Text.translatable(sonarEnabled ? "submarines.hud.sonar.active" : "submarines.hud.sonar.off"),
                startX, textY + 72, sonarColor, false);

        if (sonarEnabled) {
            drawContext.drawText(client.textRenderer,
                    Text.translatable("submarines.hud.sonar.ping_hint"),
                    startX, textY + 84, 0xFFAAAAAA, false);

            float cooldownProgress = SubmarineInputHandler.getSonarCooldownProgress();
            int barWidth = 100;
            int barHeight = 4;
            int barX = startX;
            int barY = textY + 96;

            drawContext.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);

            int fillWidth = (int) (barWidth * cooldownProgress);
            int barColor = cooldownProgress >= 1.0f ? 0xFF00FF00 : 0xFF888888;
            drawContext.fill(barX, barY, barX + fillWidth, barY + barHeight, barColor);
        }
    }

    private void renderTorpedoInfo(DrawContext drawContext, MinecraftClient client,
                                   BaseSubmarine submarine, int screenWidth, int screenHeight) {
        int torpedoWidth = 120;

        int gap = 20;

        int startX = gap;

        int startY = screenHeight - 125;

        int textY = startY;

        drawContext.drawText(client.textRenderer,
                Text.translatable("submarines.hud.torpedoes"),
                startX, textY, 0xFFFF4444, true);

        int torpedoCount = submarine.getTorpedoCount();
        Text countText = Text.translatable("submarines.hud.ammo", torpedoCount);
        int countColor = torpedoCount > 0 ? 0xFF00FF00 : 0xFFFF0000;
        drawContext.drawText(client.textRenderer, countText,
                startX, textY + 12, countColor, false);

        drawContext.drawText(client.textRenderer,
                Text.translatable("submarines.hud.target", submarine.getTargetingMode().getDisplayName()),
                startX, textY + 24, 0xFFFFAA00, false);

        int armingTimer = submarine.getTorpedoArmingTimer();
        boolean isArmed = submarine.isTorpedoArmed();

        textY += 36;

        if (torpedoCount == 0) {
            drawContext.drawText(client.textRenderer,
                    Text.translatable("submarines.hud.status.no_ammo"),
                    startX, textY, 0xFF888888, false);
        } else if (armingTimer > 0) {
            drawContext.drawText(client.textRenderer,
                    Text.translatable("submarines.hud.status.arming"),
                    startX, textY, 0xFFFFAA00, false);

            int barY = textY + 12;
            int barHeight = 6;

            drawContext.fill(startX, barY, startX + torpedoWidth, barY + barHeight, 0xFF440000);

            float progress = 1.0f - (armingTimer / 60.0f);
            int fillWidth = (int) (torpedoWidth * progress);
            drawContext.fill(startX, barY, startX + fillWidth, barY + barHeight, 0xFFFF0000);

            int progressPercent = (int) (progress * 100);
            String progressText = String.format("%d%%", progressPercent);
            int progressTextX = startX + (torpedoWidth / 2) - (client.textRenderer.getWidth(progressText) / 2);
            drawContext.drawText(client.textRenderer,
                    Text.literal(progressText),
                    progressTextX, barY + barHeight + 4, 0xFFFFFFFF, false);

        } else if (isArmed) {
            drawContext.drawText(client.textRenderer,
                    Text.translatable("submarines.hud.status.ready"),
                    startX, textY, 0xFF00FF00, true);

            drawContext.drawText(client.textRenderer,
                    Text.translatable("submarines.hud.fire_hint"),
                    startX, textY + 12, 0xFFAAAAAA, false);
        }
    }

    private int getHealthColor(float health) {
        if (health > 75) {
            return 0xFF00FF00;
        } else if (health > 50) {
            return 0xFFFFFF00;
        } else if (health > 25) {
            return 0xFFFF8800;
        } else {
            return 0xFFFF0000;
        }
    }

    private int getPowerColor(float power) {
        if (power > 50) {
            return 0xFF00FFFF;
        } else if (power > 25) {
            return 0xFFFFFF00;
        } else if (power > 10) {
            return 0xFFFF8800;
        } else {
            return 0xFFFF0000;
        }
    }

    private static final int RADAR_RADIUS = 45;
    private static final int RADAR_CENTER_X_OFFSET = 0;
    private static final int RADAR_CENTER_Y_OFFSET = -25;
    private static final float GRID_CIRCLE_COUNT = 3;

    private void renderSonarDisplay(DrawContext drawContext, BaseSubmarine submarine, int screenWidth, int screenHeight, float tickDelta) {
        SonarSystem sonarSystem = submarine.getSonarSystem();
        long currentTime = System.currentTimeMillis();

        clientSweepAngle += SWEEP_SPEED;
        if (clientSweepAngle >= 360.0f) {
            clientSweepAngle -= 360.0f;
        }

        int centerX = screenWidth / 2 + RADAR_CENTER_X_OFFSET;
        int centerY = screenHeight - RADAR_RADIUS - 20 + RADAR_CENTER_Y_OFFSET;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        renderRadarBackground(drawContext, centerX, centerY);
        renderGridCircles(drawContext, centerX, centerY);
        renderTerrainArcs(drawContext, submarine, centerX, centerY, currentTime);
        renderContactBlips(drawContext, submarine, centerX, centerY, currentTime);
        renderSweepLine(drawContext, centerX, centerY, clientSweepAngle, tickDelta);
        renderCenterDot(drawContext, centerX, centerY);

        RenderSystem.disableBlend();
    }

    private void renderRadarBackground(DrawContext drawContext, int centerX, int centerY) {
        int color = 0x40000000;
        drawFilledCircle(drawContext, centerX, centerY, RADAR_RADIUS, color);

        int borderColor = 0xFF00FF00;
        drawCircleOutline(drawContext, centerX, centerY, RADAR_RADIUS, borderColor, 2.0f);
    }

    private void renderGridCircles(DrawContext drawContext, int centerX, int centerY) {
        int gridColor = 0x4000FF00;
        for (int i = 1; i <= GRID_CIRCLE_COUNT; i++) {
            int radius = (int) (RADAR_RADIUS * (i / GRID_CIRCLE_COUNT));
            drawCircleOutline(drawContext, centerX, centerY, radius, gridColor, 1.0f);
        }
    }

    private void renderSweepLine(DrawContext drawContext, int centerX, int centerY, float sweepAngle, float tickDelta) {
        float angleRad = (float) Math.toRadians(sweepAngle);

        int endX = centerX + (int) (Math.sin(angleRad) * RADAR_RADIUS);
        int endY = centerY - (int) (Math.cos(angleRad) * RADAR_RADIUS);

        int sweepColor = 0x8000FF00;
        drawLine(drawContext, centerX, centerY, endX, endY, sweepColor, 2.0f);

        for (int i = -10; i < 0; i += 2) {
            float glowAngle = sweepAngle + i * 1.5f;
            float glowAngleRad = (float) Math.toRadians(glowAngle);
            int glowEndX = centerX + (int) (Math.sin(glowAngleRad) * RADAR_RADIUS);
            int glowEndY = centerY - (int) (Math.cos(glowAngleRad) * RADAR_RADIUS);

            int alpha = (int) (40 * (1.0f + i / 10.0f));
            int glowColorWithAlpha = (alpha << 24) | 0x00FF00;
            drawLine(drawContext, centerX, centerY, glowEndX, glowEndY, glowColorWithAlpha, 1.0f);
        }
    }

    private void renderCenterDot(DrawContext drawContext, int centerX, int centerY) {
        int dotColor = 0xFFFF0000;
        drawFilledCircle(drawContext, centerX, centerY, 2, dotColor);
    }

    private void renderContactBlips(DrawContext drawContext, BaseSubmarine submarine, int centerX, int centerY, long currentTime) {
        SonarSystem sonarSystem = submarine.getSonarSystem();
        List<SonarContact> contacts = sonarSystem.getContacts();

        for (SonarContact contact : contacts) {
            if (contact.getType() == ContactType.TERRAIN) {
                continue;
            }

            if (!contact.isRevealed() && isAngleInSweep(contact.getAngle(), clientSweepAngle)) {
                contact.reveal(currentTime);
            }

            if (!contact.isRevealed()) {
                continue;
            }

            float alpha = contact.getFadeAlpha(currentTime);
            if (alpha <= 0.0f) {
                continue;
            }

            double distance = contact.getDistance();
            double normalizedDistance = Math.min(distance / sonarSystem.getMaxRange(), 1.0);
            int blipRadius = (int) (normalizedDistance * RADAR_RADIUS);

            float angleRad = (float) Math.toRadians(contact.getAngle());
            int blipX = centerX + (int) (Math.sin(angleRad) * blipRadius);
            int blipY = centerY - (int) (Math.cos(angleRad) * blipRadius);

            int blipSize = getBlipSize(contact.getType());
            int blipColor = getBlipColor(contact.getType(), alpha);

            drawFilledCircle(drawContext, blipX, blipY, blipSize, blipColor);
        }
    }

    private boolean isAngleInSweep(float contactAngle, float sweepAngle) {
        float tolerance = SWEEP_SPEED + 2.0f;
        float diff = Math.abs(normalizeAngle(sweepAngle - contactAngle));
        return diff <= tolerance;
    }

    private float normalizeAngle(float angle) {
        while (angle > 180.0f) angle -= 360.0f;
        while (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    private void renderTerrainArcs(DrawContext drawContext, BaseSubmarine submarine, int centerX, int centerY, long currentTime) {
        SonarSystem sonarSystem = submarine.getSonarSystem();
        List<SonarContact> contacts = sonarSystem.getContacts();

        Map<Integer, List<SonarContact>> terrainByAngleBucket = new HashMap<>();
        for (SonarContact contact : contacts) {
            if (contact.getType() != ContactType.TERRAIN) {
                continue;
            }

            if (!contact.isRevealed() && isAngleInSweep(contact.getAngle(), clientSweepAngle)) {
                contact.reveal(currentTime);
            }

            if (!contact.isRevealed()) {
                continue;
            }

            int angleBucket = (int) (contact.getAngle() / 2) * 2;
            terrainByAngleBucket.computeIfAbsent(angleBucket, k -> new ArrayList<>()).add(contact);
        }

        for (Map.Entry<Integer, List<SonarContact>> entry : terrainByAngleBucket.entrySet()) {
            SonarContact closestContact = entry.getValue().stream()
                    .min(Comparator.comparingDouble(SonarContact::getDistance))
                    .orElse(null);

            if (closestContact == null) {
                continue;
            }

            float alpha = closestContact.getFadeAlpha(currentTime);
            if (alpha <= 0.0f) {
                continue;
            }

            double distance = closestContact.getDistance();
            double normalizedDistance = Math.min(distance / sonarSystem.getMaxRange(), 1.0);
            int arcRadius = (int) (normalizedDistance * RADAR_RADIUS);

            float startAngle = closestContact.getAngle() - 4.0f;
            float endAngle = closestContact.getAngle() + 4.0f;

            int arcColor = getTerrainArcColor(alpha);
            drawArc(drawContext, centerX, centerY, arcRadius, startAngle, endAngle, arcColor, 4.0f);
        }
    }

    private int getBlipSize(ContactType type) {
        return switch (type) {
            case SMALL_MOB -> 2;
            case MEDIUM_ENTITY -> 3;
            case PLAYER -> 3;
            case ITEM -> 1;
            case SUBMARINE -> 5;
            default -> 2;
        };
    }

    private int getBlipColor(ContactType type, float alpha) {
        int alphaInt = (int) (alpha * 255);
        return switch (type) {
            case SMALL_MOB -> (alphaInt << 24) | 0x00FFFF;
            case MEDIUM_ENTITY -> (alphaInt << 24) | 0xFFFF00;
            case PLAYER -> (alphaInt << 24) | 0xAA00FF;
            case ITEM -> (alphaInt << 24) | 0xFFFFFF;
            case SUBMARINE -> (alphaInt << 24) | 0xFF0000;
            default -> (alphaInt << 24) | 0xFFFFFF;
        };
    }

    private int getTerrainArcColor(float alpha) {
        int alphaInt = (int) (alpha * 255);
        return (alphaInt << 24) | 0x88FF88;
    }

    private void drawFilledCircle(DrawContext drawContext, int centerX, int centerY, int radius, int color) {
        Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, centerX, centerY, 0).color(r, g, b, a);

        int segments = Math.max(32, radius * 2);
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = centerX + (float) (Math.cos(angle) * radius);
            float y = centerY + (float) (Math.sin(angle) * radius);
            buffer.vertex(matrix, x, y, 0).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    private void drawCircleOutline(DrawContext drawContext, int centerX, int centerY, int radius, int color, float thickness) {
        Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(thickness);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        int segments = Math.max(64, radius * 2);
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            float x = centerX + (float) (Math.cos(angle) * radius);
            float y = centerY + (float) (Math.sin(angle) * radius);
            buffer.vertex(matrix, x, y, 0).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.lineWidth(1.0f);
    }

    private void drawLine(DrawContext drawContext, int x1, int y1, int x2, int y2, int color, float thickness) {
        Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(thickness);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x1, y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, 0).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.lineWidth(1.0f);
    }

    private void drawArc(DrawContext drawContext, int centerX, int centerY, int radius, float startAngleDeg, float endAngleDeg, int color, float thickness) {
        Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(thickness);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        int segments = (int) Math.abs(endAngleDeg - startAngleDeg);
        segments = Math.max(segments, 3);

        for (int i = 0; i <= segments; i++) {
            float angle = startAngleDeg + (endAngleDeg - startAngleDeg) * i / segments;
            float angleRad = (float) Math.toRadians(angle);

            float x = centerX + (float) (Math.sin(angleRad) * radius);
            float y = centerY - (float) (Math.cos(angleRad) * radius);

            buffer.vertex(matrix, x, y, 0).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.lineWidth(1.0f);
    }
}