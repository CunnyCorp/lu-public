package pictures.cunny.loli_utils.modules.rendering;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.modules.LoliModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Ripples extends LoliModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSelf = settings.createGroup("Self");
    private final SettingGroup sgOthers = settings.createGroup("Others");
    private final SettingGroup sgVisual = settings.createGroup("Visual");

    // General Settings
    private final Setting<Integer> trailLength = sgGeneral.add(new IntSetting.Builder()
            .name("trail-length")
            .description("Maximum number of positions to track.")
            .defaultValue(20)
            .min(1)
            .max(100)
            .sliderMax(50)
            .build()
    );

    private final Setting<Integer> updateRate = sgGeneral.add(new IntSetting.Builder()
            .name("update-rate")
            .description("How often to update positions (in ticks).")
            .defaultValue(2)
            .min(1)
            .max(20)
            .build()
    );

    private final Setting<Double> minDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("min-distance")
            .description("Minimum distance player must move to record new position.")
            .defaultValue(0.1)
            .min(0.01)
            .max(5.0)
            .sliderMax(2.0)
            .build()
    );

    private final Setting<Double> renderDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("render-distance")
            .description("Maximum distance to render trails.")
            .defaultValue(64.0)
            .min(1.0)
            .max(512.0)
            .sliderMax(128.0)
            .build()
    );

    // Self Settings
    private final Setting<Boolean> renderSelf = sgSelf.add(new BoolSetting.Builder()
            .name("render-self")
            .description("Render your own trail.")
            .defaultValue(true)
            .build()
    );

    private final Setting<TrailType> selfTrailType = sgSelf.add(new EnumSetting.Builder<TrailType>()
            .name("self-trail-type")
            .description("Type of trail to render for yourself.")
            .defaultValue(TrailType.Circle)
            .visible(renderSelf::get)
            .build()
    );
    private final Setting<Double> selfCircleSize = sgSelf.add(new DoubleSetting.Builder()
            .name("self-circle-size")
            .description("Size of your trail circles.")
            .defaultValue(0.5)
            .min(0.1)
            .max(3.0)
            .visible(() -> renderSelf.get() && selfTrailType.get() == TrailType.Circle)
            .build()
    );
    private final Setting<Double> selfSphereSize = sgSelf.add(new DoubleSetting.Builder()
            .name("self-sphere-size")
            .description("Size of your trail spheres.")
            .defaultValue(0.4)
            .min(0.1)
            .max(3.0)
            .visible(() -> renderSelf.get() && selfTrailType.get() == TrailType.Sphere)
            .build()
    );
    private final Setting<Double> selfStarSize = sgSelf.add(new DoubleSetting.Builder()
            .name("self-star-size")
            .description("Size of your trail stars.")
            .defaultValue(0.6)
            .min(0.1)
            .max(3.0)
            .visible(() -> renderSelf.get() && selfTrailType.get() == TrailType.Star)
            .build()
    );
    private final Setting<SettingColor> selfStartColor = sgSelf.add(new ColorSetting.Builder()
            .name("self-start-color")
            .description("Color of the newest position in your trail.")
            .defaultValue(new SettingColor(255, 255, 255, 200))
            .visible(renderSelf::get)
            .build()
    );
    private final Setting<SettingColor> selfEndColor = sgSelf.add(new ColorSetting.Builder()
            .name("self-end-color")
            .description("Color of the oldest position in your trail.")
            .defaultValue(new SettingColor(255, 255, 255, 50))
            .visible(renderSelf::get)
            .build()
    );
    // Others Settings
    private final Setting<Boolean> renderOthers = sgOthers.add(new BoolSetting.Builder()
            .name("render-others")
            .description("Render other players' trails.")
            .defaultValue(true)
            .build()
    );

    private final Setting<TrailType> othersTrailType = sgOthers.add(new EnumSetting.Builder<TrailType>()
            .name("others-trail-type")
            .description("Type of trail to render for other players.")
            .defaultValue(TrailType.Circle)
            .visible(renderOthers::get)
            .build()
    );
    private final Setting<Double> othersCircleSize = sgOthers.add(new DoubleSetting.Builder()
            .name("others-circle-size")
            .description("Size of other players' trail circles.")
            .defaultValue(0.4)
            .min(0.1)
            .max(3.0)
            .visible(() -> renderOthers.get() && othersTrailType.get() == TrailType.Circle)
            .build()
    );
    private final Setting<Double> othersSphereSize = sgOthers.add(new DoubleSetting.Builder()
            .name("others-sphere-size")
            .description("Size of other players' trail spheres.")
            .defaultValue(0.3)
            .min(0.1)
            .max(3.0)
            .visible(() -> renderOthers.get() && othersTrailType.get() == TrailType.Sphere)
            .build()
    );
    private final Setting<Double> othersStarSize = sgOthers.add(new DoubleSetting.Builder()
            .name("others-star-size")
            .description("Size of other players' trail stars.")
            .defaultValue(0.5)
            .min(0.1)
            .max(3.0)
            .visible(() -> renderOthers.get() && othersTrailType.get() == TrailType.Star)
            .build()
    );
    private final Setting<SettingColor> othersStartColor = sgOthers.add(new ColorSetting.Builder()
            .name("others-start-color")
            .description("Color of the newest position in other players' trails.")
            .defaultValue(new SettingColor(255, 100, 100, 200))
            .visible(renderOthers::get)
            .build()
    );
    private final Setting<SettingColor> othersEndColor = sgOthers.add(new ColorSetting.Builder()
            .name("others-end-color")
            .description("Color of the oldest position in other players' trails.")
            .defaultValue(new SettingColor(255, 100, 100, 50))
            .visible(renderOthers::get)
            .build()
    );
    // Visual Settings
    private final Setting<Boolean> fadeWithTime = sgVisual.add(new BoolSetting.Builder()
            .name("fade-with-time")
            .description("Gradually fade trail positions over time.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> scaleWithDistance = sgVisual.add(new BoolSetting.Builder()
            .name("scale-with-distance")
            .description("Scale trail elements based on distance from camera.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> maxScale = sgVisual.add(new DoubleSetting.Builder()
            .name("max-scale")
            .description("Maximum scale multiplier for distant trails.")
            .defaultValue(2.0)
            .min(0.1)
            .max(5.0)
            .visible(scaleWithDistance::get)
            .build()
    );

    private final Setting<Boolean> onlyWhenMoving = sgVisual.add(new BoolSetting.Builder()
            .name("only-when-moving")
            .description("Only show trails when players are moving.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> circleSegments = sgVisual.add(new IntSetting.Builder()
            .name("circle-segments")
            .description("Number of segments to use for rendering circles.")
            .defaultValue(16)
            .min(8)
            .max(64)
            .build()
    );

    private final Setting<Integer> sphereSegments = sgVisual.add(new IntSetting.Builder()
            .name("sphere-segments")
            .description("Number of segments to use for rendering spheres.")
            .defaultValue(12)
            .min(6)
            .max(32)
            .build()
    );

    private final Setting<Integer> starPoints = sgVisual.add(new IntSetting.Builder()
            .name("star-points")
            .description("Number of points for star shapes.")
            .defaultValue(5)
            .min(3)
            .max(12)
            .build()
    );

    private final Setting<Boolean> glowEffect = sgVisual.add(new BoolSetting.Builder()
            .name("glow-effect")
            .description("Add a glowing effect to the trails.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> glowIntensity = sgVisual.add(new DoubleSetting.Builder()
            .name("glow-intensity")
            .description("Intensity of the glow effect.")
            .defaultValue(1.5)
            .min(0.5)
            .max(3.0)
            .visible(glowEffect::get)
            .build()
    );

    private final Setting<Integer> glowLayers = sgVisual.add(new IntSetting.Builder()
            .name("glow-layers")
            .description("Number of glow layers to render.")
            .defaultValue(3)
            .min(1)
            .max(6)
            .visible(glowEffect::get)
            .build()
    );

    private final Setting<Boolean> connectTrails = sgVisual.add(new BoolSetting.Builder()
            .name("connect-trails")
            .description("Connect trail positions with glowing lines.")
            .defaultValue(true)
            .build()
    );


    // Data structures
    private final Map<UUID, List<TrailPosition>> playerTrails = new ConcurrentHashMap<>();
    private final Map<UUID, Vec3> lastPositions = new ConcurrentHashMap<>();
    private int tickCounter = 0;

    public Ripples() {
        super(LoliUtilsMeteor.CATEGORY, "ripples", "Shows player movement trails with glowing, fading effects.");
    }

    @Override
    public void safeOnActivate() {
        playerTrails.clear();
        lastPositions.clear();
        tickCounter = 0;
    }

    @Override
    public void onDeactivate() {
        playerTrails.clear();
        lastPositions.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        tickCounter++;

        if (tickCounter % updateRate.get() != 0) return;

        if (mc.level == null || mc.player == null) return;

        // Update self position
        if (renderSelf.get()) {
            updatePlayerTrail(mc.player.getUUID(), mc.player);
        }

        // Update other players' positions
        if (renderOthers.get()) {
            for (AbstractClientPlayer player : mc.level.players()) {
                if (player == mc.player) continue;

                double distance = mc.player.distanceTo(player);
                if (distance > renderDistance.get()) continue;

                updatePlayerTrail(player.getUUID(), player);
            }
        }

        // Clean up old trails
        cleanupOldTrails();
    }

    private void updatePlayerTrail(UUID playerId, Player player) {
        Vec3 currentPos = player.position();
        Vec3 lastPos = lastPositions.get(playerId);

        // Check if player moved enough
        if (lastPos != null && currentPos.distanceTo(lastPos) < minDistance.get()) {
            return;
        }

        // Check if player is moving (if required)
        if (onlyWhenMoving.get() && currentPos.equals(lastPos)) {
            return;
        }

        List<TrailPosition> trail = playerTrails.computeIfAbsent(playerId, k -> new ArrayList<>());

        TrailPosition newPosition = new TrailPosition(currentPos, player.getYRot(), player.getXRot());
        trail.add(0, newPosition); // Add to front

        // Limit trail length
        while (trail.size() > trailLength.get()) {
            trail.remove(trail.size() - 1);
        }

        lastPositions.put(playerId, currentPos);
    }

    private void cleanupOldTrails() {
        Iterator<Map.Entry<UUID, List<TrailPosition>>> iterator = playerTrails.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, List<TrailPosition>> entry = iterator.next();
            UUID playerId = entry.getKey();
            List<TrailPosition> trail = entry.getValue();

            // Remove if player no longer exists
            boolean playerExists = false;
            if (playerId.equals(mc.player.getUUID())) {
                playerExists = renderSelf.get();
            } else {
                playerExists = mc.level.players().stream()
                        .anyMatch(p -> p.getUUID().equals(playerId) &&
                                mc.player.distanceTo(p) <= renderDistance.get());
            }

            if (!playerExists) {
                iterator.remove();
                lastPositions.remove(playerId);
                continue;
            }

            // Remove old positions from trail
            trail.removeIf(pos -> pos.getAge() > 30.0); // Remove positions older than 30 seconds

            if (trail.isEmpty()) {
                iterator.remove();
                lastPositions.remove(playerId);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (Map.Entry<UUID, List<TrailPosition>> entry : playerTrails.entrySet()) {
            UUID playerId = entry.getKey();
            List<TrailPosition> trail = entry.getValue();

            if (trail.isEmpty()) continue;

            boolean isSelf = playerId.equals(mc.player.getUUID());
            renderPlayerTrail(event, trail, isSelf);
        }
    }

    private void renderPlayerTrail(Render3DEvent event, List<TrailPosition> trail, boolean isSelf) {
        if (trail.isEmpty()) return;

        TrailType trailType = isSelf ? selfTrailType.get() : othersTrailType.get();
        SettingColor startColor = isSelf ? selfStartColor.get() : othersStartColor.get();
        SettingColor endColor = isSelf ? selfEndColor.get() : othersEndColor.get();
        double baseSize = getBaseSize(isSelf, trailType);

        // Render connecting lines if enabled
        if (connectTrails.get() && trail.size() > 1) {
            renderConnectingLines(event, trail, startColor, endColor);
        }

        // Render trail elements
        for (int i = 0; i < trail.size(); i++) {
            TrailPosition pos = trail.get(i);
            double progress = (double) i / Math.max(1, trail.size() - 1);
            Color color = interpolateColor(startColor, endColor, progress);

            if (fadeWithTime.get()) {
                double age = Math.min(pos.getAge(), 10.0) / 10.0;
                color = new Color(color.r, color.g, color.b, (int) (color.a * (1.0 - age)));
            }

            double size = baseSize;
            if (scaleWithDistance.get()) {
                double distance = mc.player.position().distanceTo(pos.position);
                size *= Math.min(maxScale.get(), 1.0 + distance / 32.0);
            }

            // Render the appropriate trail type
            switch (trailType) {
                case Circle -> renderGlowingCircle(event, pos.position, size, color, circleSegments.get());
                case Sphere -> renderGlowingSphere(event, pos.position, size, color, sphereSegments.get());
                case Star -> renderGlowingStar(event, pos.position, size, color, starPoints.get());
                case Cube -> renderGlowingCube(event, pos.position, size, color);
                case Cross -> renderGlowingCross(event, pos.position, size, color);
            }
        }
    }

    private double getBaseSize(boolean isSelf, TrailType trailType) {
        return switch (trailType) {
            case Circle -> isSelf ? selfCircleSize.get() : othersCircleSize.get();
            case Sphere -> isSelf ? selfSphereSize.get() : othersSphereSize.get();
            case Star -> isSelf ? selfStarSize.get() : othersStarSize.get();
            case Cube, Cross -> isSelf ? 0.3 : 0.25;
        };
    }

    private void renderConnectingLines(Render3DEvent event, List<TrailPosition> trail, SettingColor startColor, SettingColor endColor) {
        for (int i = 0; i < trail.size() - 1; i++) {
            TrailPosition current = trail.get(i);
            TrailPosition next = trail.get(i + 1);

            double progress = (double) i / (trail.size() - 1);
            Color color = interpolateColor(startColor, endColor, progress);

            if (fadeWithTime.get()) {
                double age = Math.min(current.getAge(), 10.0) / 10.0;
                color = new Color(color.r, color.g, color.b, (int) (color.a * (1.0 - age)));
            }

            // Draw the connecting line
            event.renderer.line(
                    current.position.x, current.position.y + 0.05, current.position.z,
                    next.position.x, next.position.y + 0.05, next.position.z,
                    color
            );

            // Add glow effect to the line
            if (glowEffect.get()) {
                for (int j = 1; j <= glowLayers.get(); j++) {
                    int glowAlpha = (int) (color.a * (0.3 / j) * glowIntensity.get());
                    Color glowColor = new Color(color.r, color.g, color.b, Math.min(glowAlpha, 80));

                    event.renderer.line(
                            current.position.x, current.position.y + 0.05, current.position.z,
                            next.position.x, next.position.y + 0.05, next.position.z,
                            glowColor
                    );
                }
            }
        }
    }

    private void renderGlowingCircle(Render3DEvent event, Vec3 center, double radius, Color color, int segments) {
        double x = center.x;
        double y = center.y + 0.05; // Slightly above the ground
        double z = center.z;

        // Draw the main circle
        drawCircle(event, x, y, z, radius, color, segments);

        // Add glow effect with multiple concentric circles
        if (glowEffect.get()) {
            for (int i = 1; i <= glowLayers.get(); i++) {
                double glowRadius = radius * (1 + i * 0.2 * glowIntensity.get());
                int glowAlpha = (int) (color.a * (0.4 / i) * glowIntensity.get());
                Color glowColor = new Color(color.r, color.g, color.b, Math.min(glowAlpha, 100));
                drawCircle(event, x, y, z, glowRadius, glowColor, segments);
            }
        }
    }

    private void renderGlowingSphere(Render3DEvent event, Vec3 center, double radius, Color color, int segments) {
        double x = center.x;
        double y = center.y + radius / 2; // Center the sphere
        double z = center.z;

        // Draw the main sphere
        drawSphere(event, x, y, z, radius, color, segments);

        // Add glow effect with multiple concentric spheres
        if (glowEffect.get()) {
            for (int i = 1; i <= glowLayers.get(); i++) {
                double glowRadius = radius * (1 + i * 0.15 * glowIntensity.get());
                int glowAlpha = (int) (color.a * (0.3 / i) * glowIntensity.get());
                Color glowColor = new Color(color.r, color.g, color.b, Math.min(glowAlpha, 80));
                drawSphere(event, x, y, z, glowRadius, glowColor, segments);
            }
        }
    }

    private void renderGlowingStar(Render3DEvent event, Vec3 center, double size, Color color, int points) {
        double x = center.x;
        double y = center.y + 0.05;
        double z = center.z;

        // Draw the main star
        drawStar(event, x, y, z, size, color, points);

        // Add glow effect with multiple stars
        if (glowEffect.get()) {
            for (int i = 1; i <= glowLayers.get(); i++) {
                double glowSize = size * (1 + i * 0.25 * glowIntensity.get());
                int glowAlpha = (int) (color.a * (0.35 / i) * glowIntensity.get());
                Color glowColor = new Color(color.r, color.g, color.b, Math.min(glowAlpha, 90));
                drawStar(event, x, y, z, glowSize, glowColor, points);
            }
        }
    }

    private void renderGlowingCube(Render3DEvent event, Vec3 center, double size, Color color) {
        double x = center.x;
        double y = center.y + size / 2; // Center the cube
        double z = center.z;
        double half = size / 2;

        // Draw the main cube
        drawCube(event, x, y, z, half, color);

        // Add glow effect with multiple cubes
        if (glowEffect.get()) {
            for (int i = 1; i <= glowLayers.get(); i++) {
                double glowHalf = half * (1 + i * 0.2 * glowIntensity.get());
                int glowAlpha = (int) (color.a * (0.3 / i) * glowIntensity.get());
                Color glowColor = new Color(color.r, color.g, color.b, Math.min(glowAlpha, 80));
                drawCube(event, x, y, z, glowHalf, glowColor);
            }
        }
    }

    private void renderGlowingCross(Render3DEvent event, Vec3 center, double size, Color color) {
        double x = center.x;
        double y = center.y + 0.05;
        double z = center.z;
        double half = size / 2;

        // Draw the main cross
        drawCross(event, x, y, z, half, color);

        // Add glow effect with multiple crosses
        if (glowEffect.get()) {
            for (int i = 1; i <= glowLayers.get(); i++) {
                double glowHalf = half * (1 + i * 0.25 * glowIntensity.get());
                int glowAlpha = (int) (color.a * (0.35 / i) * glowIntensity.get());
                Color glowColor = new Color(color.r, color.g, color.b, Math.min(glowAlpha, 90));
                drawCross(event, x, y, z, glowHalf, glowColor);
            }
        }
    }

    private void drawCircle(Render3DEvent event, double x, double y, double z, double radius, Color color, int segments) {
        double segmentAngle = 2 * Math.PI / segments;

        for (int i = 0; i < segments; i++) {
            double angle1 = i * segmentAngle;
            double angle2 = (i + 1) * segmentAngle;

            double x1 = x + Math.cos(angle1) * radius;
            double z1 = z + Math.sin(angle1) * radius;
            double x2 = x + Math.cos(angle2) * radius;
            double z2 = z + Math.sin(angle2) * radius;

            event.renderer.line(x1, y, z1, x2, y, z2, color);
        }
    }

    private void drawSphere(Render3DEvent event, double x, double y, double z, double radius, Color color, int segments) {
        // Draw horizontal circles
        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI / segments;
            double circleRadius = Math.sin(angle) * radius;
            double circleY = y + Math.cos(angle) * radius - radius / 2;

            drawCircle(event, x, circleY, z, circleRadius, color, segments);
        }

        // Draw vertical circles
        for (int i = 0; i < segments / 2; i++) {
            double angle = i * 2 * Math.PI / segments;
            drawCircle(event, x, y, z, radius, color, segments);
        }
    }

    private void drawStar(Render3DEvent event, double x, double y, double z, double size, Color color, int points) {
        double outerRadius = size;
        double innerRadius = size * 0.5;

        for (int i = 0; i < points * 2; i++) {
            double angle1 = i * Math.PI / points;
            double angle2 = (i + 1) * Math.PI / points;

            double radius1 = (i % 2 == 0) ? outerRadius : innerRadius;
            double radius2 = ((i + 1) % 2 == 0) ? outerRadius : innerRadius;

            double x1 = x + Math.cos(angle1) * radius1;
            double z1 = z + Math.sin(angle1) * radius1;
            double x2 = x + Math.cos(angle2) * radius2;
            double z2 = z + Math.sin(angle2) * radius2;

            event.renderer.line(x1, y, z1, x2, y, z2, color);
        }
    }

    private void drawCube(Render3DEvent event, double x, double y, double z, double half, Color color) {
        // Bottom square
        event.renderer.line(x - half, y - half, z - half, x + half, y - half, z - half, color);
        event.renderer.line(x + half, y - half, z - half, x + half, y - half, z + half, color);
        event.renderer.line(x + half, y - half, z + half, x - half, y - half, z + half, color);
        event.renderer.line(x - half, y - half, z + half, x - half, y - half, z - half, color);

        // Top square
        event.renderer.line(x - half, y + half, z - half, x + half, y + half, z - half, color);
        event.renderer.line(x + half, y + half, z - half, x + half, y + half, z + half, color);
        event.renderer.line(x + half, y + half, z + half, x - half, y + half, z + half, color);
        event.renderer.line(x - half, y + half, z + half, x - half, y + half, z - half, color);

        // Vertical edges
        event.renderer.line(x - half, y - half, z - half, x - half, y + half, z - half, color);
        event.renderer.line(x + half, y - half, z - half, x + half, y + half, z - half, color);
        event.renderer.line(x + half, y - half, z + half, x + half, y + half, z + half, color);
        event.renderer.line(x - half, y - half, z + half, x - half, y + half, z + half, color);
    }

    private void drawCross(Render3DEvent event, double x, double y, double z, double half, Color color) {
        // Horizontal line
        event.renderer.line(x - half, y, z, x + half, y, z, color);

        // Vertical line
        event.renderer.line(x, y, z - half, x, y, z + half, color);

        // Diagonal lines (for a more interesting cross)
        event.renderer.line(x - half / 2, y, z - half / 2, x + half / 2, y, z + half / 2, color);
        event.renderer.line(x - half / 2, y, z + half / 2, x + half / 2, y, z - half / 2, color);
    }

    private Color interpolateColor(SettingColor start, SettingColor end, double progress) {
        progress = Math.max(0, Math.min(1, progress));

        int r = (int) (start.r + (end.r - start.r) * progress);
        int g = (int) (start.g + (end.g - start.g) * progress);
        int b = (int) (start.b + (end.b - start.b) * progress);
        int a = (int) (start.a + (end.a - start.a) * progress);

        return new Color(r, g, b, a);
    }

    public enum TrailType {
        Circle,
        Sphere,
        Star,
        Cube,
        Cross
    }

    private static class TrailPosition {
        public final Vec3 position;
        public final long timestamp;
        public final float yaw;
        public final float pitch;

        public TrailPosition(Vec3 position, float yaw, float pitch) {
            this.position = position;
            this.timestamp = System.currentTimeMillis();
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public double getAge() {
            return (System.currentTimeMillis() - timestamp) / 1000.0;
        }
    }
}