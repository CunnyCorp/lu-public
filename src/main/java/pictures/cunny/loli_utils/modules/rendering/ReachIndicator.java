package pictures.cunny.loli_utils.modules.rendering;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.modules.LoliModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReachIndicator extends LoliModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgReach = settings.createGroup("Reach Settings");
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgTracers = settings.createGroup("Tracers");
    private final SettingGroup sgSphere = settings.createGroup("Reach Sphere");
    private final SettingGroup sgEffects = settings.createGroup("Special Effects");

    // General Settings
    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("max-distance")
            .description("Maximum distance to render reach indicators.")
            .defaultValue(64.0)
            .min(1.0)
            .max(128.0)
            .sliderMax(100.0)
            .build()
    );

    private final Setting<Boolean> showSelf = sgGeneral.add(new BoolSetting.Builder()
            .name("show-self")
            .description("Show your own reach indicator.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> showInvisible = sgGeneral.add(new BoolSetting.Builder()
            .name("show-invisible")
            .description("Show reach for invisible players.")
            .defaultValue(true)
            .build()
    );

    private final Setting<List<String>> excludedPlayers = sgGeneral.add(new StringListSetting.Builder()
            .name("excluded-players")
            .description("Players to exclude from reach indicators.")
            .defaultValue(new ArrayList<>())
            .build()
    );

    // Reach Settings
    private final Setting<Double> reachDistance = sgReach.add(new DoubleSetting.Builder()
            .name("reach-distance")
            .description("The reach distance to display (blocks).")
            .defaultValue(3.0)
            .min(1.0)
            .max(6.0)
            .sliderMin(1.0)
            .sliderMax(6.0)
            .build()
    );

    private final Setting<Boolean> showHitbox = sgReach.add(new BoolSetting.Builder()
            .name("show-hitbox")
            .description("Show the actual hitbox area where players can attack.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> hitboxHeight = sgReach.add(new DoubleSetting.Builder()
            .name("hitbox-height")
            .description("Height of the hitbox area.")
            .defaultValue(1.8)
            .min(0.5)
            .max(3.0)
            .visible(showHitbox::get)
            .build()
    );

    // Color Settings
    private final Setting<SettingColor> defaultColor = sgColors.add(new ColorSetting.Builder()
            .name("default-color")
            .description("Default color for player reach.")
            .defaultValue(new SettingColor(255, 50, 50, 150))
            .build()
    );

    private final Setting<SettingColor> friendColor = sgColors.add(new ColorSetting.Builder()
            .name("friend-color")
            .description("Color for friends' reach.")
            .defaultValue(new SettingColor(50, 255, 50, 150))
            .build()
    );

    private final Setting<SettingColor> teamColor = sgColors.add(new ColorSetting.Builder()
            .name("team-color")
            .description("Color for teammates' reach.")
            .defaultValue(new SettingColor(50, 50, 255, 150))
            .build()
    );

    private final Setting<SettingColor> selfColor = sgColors.add(new ColorSetting.Builder()
            .name("self-color")
            .description("Color for your own reach.")
            .defaultValue(new SettingColor(200, 200, 200, 100))
            .build()
    );

    // Tracer Settings
    private final Setting<Boolean> enableTracers = sgTracers.add(new BoolSetting.Builder()
            .name("enable-tracers")
            .description("Enable enhanced tracers to reach spheres.")
            .defaultValue(true)
            .build()
    );

    private final Setting<TracerStyle> tracerStyle = sgTracers.add(new EnumSetting.Builder<TracerStyle>()
            .name("tracer-style")
            .description("Visual style of the tracers.")
            .defaultValue(TracerStyle.Beam)
            .visible(enableTracers::get)
            .build()
    );

    private final Setting<Double> tracerWidth = sgTracers.add(new DoubleSetting.Builder()
            .name("tracer-width")
            .description("Width of the tracers.")
            .defaultValue(0.05)
            .min(0.01)
            .max(0.2)
            .visible(enableTracers::get)
            .build()
    );

    private final Setting<Boolean> tracerPulse = sgTracers.add(new BoolSetting.Builder()
            .name("tracer-pulse")
            .description("Make tracers pulse with energy.")
            .defaultValue(true)
            .visible(enableTracers::get)
            .build()
    );

    // Sphere Settings
    private final Setting<SphereStyle> sphereStyle = sgSphere.add(new EnumSetting.Builder<SphereStyle>()
            .name("sphere-style")
            .description("Visual style of the reach sphere.")
            .defaultValue(SphereStyle.Wireframe)
            .build()
    );

    private final Setting<Integer> sphereResolution = sgSphere.add(new IntSetting.Builder()
            .name("sphere-resolution")
            .description("Resolution of the sphere (more = smoother but heavier).")
            .defaultValue(16)
            .min(8)
            .max(32)
            .build()
    );

    private final Setting<Boolean> spherePulse = sgSphere.add(new BoolSetting.Builder()
            .name("sphere-pulse")
            .description("Make the sphere pulse with energy.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> spherePulseSpeed = sgSphere.add(new DoubleSetting.Builder()
            .name("sphere-pulse-speed")
            .description("Speed of the sphere pulse.")
            .defaultValue(1.5)
            .min(0.5)
            .max(3.0)
            .visible(spherePulse::get)
            .build()
    );

    // Special Effects
    private final Setting<Boolean> enableEnergyField = sgEffects.add(new BoolSetting.Builder()
            .name("enable-energy-field")
            .description("Add energy field effect at reach boundary.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> energyIntensity = sgEffects.add(new DoubleSetting.Builder()
            .name("energy-intensity")
            .description("Intensity of the energy field.")
            .defaultValue(1.2)
            .min(0.5)
            .max(2.5)
            .visible(enableEnergyField::get)
            .build()
    );

    private final Setting<Boolean> enableDangerZone = sgEffects.add(new BoolSetting.Builder()
            .name("enable-danger-zone")
            .description("Highlight when you're in another player's reach.")
            .defaultValue(true)
            .build()
    );

    private final Setting<SettingColor> dangerColor = sgEffects.add(new ColorSetting.Builder()
            .name("danger-color")
            .description("Color for danger zone indication.")
            .defaultValue(new SettingColor(255, 0, 0, 100))
            .visible(enableDangerZone::get)
            .build()
    );

    // Animation state
    private double animationProgress = 0;
    private final Map<Player, Boolean> playerInReach = new HashMap<>();

    public ReachIndicator() {
        super(LoliUtilsMeteor.CATEGORY, "reach-indicator", "Shows player reach distance with enhanced visual effects.");
    }

    @Override
    public void safeOnActivate() {
        animationProgress = 0;
        playerInReach.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        animationProgress += 0.05;
        if (animationProgress > 100) animationProgress = 0;

        if (mc.level != null && mc.player != null && enableDangerZone.get()) {
            playerInReach.clear();
            for (Player player : mc.level.players()) {
                if (shouldSkipPlayer(player) || player == mc.player) continue;

                double distance = mc.player.distanceTo(player);
                double playerReach = getReachDistance(player);

                playerInReach.put(player, distance <= playerReach);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (AbstractClientPlayer player : mc.level.players()) {
            if (shouldSkipPlayer(player)) continue;

            double distance = mc.player.distanceTo(player);
            if (distance > maxDistance.get()) continue;

            Color color = getPlayerColor(player);
            double reach = getReachDistance(player);

            renderReachIndicator(event, player, color, reach);

            if (enableTracers.get()) {
                renderEnhancedTracer(event, player, color, reach);
            }

            if (enableDangerZone.get() && player != mc.player &&
                    playerInReach.getOrDefault(player, false)) {
                renderDangerZone(event, player, reach);
            }
        }
    }

    private boolean shouldSkipPlayer(Player player) {
        if (player == mc.player && !showSelf.get()) return true;
        if (!player.isAlive()) return true;
        if (player.isInvisible() && !showInvisible.get()) return true;
        return excludedPlayers.get().contains(player.getGameProfile().getName());
    }

    private Color getPlayerColor(Player player) {
        if (player == mc.player) return selfColor.get();

        // Check if player is on the same team
        if (player.getTeam() != null && mc.player.getTeam() != null &&
                player.getTeam().isAlliedTo(mc.player.getTeam())) {
            return teamColor.get();
        }

        // Check if player is a friend
        if (Friends.get().isFriend(player)) {
            return friendColor.get();
        }

        return defaultColor.get();
    }

    private double getReachDistance(Player player) {
        return reachDistance.get();
    }

    private void renderReachIndicator(Render3DEvent event, Player player, Color color, double reach) {
        Vec3 eyePos = player.getEyePosition(1.0f);

        renderReachSphere(event, eyePos, reach, color);

        if (showHitbox.get()) {
            renderHitbox(event, player, eyePos, reach, color);
        }

        if (enableEnergyField.get()) {
            renderEnergyField(event, eyePos, reach, color);
        }
    }

    private void renderReachSphere(Render3DEvent event, Vec3 center, double radius, Color color) {
        double pulse = spherePulse.get() ?
                (Math.sin(animationProgress * spherePulseSpeed.get()) + 1) * 0.5 + 0.5 : 1.0;

        double effectiveRadius = radius * pulse;

        switch (sphereStyle.get()) {
            case Wireframe:
                renderWireframeSphere(event, center, effectiveRadius, color);
                break;
            case Transparent:
                renderTransparentSphere(event, center, effectiveRadius, color);
                break;
            case Energy:
                renderEnergySphere(event, center, effectiveRadius, color);
                break;
        }
    }

    private void renderWireframeSphere(Render3DEvent event, Vec3 center, double radius, Color color) {
        int resolution = sphereResolution.get();

        // Render latitude circles
        for (int i = 0; i < resolution; i++) {
            double theta = Math.PI * i / resolution;
            double circleRadius = radius * Math.sin(theta);
            double y = center.y + radius * Math.cos(theta);

            renderCircle(event, center.x, y, center.z, circleRadius, resolution, color);
        }

        // Render longitude circles
        for (int i = 0; i < resolution; i++) {
            double phi = 2 * Math.PI * i / resolution;
            renderVerticalCircle(event, center, radius, phi, resolution, color);
        }
    }

    private void renderTransparentSphere(Render3DEvent event, Vec3 center, double radius, Color color) {
        int resolution = sphereResolution.get();
        Color transparentColor = new Color(color.r, color.g, color.b, color.a / 3);

        // Create sphere using quads for transparency
        for (int i = 0; i < resolution; i++) {
            double theta1 = Math.PI * i / resolution;
            double theta2 = Math.PI * (i + 1) / resolution;

            for (int j = 0; j < resolution; j++) {
                double phi1 = 2 * Math.PI * j / resolution;
                double phi2 = 2 * Math.PI * (j + 1) / resolution;

                // Calculate vertices for this quad
                Vec3 v1 = getSpherePoint(center, radius, theta1, phi1);
                Vec3 v2 = getSpherePoint(center, radius, theta1, phi2);
                Vec3 v3 = getSpherePoint(center, radius, theta2, phi2);
                Vec3 v4 = getSpherePoint(center, radius, theta2, phi1);

                // Render the quad
                event.renderer.triangles.ensureQuadCapacity();
                event.renderer.triangles.quad(
                        event.renderer.triangles.vec3(v1.x, v1.y, v1.z).color(transparentColor).next(),
                        event.renderer.triangles.vec3(v2.x, v2.y, v2.z).color(transparentColor).next(),
                        event.renderer.triangles.vec3(v3.x, v3.y, v3.z).color(transparentColor).next(),
                        event.renderer.triangles.vec3(v4.x, v4.y, v4.z).color(transparentColor).next()
                );
            }
        }
    }

    private void renderEnergySphere(Render3DEvent event, Vec3 center, double radius, Color color) {
        int resolution = sphereResolution.get();
        double pulse = (Math.sin(animationProgress * 2) + 1) * 0.5;

        // Create energy sphere with pulsating effect
        for (int i = 0; i < resolution; i++) {
            double theta = Math.PI * i / resolution;
            double energyRadius = radius * (0.9 + pulse * 0.1);

            // Vary color intensity
            int alpha = (int) (color.a * (0.7 + 0.3 * Math.sin(animationProgress + i * 0.5)));
            Color energyColor = new Color(color.r, color.g, color.b, alpha);

            renderCircle(event, center.x, center.y, center.z, energyRadius, resolution, energyColor);
        }
    }

    private void renderCircle(Render3DEvent event, double x, double y, double z, double radius, int segments, Color color) {
        double angleIncrement = 2 * Math.PI / segments;

        for (int i = 0; i < segments; i++) {
            double angle1 = i * angleIncrement;
            double angle2 = (i + 1) * angleIncrement;

            double x1 = x + radius * Math.cos(angle1);
            double z1 = z + radius * Math.sin(angle1);
            double x2 = x + radius * Math.cos(angle2);
            double z2 = z + radius * Math.sin(angle2);

            event.renderer.line(x1, y, z1, x2, y, z2, color);
        }
    }

    private void renderVerticalCircle(Render3DEvent event, Vec3 center, double radius, double phi, int segments, Color color) {
        double angleIncrement = Math.PI / segments;

        for (int i = 0; i < segments; i++) {
            double theta1 = i * angleIncrement;
            double theta2 = (i + 1) * angleIncrement;

            double x1 = center.x + radius * Math.sin(theta1) * Math.cos(phi);
            double y1 = center.y + radius * Math.cos(theta1);
            double z1 = center.z + radius * Math.sin(theta1) * Math.sin(phi);

            double x2 = center.x + radius * Math.sin(theta2) * Math.cos(phi);
            double y2 = center.y + radius * Math.cos(theta2);
            double z2 = center.z + radius * Math.sin(theta2) * Math.sin(phi);

            event.renderer.line(x1, y1, z1, x2, y2, z2, color);
        }
    }

    private Vec3 getSpherePoint(Vec3 center, double radius, double theta, double phi) {
        double x = center.x + radius * Math.sin(theta) * Math.cos(phi);
        double y = center.y + radius * Math.cos(theta);
        double z = center.z + radius * Math.sin(theta) * Math.sin(phi);
        return new Vec3(x, y, z);
    }

    private void renderHitbox(Render3DEvent event, Player player, Vec3 eyePos, double reach, Color color) {
        // Calculate hitbox area in front of player
        Vec3 lookVec = player.getLookAngle().normalize();
        Vec3 hitboxStart = eyePos.add(lookVec.scale(0.5)); // Start slightly in front of eyes
        Vec3 hitboxEnd = eyePos.add(lookVec.scale(reach));

        double halfWidth = reach * 0.3;
        double height = hitboxHeight.get();

        // Calculate hitbox corners
        Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 upVec = rightVec.cross(lookVec).normalize();

        Vec3 bottomLeft = hitboxEnd.add(rightVec.scale(-halfWidth)).add(upVec.scale(-height / 2));
        Vec3 bottomRight = hitboxEnd.add(rightVec.scale(halfWidth)).add(upVec.scale(-height / 2));
        Vec3 topLeft = hitboxEnd.add(rightVec.scale(-halfWidth)).add(upVec.scale(height / 2));
        Vec3 topRight = hitboxEnd.add(rightVec.scale(halfWidth)).add(upVec.scale(height / 2));

        // Render hitbox as a transparent quad
        Color hitboxColor = new Color(color.r, color.g, color.b, color.a / 2);
        event.renderer.triangles.ensureQuadCapacity();
        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(bottomLeft.x, bottomLeft.y, bottomLeft.z).color(hitboxColor).next(),
                event.renderer.triangles.vec3(bottomRight.x, bottomRight.y, bottomRight.z).color(hitboxColor).next(),
                event.renderer.triangles.vec3(topRight.x, topRight.y, topRight.z).color(hitboxColor).next(),
                event.renderer.triangles.vec3(topLeft.x, topLeft.y, topLeft.z).color(hitboxColor).next()
        );
    }

    private void renderEnergyField(Render3DEvent event, Vec3 center, double radius, Color color) {
        double intensity = energyIntensity.get();
        int segments = sphereResolution.get() * 2;

        // Create energy field using pulsating quads
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            double pulse = (Math.sin(animationProgress * 3 + i * 0.5) + 1) * 0.5;
            double effectiveRadius = radius * (1.0 + pulse * 0.1 * intensity);

            // Calculate points for energy tendril
            Vec3 inner1 = getSpherePoint(center, radius, Math.PI / 2, angle1);
            Vec3 inner2 = getSpherePoint(center, radius, Math.PI / 2, angle2);
            Vec3 outer1 = getSpherePoint(center, effectiveRadius, Math.PI / 2, angle1);
            Vec3 outer2 = getSpherePoint(center, effectiveRadius, Math.PI / 2, angle2);

            // Vary color intensity
            int alpha = (int) (color.a * (0.6 + 0.4 * pulse) * intensity);
            Color energyColor = new Color(color.r, color.g, color.b, alpha);

            // Render energy tendril as
            event.renderer.triangles.ensureQuadCapacity();
            event.renderer.triangles.quad(
                    event.renderer.triangles.vec3(inner1.x, inner1.y, inner1.z).color(energyColor).next(),
                    event.renderer.triangles.vec3(inner2.x, inner2.y, inner2.z).color(energyColor).next(),
                    event.renderer.triangles.vec3(outer2.x, outer2.y, outer2.z).color(energyColor).next(),
                    event.renderer.triangles.vec3(outer1.x, outer1.y, outer1.z).color(energyColor).next()
            );
        }
    }

    private void renderEnhancedTracer(Render3DEvent event, Player player, Color color, double reach) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 playerPos = new Vec3(player.getX(), player.getY() + player.getBbHeight() / 2, player.getZ());

        switch (tracerStyle.get()) {
            case Beam:
                renderBeamTracer(event, eyePos, playerPos, color);
                break;
            case Energy:
                renderEnergyTracer(event, eyePos, playerPos, color);
                break;
            case Ribbon:
                renderRibbonTracer(event, eyePos, playerPos, color);
                break;
        }
    }

    private void renderBeamTracer(Render3DEvent event, Vec3 start, Vec3 end, Color color) {
        double pulse = tracerPulse.get() ?
                (Math.sin(animationProgress * 2) + 1) * 0.5 + 0.5 : 1.0;
        double width = tracerWidth.get() * pulse;

        // Calculate perpendicular vectors for quad width
        Vec3 dir = end.subtract(start).normalize();
        Vec3 right = dir.cross(new Vec3(0, 1, 0)).normalize();
        if (right.length() < 0.1) {
            right = dir.cross(new Vec3(1, 0, 0)).normalize();
        }
        Vec3 up = right.cross(dir).normalize();

        // Calculate quad corners
        Vec3 rightOffset = right.scale(width);
        Vec3 upOffset = up.scale(width);

        Vec3 bottomLeft = start.add(rightOffset).add(upOffset);
        Vec3 bottomRight = start.subtract(rightOffset).add(upOffset);
        Vec3 topLeft = start.add(rightOffset).subtract(upOffset);
        Vec3 topRight = start.subtract(rightOffset).subtract(upOffset);

        Vec3 endBottomLeft = end.add(rightOffset).add(upOffset);
        Vec3 endBottomRight = end.subtract(rightOffset).add(upOffset);
        Vec3 endTopLeft = end.add(rightOffset).subtract(upOffset);
        Vec3 endTopRight = end.subtract(rightOffset).subtract(upOffset);

        // Render beam as a box of quads
        renderTracerQuad(event, bottomLeft, bottomRight, endBottomRight, endBottomLeft, color); // Bottom
        renderTracerQuad(event, topLeft, topRight, endTopRight, endTopLeft, color); // Top
        renderTracerQuad(event, bottomLeft, topLeft, endTopLeft, endBottomLeft, color); // Left
        renderTracerQuad(event, bottomRight, topRight, endTopRight, endBottomRight, color); // Right
    }

    private void renderEnergyTracer(Render3DEvent event, Vec3 start, Vec3 end, Color color) {
        double pulse = (Math.sin(animationProgress * 3) + 1) * 0.5;
        double width = tracerWidth.get() * (1.0 + pulse * 0.5);

        // Create energy tendrils along the tracer path
        Vec3 dir = end.subtract(start);
        int segments = 8;

        for (int i = 0; i < segments; i++) {
            double progress1 = (double) i / segments;
            double progress2 = (double) (i + 1) / segments;

            Vec3 segStart = start.add(dir.scale(progress1));
            Vec3 segEnd = start.add(dir.scale(progress2));

            // Vary color intensity
            int alpha = (int) (color.a * (0.7 + 0.3 * Math.sin(animationProgress + i * 0.5)));
            Color segmentColor = new Color(color.r, color.g, color.b, alpha);

            renderBeamTracer(event, segStart, segEnd, segmentColor);
        }
    }

    private void renderRibbonTracer(Render3DEvent event, Vec3 start, Vec3 end, Color color) {
        Vec3 dir = end.subtract(start);
        Vec3 right = dir.cross(new Vec3(0, 1, 0)).normalize();
        if (right.length() < 0.1) {
            right = dir.cross(new Vec3(1, 0, 0)).normalize();
        }

        int segments = 12;
        double maxWidth = tracerWidth.get() * 2;

        for (int i = 0; i < segments; i++) {
            double progress1 = (double) i / segments;
            double progress2 = (double) (i + 1) / segments;

            // Calculate ribbon width with wave pattern
            double width = maxWidth * Math.sin(progress1 * Math.PI);

            Vec3 segStart = start.add(dir.scale(progress1));
            Vec3 segEnd = start.add(dir.scale(progress2));

            Vec3 offset1 = right.scale(width * Math.sin(animationProgress + i * 0.5));
            Vec3 offset2 = right.scale(width * Math.sin(animationProgress + (i + 1) * 0.5));

            // Vary color
            int alpha = (int) (color.a * (0.6 + 0.4 * Math.sin(progress1 * Math.PI)));
            Color segmentColor = new Color(color.r, color.g, color.b, alpha);

            // Render ribbon segment as quad
            event.renderer.triangles.ensureQuadCapacity();
            event.renderer.triangles.quad(
                    event.renderer.triangles.vec3(segStart.x + offset1.x, segStart.y + offset1.y, segStart.z + offset1.z).color(segmentColor).next(),
                    event.renderer.triangles.vec3(segStart.x - offset1.x, segStart.y - offset1.y, segStart.z - offset1.z).color(segmentColor).next(),
                    event.renderer.triangles.vec3(segEnd.x - offset2.x, segEnd.y - offset2.y, segEnd.z - offset2.z).color(segmentColor).next(),
                    event.renderer.triangles.vec3(segEnd.x + offset2.x, segEnd.y + offset2.y, segEnd.z + offset2.z).color(segmentColor).next()
            );
        }
    }

    private void renderTracerQuad(Render3DEvent event, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, Color color) {
        event.renderer.triangles.ensureQuadCapacity();
        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(v1.x, v1.y, v1.z).color(color).next(),
                event.renderer.triangles.vec3(v2.x, v2.y, v2.z).color(color).next(),
                event.renderer.triangles.vec3(v3.x, v3.y, v3.z).color(color).next(),
                event.renderer.triangles.vec3(v4.x, v4.y, v4.z).color(color).next()
        );
    }

    private void renderDangerZone(Render3DEvent event, Player player, double reach) {
        Vec3 eyePos = player.getEyePosition(1.0f);
        Color danger = dangerColor.get();

        // Create pulsating danger indicator
        double pulse = (Math.sin(animationProgress * 4) + 1) * 0.5;
        int alpha = (int) (danger.a * (0.7 + 0.3 * pulse));
        Color pulseColor = new Color(danger.r, danger.g, danger.b, alpha);

        // Render danger sphere
        renderWireframeSphere(event, eyePos, reach, pulseColor);

        // Add warning effect
        if (pulse > 0.9) {
            renderWarningFlash(event, eyePos, reach * 1.1, pulseColor);
        }
    }

    private void renderWarningFlash(Render3DEvent event, Vec3 center, double radius, Color color) {
        // Create a quick flash effect
        int segments = 8;

        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            Vec3 inner = getSpherePoint(center, radius * 0.9, Math.PI / 2, angle1);
            Vec3 outer = getSpherePoint(center, radius * 1.1, Math.PI / 2, angle1);
            Vec3 inner2 = getSpherePoint(center, radius * 0.9, Math.PI / 2, angle2);
            Vec3 outer2 = getSpherePoint(center, radius * 1.1, Math.PI / 2, angle2);

            event.renderer.triangles.ensureQuadCapacity();
            event.renderer.triangles.quad(
                    event.renderer.triangles.vec3(inner.x, inner.y, inner.z).color(color).next(),
                    event.renderer.triangles.vec3(inner2.x, inner2.y, inner2.z).color(color).next(),
                    event.renderer.triangles.vec3(outer2.x, outer2.y, outer2.z).color(color).next(),
                    event.renderer.triangles.vec3(outer.x, outer.y, outer.z).color(color).next()
            );
        }
    }

    public enum TracerStyle {
        Beam,
        Energy,
        Ribbon
    }

    public enum SphereStyle {
        Wireframe,
        Transparent,
        Energy
    }
}