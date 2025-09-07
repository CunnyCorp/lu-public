package pictures.cunny.loli_utils.modules.rendering;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.modules.LoliModule;

import java.util.*;

public class BoxESP extends LoliModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgFoggyEffect = settings.createGroup("Foggy Effect");
    private final SettingGroup sgRadiatingOutline = settings.createGroup("Radiating Outline");
    private final SettingGroup sgShiningAnimation = settings.createGroup("Shining Animation");
    private final SettingGroup sgRainbow = settings.createGroup("Rainbow Effect");
    private final SettingGroup sgAdvanced = settings.createGroup("Advanced Effects");
    private final SettingGroup sgRendering = settings.createGroup("Rendering Options");
    private final SettingGroup sgPositionHistory = settings.createGroup("Position History");

    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("max-distance")
            .description("Maximum distance to render ESP.")
            .defaultValue(128.0)
            .min(1.0)
            .max(512.0)
            .sliderMax(256.0)
            .build()
    );

    private final Setting<Boolean> showSelf = sgGeneral.add(new BoolSetting.Builder()
            .name("show-player")
            .description("Show the player itself.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> showFriends = sgGeneral.add(new BoolSetting.Builder()
            .name("show-friends")
            .description("Show friends.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> showInvisible = sgGeneral.add(new BoolSetting.Builder()
            .name("show-invisible")
            .description("Show invisible players.")
            .defaultValue(true)
            .build()
    );

    private final Setting<List<String>> excludedPlayers = sgGeneral.add(new StringListSetting.Builder()
            .name("excluded-players")
            .description("Players to exclude from ESP.")
            .defaultValue(new ArrayList<>())
            .build()
    );

    private final Setting<SettingColor> defaultColor = sgColors.add(new ColorSetting.Builder()
            .name("default-color")
            .description("Default color for players.")
            .defaultValue(new SettingColor(255, 50, 50, 200))
            .build()
    );

    private final Setting<SettingColor> friendColor = sgColors.add(new ColorSetting.Builder()
            .name("friend-color")
            .description("Color for friends.")
            .defaultValue(new SettingColor(50, 255, 50, 200))
            .build()
    );

    private final Setting<SettingColor> teamColor = sgColors.add(new ColorSetting.Builder()
            .name("team-color")
            .description("Color for teammates.")
            .defaultValue(new SettingColor(50, 50, 255, 200))
            .build()
    );

    private final Setting<Boolean> enableFoggyEffect = sgFoggyEffect.add(new BoolSetting.Builder()
            .name("enable-foggy-effect")
            .description("Enable foggy blur/glow effect.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> foggyIntensity = sgFoggyEffect.add(new DoubleSetting.Builder()
            .name("foggy-intensity")
            .description("Intensity of the foggy effect.")
            .defaultValue(2.5)
            .min(1.0)
            .max(8.0)
            .visible(enableFoggyEffect::get)
            .build()
    );

    private final Setting<Integer> foggyLayers = sgFoggyEffect.add(new IntSetting.Builder()
            .name("foggy-layers")
            .description("Number of foggy layers.")
            .defaultValue(8)
            .min(3)
            .max(15)
            .visible(enableFoggyEffect::get)
            .build()
    );

    private final Setting<Double> foggySize = sgFoggyEffect.add(new DoubleSetting.Builder()
            .name("foggy-size")
            .description("Size multiplier for foggy effect.")
            .defaultValue(1.4)
            .min(1.1)
            .max(2.5)
            .visible(enableFoggyEffect::get)
            .build()
    );

    private final Setting<Boolean> foggyPulse = sgFoggyEffect.add(new BoolSetting.Builder()
            .name("foggy-pulse")
            .description("Make the foggy effect pulse.")
            .defaultValue(true)
            .visible(enableFoggyEffect::get)
            .build()
    );

    private final Setting<Double> foggyPulseSpeed = sgFoggyEffect.add(new DoubleSetting.Builder()
            .name("foggy-pulse-speed")
            .description("Speed of the foggy pulse effect.")
            .defaultValue(1.5)
            .min(0.5)
            .max(3.0)
            .visible(() -> enableFoggyEffect.get() && foggyPulse.get())
            .build()
    );

    private final Setting<Boolean> enableRadiatingOutline = sgRadiatingOutline.add(new BoolSetting.Builder()
            .name("enable-radiating-outline")
            .description("Enable radiating outline effect.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> radiatingSpeed = sgRadiatingOutline.add(new DoubleSetting.Builder()
            .name("radiating-speed")
            .description("Speed of the radiating effect.")
            .defaultValue(2.0)
            .min(0.5)
            .max(5.0)
            .visible(enableRadiatingOutline::get)
            .build()
    );

    private final Setting<Double> radiatingDistance = sgRadiatingOutline.add(new DoubleSetting.Builder()
            .name("radiating-distance")
            .description("Maximum distance of radiation.")
            .defaultValue(1.2)
            .min(0.3)
            .max(3.0)
            .visible(enableRadiatingOutline::get)
            .build()
    );

    private final Setting<Integer> radiatingWaves = sgRadiatingOutline.add(new IntSetting.Builder()
            .name("radiating-waves")
            .description("Number of simultaneous radiating waves.")
            .defaultValue(5)
            .min(2)
            .max(12)
            .visible(enableRadiatingOutline::get)
            .build()
    );

    private final Setting<Double> radiatingThickness = sgRadiatingOutline.add(new DoubleSetting.Builder()
            .name("radiating-thickness")
            .description("Thickness of radiating waves.")
            .defaultValue(2.0)
            .min(0.5)
            .max(5.0)
            .visible(enableRadiatingOutline::get)
            .build()
    );

    // Shining Animation Settings
    private final Setting<Boolean> enableShining = sgShiningAnimation.add(new BoolSetting.Builder()
            .name("enable-shining")
            .description("Enable intense shining animation effect.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> shiningSpeed = sgShiningAnimation.add(new DoubleSetting.Builder()
            .name("shining-speed")
            .description("Speed of the shining animation.")
            .defaultValue(3.0)
            .min(1.0)
            .max(8.0)
            .visible(enableShining::get)
            .build()
    );

    private final Setting<Double> shiningIntensity = sgShiningAnimation.add(new DoubleSetting.Builder()
            .name("shining-intensity")
            .description("Intensity of the shining effect.")
            .defaultValue(2.5)
            .min(1.0)
            .max(5.0)
            .visible(enableShining::get)
            .build()
    );

    private final Setting<ShiningPattern> shiningPattern = sgShiningAnimation.add(new EnumSetting.Builder<ShiningPattern>()
            .name("shining-pattern")
            .description("Pattern of the shining animation.")
            .defaultValue(ShiningPattern.Strobe)
            .visible(enableShining::get)
            .build()
    );

    private final Setting<Boolean> shiningAffectsFog = sgShiningAnimation.add(new BoolSetting.Builder()
            .name("shining-affects-fog")
            .description("Make shining effect also affect the foggy glow.")
            .defaultValue(true)
            .visible(enableShining::get)
            .build()
    );

    private final Setting<Boolean> enableRainbow = sgRainbow.add(new BoolSetting.Builder()
            .name("enable-rainbow")
            .description("Enable intense rainbow color effect.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> rainbowSpeed = sgRainbow.add(new DoubleSetting.Builder()
            .name("rainbow-speed")
            .description("Speed of the rainbow color cycle.")
            .defaultValue(2.0)
            .min(0.5)
            .max(8.0)
            .visible(enableRainbow::get)
            .build()
    );

    private final Setting<Double> rainbowSaturation = sgRainbow.add(new DoubleSetting.Builder()
            .name("rainbow-saturation")
            .description("Saturation of the rainbow colors.")
            .defaultValue(1.0)
            .min(0.7)
            .max(1.5)
            .visible(enableRainbow::get)
            .build()
    );

    private final Setting<Double> rainbowBrightness = sgRainbow.add(new DoubleSetting.Builder()
            .name("rainbow-brightness")
            .description("Brightness of the rainbow colors.")
            .defaultValue(1.2)
            .min(0.8)
            .max(1.5)
            .visible(enableRainbow::get)
            .build()
    );

    private final Setting<Boolean> enableParticleTrail = sgAdvanced.add(new BoolSetting.Builder()
            .name("enable-particle-trail")
            .description("Add particle trail effects to players.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> particleTrailIntensity = sgAdvanced.add(new DoubleSetting.Builder()
            .name("particle-trail-intensity")
            .description("Intensity of particle trail effect.")
            .defaultValue(1.5)
            .min(0.5)
            .max(3.0)
            .visible(enableParticleTrail::get)
            .build()
    );

    // Rendering Options
    private final Setting<Boolean> useQuadRendering = sgRendering.add(new BoolSetting.Builder()
            .name("use-quad-rendering")
            .description("Use quad-based rendering for better visual effects.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> enableGradient = sgRendering.add(new BoolSetting.Builder()
            .name("enable-gradient")
            .description("Enable gradient coloring for the box.")
            .defaultValue(true)
            .visible(useQuadRendering::get)
            .build()
    );

    private final Setting<SettingColor> gradientTopColor = sgRendering.add(new ColorSetting.Builder()
            .name("gradient-top-color")
            .description("Top color for gradient effect.")
            .defaultValue(new SettingColor(255, 50, 50, 150))
            .visible(() -> useQuadRendering.get() && enableGradient.get())
            .build()
    );

    private final Setting<SettingColor> gradientBottomColor = sgRendering.add(new ColorSetting.Builder()
            .name("gradient-bottom-color")
            .description("Bottom color for gradient effect.")
            .defaultValue(new SettingColor(50, 50, 255, 150))
            .visible(() -> useQuadRendering.get() && enableGradient.get())
            .build()
    );

    private final Setting<Integer> verticalSegments = sgRendering.add(new IntSetting.Builder()
            .name("vertical-segments")
            .description("Number of vertical segments for detailed rendering.")
            .defaultValue(8)
            .min(2)
            .max(24)
            .visible(useQuadRendering::get)
            .build()
    );

    private final Setting<Integer> horizontalSegments = sgRendering.add(new IntSetting.Builder()
            .name("horizontal-segments")
            .description("Number of horizontal segments for detailed rendering.")
            .defaultValue(12)
            .min(4)
            .max(32)
            .visible(useQuadRendering::get)
            .build()
    );

    private final Setting<Boolean> enableEdgeHighlights = sgRendering.add(new BoolSetting.Builder()
            .name("enable-edge-highlights")
            .description("Add highlights to box edges.")
            .defaultValue(true)
            .visible(useQuadRendering::get)
            .build()
    );

    private final Setting<Double> edgeHighlightIntensity = sgRendering.add(new DoubleSetting.Builder()
            .name("edge-highlight-intensity")
            .description("Intensity of edge highlights.")
            .defaultValue(1.5)
            .min(0.5)
            .max(3.0)
            .visible(() -> useQuadRendering.get() && enableEdgeHighlights.get())
            .build()
    );

    private final Setting<Boolean> showPositionHistory = sgPositionHistory.add(new BoolSetting.Builder()
            .name("show-position-history")
            .description("Show history of player positions.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> historyLength = sgPositionHistory.add(new IntSetting.Builder()
            .name("history-length")
            .description("Number of past positions to show.")
            .defaultValue(10)
            .min(1)
            .max(50)
            .visible(showPositionHistory::get)
            .build()
    );

    private final Setting<Boolean> showPrediction = sgPositionHistory.add(new BoolSetting.Builder()
            .name("show-prediction")
            .description("Show predicted future positions.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> predictionSteps = sgPositionHistory.add(new IntSetting.Builder()
            .name("prediction-steps")
            .description("Number of future positions to predict.")
            .defaultValue(5)
            .min(1)
            .max(20)
            .visible(showPrediction::get)
            .build()
    );

    private final Setting<Double> predictionMultiplier = sgPositionHistory.add(new DoubleSetting.Builder()
            .name("prediction-multiplier")
            .description("Multiplier for prediction distance.")
            .defaultValue(1.0)
            .min(0.1)
            .max(3.0)
            .visible(showPrediction::get)
            .build()
    );

    private final Setting<Double> historyFadeSpeed = sgPositionHistory.add(new DoubleSetting.Builder()
            .name("history-fade-speed")
            .description("How quickly history positions fade out.")
            .defaultValue(0.8)
            .min(0.1)
            .max(2.0)
            .visible(showPositionHistory::get)
            .build()
    );

    private final Setting<Double> movementThreshold = sgPositionHistory.add(new DoubleSetting.Builder()
            .name("movement-threshold")
            .description("Minimum movement distance to show history/prediction.")
            .defaultValue(0.1)
            .min(0.01)
            .max(1.0)
            .sliderMax(0.5)
            .visible(() -> showPositionHistory.get() || showPrediction.get())
            .build()
    );


    private double animationProgress = 0;
    private double rainbowHue = 0;
    private final Map<Player, List<Double>> radiatingProgress = new HashMap<>();
    private final Map<Player, List<Particle>> particleTrails = new HashMap<>();
    private final Map<Player, Deque<PositionData>> positionHistory = new HashMap<>();
    private final Map<Player, List<PositionData>> predictedPositions = new HashMap<>();

    public BoxESP() {
        super(LoliUtilsMeteor.CATEGORY, "box-esp", "It's just Box ESP.");
    }

    @Override
    public void safeOnActivate() {
        animationProgress = 0;
        rainbowHue = 0;
        radiatingProgress.clear();
        particleTrails.clear();
        positionHistory.clear();
        predictedPositions.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        animationProgress += 0.05;
        if (animationProgress > 100) animationProgress = 0;

        rainbowHue += rainbowSpeed.get() * 0.01;
        if (rainbowHue > 1.0) rainbowHue = 0;

        // Update radiating animation progress for all players
        if (mc.level != null) {
            for (Player player : mc.level.players()) {
                if (shouldSkipPlayer(player)) continue;

                List<Double> progressList = radiatingProgress.getOrDefault(player, new ArrayList<>());

                // Initialize waves if needed
                while (progressList.size() < radiatingWaves.get()) {
                    progressList.add(0.0);
                }

                // Update each wave's progress
                for (int i = 0; i < progressList.size(); i++) {
                    double progress = progressList.get(i) + radiatingSpeed.get() * 0.02;
                    if (progress <= 1.0) {
                        progressList.set(i, progress);
                    }
                }

                radiatingProgress.put(player, progressList);

                if (enableParticleTrail.get()) {
                    updateParticleTrail(player);
                }
            }
        }

        if (showPositionHistory.get() || showPrediction.get()) {
            for (Player player : mc.level.players()) {
                if (shouldSkipPlayer(player)) continue;

                Deque<PositionData> history = positionHistory.getOrDefault(player, new ArrayDeque<>());
                boolean isMoving = false;

                if (!history.isEmpty()) {
                    PositionData lastPos = history.getFirst();
                    double distance = Math.sqrt(
                            Math.pow(player.getX() - lastPos.x, 2) +
                                    Math.pow(player.getY() - lastPos.y, 2) +
                                    Math.pow(player.getZ() - lastPos.z, 2)
                    );
                    isMoving = distance > movementThreshold.get();
                }

                if (showPositionHistory.get()) {
                    Color playerColor = getPlayerColor(player);
                    if (enableRainbow.get()) {
                        playerColor = getRainbowColor(playerColor.a);
                    }

                    history.addFirst(new PositionData(player.getX(), player.getY(), player.getZ(), playerColor, isMoving));

                    while (history.size() > historyLength.get()) {
                        history.removeLast();
                    }

                    positionHistory.put(player, history);
                }

                if (showPrediction.get() && isMoving) {
                    predictPlayerPositions(player);
                } else {
                    predictedPositions.remove(player);
                }
            }
        }

        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Player, Deque<PositionData>>> historyIterator = positionHistory.entrySet().iterator();
        while (historyIterator.hasNext()) {
            Map.Entry<Player, Deque<PositionData>> entry = historyIterator.next();
            Deque<PositionData> history = entry.getValue();

            if (!history.isEmpty()) {
                PositionData latest = history.getFirst();
                long timeSinceLastMove = currentTime - latest.timestamp;

                if (timeSinceLastMove > 5000 && !latest.isMoving) {
                    historyIterator.remove();
                    predictedPositions.remove(entry.getKey());
                }
            }
        }
    }

    private void predictPlayerPositions(Player player) {
        Deque<PositionData> history = positionHistory.get(player);
        if (history == null || history.size() < 2) return;

        PositionData current = history.getFirst();
        if (!current.isMoving) {
            predictedPositions.remove(player);
            return;
        }

        List<PositionData> predictions = new ArrayList<>();
        Color playerColor = getPlayerColor(player);
        if (enableRainbow.get()) {
            playerColor = getRainbowColor(playerColor.a);
        }

        Iterator<PositionData> iterator = history.iterator();
        PositionData pos1 = iterator.next();
        PositionData pos2 = null;

        if (iterator.hasNext()) {
            pos2 = iterator.next();
        }

        if (pos2 == null) return;

        if (!pos1.isMoving || !pos2.isMoving) {
            predictedPositions.remove(player);
            return;
        }

        double velX = (pos1.x - pos2.x) * predictionMultiplier.get();
        double velY = (pos1.y - pos2.y) * predictionMultiplier.get();
        double velZ = (pos1.z - pos2.z) * predictionMultiplier.get();

        double totalVelocity = Math.sqrt(velX * velX + velY * velY + velZ * velZ);
        if (totalVelocity < movementThreshold.get() * 0.5) {
            predictedPositions.remove(player);
            return;
        }

        for (int i = 1; i <= predictionSteps.get(); i++) {
            double predX = pos1.x + velX * i;
            double predY = pos1.y + velY * i;
            double predZ = pos1.z + velZ * i;

            int alpha = (int) (playerColor.a * (1.0 - (i / (double) predictionSteps.get()) * 0.8));
            Color predColor = new Color(playerColor.r, playerColor.g, playerColor.b, Math.max(30, alpha));

            predictions.add(new PositionData(predX, predY, predZ, predColor, true));
        }

        predictedPositions.put(player, predictions);
    }


    private void updateParticleTrail(Player player) {
        List<Particle> particles = particleTrails.getOrDefault(player, new ArrayList<>());

        if (Math.random() < 0.3 * particleTrailIntensity.get()) {
            double offsetX = (Math.random() - 0.5) * player.getBbWidth();
            double offsetY = Math.random() * player.getBbHeight() * 0.8;
            double offsetZ = (Math.random() - 0.5) * player.getBbWidth();

            particles.add(new Particle(
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    (Math.random() - 0.5) * 0.1,
                    Math.random() * 0.05,
                    (Math.random() - 0.5) * 0.1,
                    20 + (int) (Math.random() * 20)
            ));
        }

        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.lifetime--;

            if (particle.lifetime <= 0) {
                iterator.remove();
            } else {
                particle.x += particle.motionX;
                particle.y += particle.motionY;
                particle.z += particle.motionZ;
                particle.motionY -= 0.005; // Gravity
            }
        }

        particleTrails.put(player, particles);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (AbstractClientPlayer player : mc.level.players()) {
            if (shouldSkipPlayer(player)) continue;

            double distance = mc.player.distanceTo(player);
            if (distance > maxDistance.get()) continue;

            Color color = getPlayerColor(player);

            if (enableRainbow.get()) {
                color = getRainbowColor(color.a);
            }

            if (useQuadRendering.get()) {
                renderBoxESPQuads(event, player, color);
            } else {
                renderBoxESP(event, player, color);
            }

            if (enableParticleTrail.get()) {
                renderParticleTrail(event, player, color);
            }

            if (showPositionHistory.get()) {
                renderPositionHistory(event);
            }

            if (showPrediction.get()) {
                renderPredictedPositions(event);
            }
        }
    }

    // Modify the renderPositionHistory method to check movement state
    private void renderPositionHistory(Render3DEvent event) {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Player, Deque<PositionData>> entry : positionHistory.entrySet()) {
            Player player = entry.getKey();
            if (shouldSkipPlayer(player)) continue;

            Deque<PositionData> history = entry.getValue();
            int index = 0;

            for (PositionData data : history) {
                if (index == 0) {
                    index++;
                    continue;
                }

                if (data.isMoving) {
                    long age = currentTime - data.timestamp;
                    double fade = Math.max(0, 1.0 - (age / 1000.0) * historyFadeSpeed.get());
                    int alpha = (int) (data.color.a * fade);

                    if (alpha > 10) {
                        Color fadedColor = new Color(data.color.r, data.color.g, data.color.b, alpha);

                        if (useQuadRendering.get()) {
                            renderHistoryBoxQuads(event, data.x, data.y, data.z,
                                    player.getBbWidth(), player.getBbHeight(), fadedColor);
                        } else {
                            renderHistoryBox(event, data.x, data.y, data.z,
                                    player.getBbWidth(), player.getBbHeight(), fadedColor);
                        }
                    }
                }
                index++;
            }
        }
    }


    private void renderPredictedPositions(Render3DEvent event) {
        for (Map.Entry<Player, List<PositionData>> entry : predictedPositions.entrySet()) {
            Player player = entry.getKey();
            if (shouldSkipPlayer(player)) continue;

            for (PositionData data : entry.getValue()) {

                if (useQuadRendering.get()) {
                    renderHistoryBoxQuads(event, data.x, data.y, data.z,
                            player.getBbWidth(), player.getBbHeight(), data.color);
                } else {
                    renderHistoryBox(event, data.x, data.y, data.z,
                            player.getBbWidth(), player.getBbHeight(), data.color);
                }
            }
        }
    }


    private void renderHistoryBox(Render3DEvent event, double x, double boxY, double z,
                                  double width, double height, Color color) {
        double boxX = x - width / 2;
        double boxZ = z - width / 2;

        event.renderer.box(boxX, boxY, boxZ, boxX + width, boxY + height, boxZ + width,
                color, color, ShapeMode.Both, 0);
    }

    private void renderHistoryBoxQuads(Render3DEvent event, double x, double boxY, double z,
                                       double width, double height, Color color) {
        double boxX = x - width / 2;
        double boxZ = z - width / 2;

        Color transparentColor = new Color(color.r, color.g, color.b, (int) (color.a * 0.6));
        renderBoxWithQuadsEnhanced(event, boxX, boxY, boxZ, width, height, width, transparentColor);
    }

    private boolean shouldSkipPlayer(Player player) {
        if (player == mc.player && !showSelf.get()) return true;
        if (!player.isAlive()) return true;
        if (player.isInvisible() && !showInvisible.get()) return true;
        if (Friends.get().isFriend(player) && !showFriends.get()) return true;
        return excludedPlayers.get().contains(player.getGameProfile().getName());
    }

    private Color getPlayerColor(Player player) {
        if (player.getTeam() != null && mc.player.getTeam() != null &&
                player.getTeam().isAlliedTo(mc.player.getTeam())) {
            return teamColor.get();
        }

        if (Friends.get().isFriend(player)) {
            return friendColor.get();
        }

        return defaultColor.get();
    }

    private Color getRainbowColor(int alpha) {
        double hue = rainbowHue;
        double saturation = rainbowSaturation.get();
        double brightness = rainbowBrightness.get();

        int r, g, b;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255);
        } else {
            double h = hue * 6;
            int i = (int) Math.floor(h);
            double f = h - i;
            double p = brightness * (1 - saturation);
            double q = brightness * (1 - saturation * f);
            double t = brightness * (1 - saturation * (1 - f));

            switch (i) {
                case 0:
                    r = (int) (brightness * 255);
                    g = (int) (t * 255);
                    b = (int) (p * 255);
                    break;
                case 1:
                    r = (int) (q * 255);
                    g = (int) (brightness * 255);
                    b = (int) (p * 255);
                    break;
                case 2:
                    r = (int) (p * 255);
                    g = (int) (brightness * 255);
                    b = (int) (t * 255);
                    break;
                case 3:
                    r = (int) (p * 255);
                    g = (int) (q * 255);
                    b = (int) (brightness * 255);
                    break;
                case 4:
                    r = (int) (t * 255);
                    g = (int) (p * 255);
                    b = (int) (brightness * 255);
                    break;
                default:
                    r = (int) (brightness * 255);
                    g = (int) (p * 255);
                    b = (int) (q * 255);
                    break;
            }
        }

        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));

        return new Color(r, g, b, alpha);
    }

    private void renderBoxESP(Render3DEvent event, Player player, Color color) {
        double x = player.getX() - player.getBbWidth() / 2;
        double y = player.getY();
        double z = player.getZ() - player.getBbWidth() / 2;
        double width = player.getBbWidth();
        double height = player.getBbHeight();

        Color finalColor = color;
        if (enableShining.get()) {
            finalColor = applyShiningEffect(color);
        }

        event.renderer.box(x, y, z, x + width, y + height, z + width,
                finalColor, finalColor, ShapeMode.Both, 0);

        if (enableFoggyEffect.get()) {
            renderFog(event, x, y, z, width, height,
                    shiningAffectsFog.get() ? finalColor : color);
        }

        if (enableRadiatingOutline.get()) {
            renderRadiatingOutline(event, player,
                    shiningAffectsFog.get() ? finalColor : color);
        }
    }

    private void renderBoxESPQuads(Render3DEvent event, Player player, Color color) {
        double x = player.getX() - player.getBbWidth() / 2;
        double y = player.getY();
        double z = player.getZ() - player.getBbWidth() / 2;
        double width = player.getBbWidth();
        double height = player.getBbHeight();
        double depth = player.getBbWidth();

        Color finalColor = color;
        if (enableShining.get()) {
            finalColor = applyShiningEffect(color);
        }

        renderBoxWithQuadsEnhanced(event, x, y, z, width, height, depth, finalColor);

        if (enableFoggyEffect.get()) {
            renderFogQuads(event, x, y, z, width, height, depth,
                    shiningAffectsFog.get() ? finalColor : color);
        }

        if (enableRadiatingOutline.get()) {
            renderRadiatingOutlineQuads(event, player,
                    shiningAffectsFog.get() ? finalColor : color);
        }
    }

    private void renderBoxWithQuads(Render3DEvent event, double x, double y, double z,
                                    double width, double height, Color color) {
        double x2 = x + width;
        double y2 = y + height;
        double z2 = z + width;

        event.renderer.triangles.ensureCapacity(4 * 6, 6 * 6);

        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(x, y, z).color(color).next(),
                event.renderer.triangles.vec3(x2, y, z).color(color).next(),
                event.renderer.triangles.vec3(x2, y2, z).color(color).next(),
                event.renderer.triangles.vec3(x, y2, z).color(color).next()
        );
        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(x, y, z2).color(color).next(),
                event.renderer.triangles.vec3(x2, y, z2).color(color).next(),
                event.renderer.triangles.vec3(x2, y2, z2).color(color).next(),
                event.renderer.triangles.vec3(x, y2, z2).color(color).next()
        );
        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(x, y, z).color(color).next(),
                event.renderer.triangles.vec3(x, y, z2).color(color).next(),
                event.renderer.triangles.vec3(x, y2, z2).color(color).next(),
                event.renderer.triangles.vec3(x, y2, z).color(color).next()
        );
        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(x2, y, z).color(color).next(),
                event.renderer.triangles.vec3(x2, y, z2).color(color).next(),
                event.renderer.triangles.vec3(x2, y2, z2).color(color).next(),
                event.renderer.triangles.vec3(x2, y2, z).color(color).next()
        );
        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(x, y2, z).color(color).next(),
                event.renderer.triangles.vec3(x2, y2, z).color(color).next(),
                event.renderer.triangles.vec3(x2, y2, z2).color(color).next(),
                event.renderer.triangles.vec3(x, y2, z2).color(color).next()
        );
        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(x, y, z).color(color).next(),
                event.renderer.triangles.vec3(x2, y, z).color(color).next(),
                event.renderer.triangles.vec3(x2, y, z2).color(color).next(),
                event.renderer.triangles.vec3(x, y, z2).color(color).next()
        );
    }

    private Color applyShiningEffect(Color color) {
        double shineValue;

        switch (shiningPattern.get()) {
            case Pulse:
                shineValue = (Math.sin(animationProgress * shiningSpeed.get()) + 1) * 0.5;
                break;
            case Breathe:
                shineValue = (Math.sin(animationProgress * shiningSpeed.get() * 0.5) + 1) * 0.5;
                break;
            case Flash:
                shineValue = Math.sin(animationProgress * shiningSpeed.get()) > 0 ? 1.0 : 0.2;
                break;
            case Wave:
                shineValue = (Math.sin(animationProgress * shiningSpeed.get() + Math.PI * 0.5) + 1) * 0.5;
                break;
            case Strobe:
                shineValue = Math.sin(animationProgress * shiningSpeed.get() * 2) > 0.7 ? 1.0 : 0.1;
                break;
            case Rave:
                double base = (Math.sin(animationProgress * shiningSpeed.get()) + 1) * 0.5;
                double pulse = (Math.sin(animationProgress * shiningSpeed.get() * 3) + 1) * 0.5;
                shineValue = base * pulse;
                break;
            default:
                shineValue = 1.0;
                break;
        }

        int r = (int) Math.min(255, color.r * (1 + shineValue * shiningIntensity.get()));
        int g = (int) Math.min(255, color.g * (1 + shineValue * shiningIntensity.get()));
        int b = (int) Math.min(255, color.b * (1 + shineValue * shiningIntensity.get()));
        int a = color.a;

        return new Color(r, g, b, a);
    }

    private void renderFog(Render3DEvent event, double x, double y, double z,
                           double width, double height, Color color) {
        double pulseModifier = foggyPulse.get() ?
                (Math.sin(animationProgress * foggyPulseSpeed.get()) + 1) * 0.5 + 0.5 : 1.0;

        for (int i = 1; i <= foggyLayers.get(); i++) {
            double layerSize = 1.0 + (i * 0.08 * foggyIntensity.get() * foggySize.get() * pulseModifier);
            int fogAlpha = (int) (color.a * (0.8 / i) * foggyIntensity.get());
            Color fogColor = new Color(color.r, color.g, color.b, Math.min(fogAlpha, 150));

            double fogX = x - (width * (layerSize - 1) / 2);
            double fogY = y - (height * (layerSize - 1) / 2);
            double fogZ = z - (width * (layerSize - 1) / 2);
            double fogWidth = width * layerSize;
            double fogHeight = height * layerSize;

            event.renderer.box(fogX, fogY, fogZ, fogX + fogWidth, fogY + fogHeight, fogZ + fogWidth,
                    fogColor, fogColor, ShapeMode.Sides, 0);

            if (i % 2 == 0) {
                event.renderer.box(fogX, fogY, fogZ, fogX + fogWidth, fogY + fogHeight, fogZ + fogWidth,
                        fogColor, fogColor, ShapeMode.Lines, 0);
            }
        }
    }

    private void renderFogQuads(Render3DEvent event, double x, double y, double z,
                                double width, double height, double depth, Color color) {
        double pulseModifier = foggyPulse.get() ?
                (Math.sin(animationProgress * foggyPulseSpeed.get()) + 1) * 0.5 + 0.5 : 1.0;

        for (int i = 1; i <= foggyLayers.get(); i++) {
            double layerSize = 1.0 + (i * 0.08 * foggyIntensity.get() * foggySize.get() * pulseModifier);
            int fogAlpha = (int) (color.a * (0.8 / i) * foggyIntensity.get());
            Color fogColor = new Color(color.r, color.g, color.b, Math.min(fogAlpha, 150));

            double fogX = x - (width * (layerSize - 1) / 2);
            double fogY = y - (height * (layerSize - 1) / 2);
            double fogZ = z - (depth * (layerSize - 1) / 2);
            double fogWidth = width * layerSize;
            double fogHeight = height * layerSize;
            double fogDepth = depth * layerSize;

            // Render foggy effect with enhanced quads
            renderBoxWithQuadsEnhanced(event, fogX, fogY, fogZ, fogWidth, fogHeight, fogDepth, fogColor);
        }
    }

    private void renderRadiatingOutline(Render3DEvent event, Player player, Color color) {
        List<Double> progressList = radiatingProgress.get(player);
        if (progressList == null || progressList.isEmpty()) return;

        double x = player.getX() - player.getBbWidth() / 2;
        double y = player.getY();
        double z = player.getZ() - player.getBbWidth() / 2;
        double width = player.getBbWidth();
        double height = player.getBbHeight();

        for (int i = 0; i < progressList.size(); i++) {
            double progress = progressList.get(i);
            if (progress == 0.0) continue;

            double waveSize = 1.0 + (progress * radiatingDistance.get() * (1 + i * 0.08));
            double easedProgress = 1 - Math.pow(1 - progress, 1.5);
            int waveAlpha = (int) (color.a * (1.0 - easedProgress) * (0.9 + i * 0.04));

            double hueShift = progress * Math.PI * 4 + i * 0.5;
            int r = Math.min(255, color.r + (int) (25 * Math.sin(hueShift)));
            int g = Math.min(255, color.g + (int) (25 * Math.sin(hueShift + Math.PI * 0.66)));
            int b = Math.min(255, color.b + (int) (25 * Math.sin(hueShift + Math.PI * 1.33)));

            Color waveColor = new Color(r, g, b, waveAlpha);

            double waveX = x - (width * (waveSize - 1) / 2);
            double waveY = y - (height * (waveSize - 1) / 2);
            double waveZ = z - (width * (waveSize - 1) / 2);
            double waveWidth = width * waveSize;
            double waveHeight = height * waveSize;

            int layers = (int) Math.ceil(radiatingThickness.get());
            for (int j = 0; j < layers; j++) {
                double thicknessFactor = 1.0 - (j / (double) layers);
                double offset = j * 0.04 * thicknessFactor;
                int layerAlpha = (int) (waveAlpha * thicknessFactor * 0.8);

                Color layerColor = new Color(
                        waveColor.r,
                        waveColor.g,
                        waveColor.b,
                        Math.max(20, layerAlpha)
                );

                event.renderer.box(
                        waveX - offset, waveY - offset, waveZ - offset,
                        waveX + waveWidth + offset, waveY + waveHeight + offset, waveZ + waveWidth + offset,
                        layerColor, layerColor, ShapeMode.Lines, 0
                );

                Color glowColor = new Color(
                        layerColor.r,
                        layerColor.g,
                        layerColor.b,
                        (int) (layerAlpha * 0.5 * thicknessFactor)
                );

                event.renderer.box(
                        waveX - offset, waveY - offset, waveZ - offset,
                        waveX + waveWidth + offset, waveY + waveHeight + offset, waveZ + waveWidth + offset,
                        glowColor, glowColor, ShapeMode.Sides, 0
                );
            }

            Color innerGlow = new Color(
                    waveColor.r, waveColor.g, waveColor.b,
                    (int) (waveAlpha * 0.4 * (1.0 - progress))
            );
            event.renderer.box(
                    waveX, waveY, waveZ,
                    waveX + waveWidth, waveY + waveHeight, waveZ + waveWidth,
                    innerGlow, innerGlow, ShapeMode.Sides, 0
            );
        }
    }

    private void renderRadiatingOutlineQuads(Render3DEvent event, Player player, Color color) {
        List<Double> progressList = radiatingProgress.get(player);
        if (progressList == null || progressList.isEmpty()) return;

        double x = player.getX() - player.getBbWidth() / 2;
        double y = player.getY();
        double z = player.getZ() - player.getBbWidth() / 2;
        double width = player.getBbWidth();
        double height = player.getBbHeight();

        for (int i = 0; i < progressList.size(); i++) {
            double progress = progressList.get(i);
            if (progress == 0.0) continue;

            double waveSize = 1.0 + (progress * radiatingDistance.get() * (1 + i * 0.08));
            double easedProgress = 1 - Math.pow(1 - progress, 1.5);
            int waveAlpha = (int) (color.a * (1.0 - easedProgress) * (0.9 + i * 0.04));

            double hueShift = progress * Math.PI * 4 + i * 0.5;
            int r = Math.min(255, color.r + (int) (25 * Math.sin(hueShift)));
            int g = Math.min(255, color.g + (int) (25 * Math.sin(hueShift + Math.PI * 0.66)));
            int b = Math.min(255, color.b + (int) (25 * Math.sin(hueShift + Math.PI * 1.33)));

            Color waveColor = new Color(r, g, b, waveAlpha);

            double waveX = x - (width * (waveSize - 1) / 2);
            double waveY = y - (height * (waveSize - 1) / 2);
            double waveZ = z - (width * (waveSize - 1) / 2);
            double waveWidth = width * waveSize;
            double waveHeight = height * waveSize;

            int layers = (int) Math.ceil(radiatingThickness.get());
            for (int j = 0; j < layers; j++) {
                double thicknessFactor = 1.0 - (j / (double) layers);
                double offset = j * 0.04 * thicknessFactor;
                int layerAlpha = (int) (waveAlpha * thicknessFactor * 0.8);

                Color layerColor = new Color(
                        waveColor.r,
                        waveColor.g,
                        waveColor.b,
                        Math.max(20, layerAlpha)
                );

                // Use quad rendering for smoother outlines
                renderBoxWithQuads(event,
                        waveX - offset, waveY - offset, waveZ - offset,
                        waveWidth + offset * 2, waveHeight + offset * 2, layerColor);
            }
        }
    }

    private void renderParticleTrail(Render3DEvent event, Player player, Color color) {
        List<Particle> particles = particleTrails.get(player);
        if (particles == null || particles.isEmpty()) return;

        for (Particle particle : particles) {
            double alpha = (double) particle.lifetime / 40.0;
            int particleAlpha = (int) (color.a * alpha * 0.8);
            Color particleColor = new Color(color.r, color.g, color.b, particleAlpha);

            double size = 0.1 + (0.1 * (1 - alpha));
            event.renderer.box(
                    particle.x - size, particle.y - size, particle.z - size,
                    particle.x + size, particle.y + size, particle.z + size,
                    particleColor, particleColor, ShapeMode.Both, 0
            );
        }
    }

    private void renderBoxWithQuadsEnhanced(Render3DEvent event, double x, double y, double z,
                                            double width, double height, double depth, Color baseColor) {
        double x2 = x + width;
        double y2 = y + height;
        double z2 = z + depth;

        int vertSegments = verticalSegments.get();
        int horizSegments = horizontalSegments.get();

        // Render each face with enhanced segmentation and gradients
        renderQuadFaceEnhanced(event, x, y, z, x2, y, z, x, y2, z, baseColor, vertSegments, horizSegments, false); // Front
        renderQuadFaceEnhanced(event, x, y, z2, x, y, z, x, y2, z2, baseColor, vertSegments, horizSegments, true); // Left
        renderQuadFaceEnhanced(event, x2, y, z, x2, y, z2, x2, y2, z, baseColor, vertSegments, horizSegments, true); // Right
        renderQuadFaceEnhanced(event, x, y, z2, x2, y, z2, x, y, z, baseColor, vertSegments, horizSegments, false); // Bottom
        renderQuadFaceEnhanced(event, x, y2, z, x2, y2, z, x, y2, z2, baseColor, vertSegments, horizSegments, false); // Top
        renderQuadFaceEnhanced(event, x2, y, z2, x, y, z2, x2, y2, z2, baseColor, vertSegments, horizSegments, true); // Back

        // Add edge highlights if enabled
        if (enableEdgeHighlights.get()) {
            renderEdgeHighlights(event, x, y, z, width, height, depth, baseColor);
        }
    }

    private void renderQuadFaceEnhanced(Render3DEvent event,
                                        double x1, double y1, double z1,
                                        double x2, double y2, double z2,
                                        double x4, double y4, double z4,
                                        Color baseColor, int vertSegments, int horizSegments, boolean isVerticalFace) {
        // Calculate vectors for the face
        double dx1 = (x2 - x1) / horizSegments;
        double dy1 = (y2 - y1) / horizSegments;
        double dz1 = (z2 - z1) / horizSegments;

        double dx2 = (x4 - x1) / vertSegments;
        double dy2 = (y4 - y1) / vertSegments;
        double dz2 = (z4 - z1) / vertSegments;

        for (int i = 0; i < horizSegments; i++) {
            for (int j = 0; j < vertSegments; j++) {
                // Calculate the four corners of the sub-quad
                double sx1 = x1 + i * dx1 + j * dx2;
                double sy1 = y1 + i * dy1 + j * dy2;
                double sz1 = z1 + i * dz1 + j * dz2;

                double sx2 = x1 + (i + 1) * dx1 + j * dx2;
                double sy2 = y1 + (i + 1) * dy1 + j * dy2;
                double sz2 = z1 + (i + 1) * dz1 + j * dz2;

                double sx3 = x1 + (i + 1) * dx1 + (j + 1) * dx2;
                double sy3 = y1 + (i + 1) * dy1 + (j + 1) * dy2;
                double sz3 = z1 + (i + 1) * dz1 + (j + 1) * dz2;

                double sx4 = x1 + i * dx1 + (j + 1) * dx2;
                double sy4 = y1 + i * dy1 + (j + 1) * dy2;
                double sz4 = z1 + i * dz1 + (j + 1) * dz2;

                // Calculate gradient color if enabled
                Color quadColor = baseColor;
                if (enableGradient.get()) {
                    double progress;
                    if (isVerticalFace) {
                        progress = (double) j / vertSegments;
                    } else {
                        progress = (double) i / horizSegments;
                    }

                    quadColor = interpolateColor(gradientTopColor.get(), gradientBottomColor.get(), progress);

                    // Apply base color alpha
                    quadColor = new Color(quadColor.r, quadColor.g, quadColor.b, baseColor.a);
                }


                event.renderer.triangles.ensureQuadCapacity();
                event.renderer.triangles.quad(
                        event.renderer.triangles.vec3(sx1, sy1, sz1).color(quadColor).next(),
                        event.renderer.triangles.vec3(sx2, sy2, sz2).color(quadColor).next(),
                        event.renderer.triangles.vec3(sx3, sy3, sz3).color(quadColor).next(),
                        event.renderer.triangles.vec3(sx4, sy4, sz4).color(quadColor).next()
                );
            }
        }
    }

    private void renderEdgeHighlights(Render3DEvent event, double x, double y, double z,
                                      double width, double height, double depth, Color baseColor) {
        double x2 = x + width;
        double y2 = y + height;
        double z2 = z + depth;

        // Calculate highlight color
        int highlightAlpha = (int) Math.min(255, baseColor.a * edgeHighlightIntensity.get());
        Color highlightColor = new Color(255, 255, 255, highlightAlpha);

        // Render edges with highlights
        double edgeSize = 0.02;

        // Vertical edges
        renderEdgeQuad(event, x, y, z, x, y2, z, edgeSize, highlightColor);
        renderEdgeQuad(event, x2, y, z, x2, y2, z, edgeSize, highlightColor);
        renderEdgeQuad(event, x, y, z2, x, y2, z2, edgeSize, highlightColor);
        renderEdgeQuad(event, x2, y, z2, x2, y2, z2, edgeSize, highlightColor);

        // Horizontal edges
        renderEdgeQuad(event, x, y, z, x2, y, z, edgeSize, highlightColor);
        renderEdgeQuad(event, x, y2, z, x2, y2, z, edgeSize, highlightColor);
        renderEdgeQuad(event, x, y, z2, x2, y, z2, edgeSize, highlightColor);
        renderEdgeQuad(event, x, y2, z2, x2, y2, z2, edgeSize, highlightColor);

        // Depth edges
        renderEdgeQuad(event, x, y, z, x, y, z2, edgeSize, highlightColor);
        renderEdgeQuad(event, x2, y, z, x2, y, z2, edgeSize, highlightColor);
        renderEdgeQuad(event, x, y2, z, x, y2, z2, edgeSize, highlightColor);
        renderEdgeQuad(event, x2, y2, z, x2, y2, z2, edgeSize, highlightColor);
    }

    private void renderEdgeQuad(Render3DEvent event, double x1, double y1, double z1,
                                double x2, double y2, double z2, double size, Color color) {
        // Calculate direction vector
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        // Calculate perpendicular vectors for the quad
        // This is a simplified approach - you might want to improve this
        double perpX = 0, perpY = 0, perpZ = 0;
        if (dx != 0) {
            perpY = size;
        } else if (dy != 0) {
            perpZ = size;
        } else if (dz != 0) {
            perpX = size;
        }

        // Render the edge quad
        event.renderer.triangles.ensureQuadCapacity();
        event.renderer.triangles.quad(
                event.renderer.triangles.vec3(x1 - perpX, y1 - perpY, z1 - perpZ).color(color).next(),
                event.renderer.triangles.vec3(x2 - perpX, y2 - perpY, z2 - perpZ).color(color).next(),
                event.renderer.triangles.vec3(x2 + perpX, y2 + perpY, z2 + perpZ).color(color).next(),
                event.renderer.triangles.vec3(x1 + perpX, y1 + perpY, z1 + perpZ).color(color).next()
        );
    }

    private Color interpolateColor(Color color1, Color color2, double progress) {
        int r = (int) (color1.r + (color2.r - color1.r) * progress);
        int g = (int) (color1.g + (color2.g - color1.g) * progress);
        int b = (int) (color1.b + (color2.b - color1.b) * progress);
        int a = (int) (color1.a + (color2.a - color1.a) * progress);

        return new Color(r, g, b, a);
    }

    private static class Particle {
        double x, y, z;
        double motionX, motionY, motionZ;
        int lifetime;

        Particle(double x, double y, double z, double motionX, double motionY, double motionZ, int lifetime) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            this.lifetime = lifetime;
        }
    }

    private enum ShiningPattern {
        Pulse,
        Breathe,
        Flash,
        Wave,
        Strobe,
        Rave
    }

    private static class PositionData {
        double x, y, z;
        long timestamp;
        Color color;
        boolean isMoving;

        PositionData(double x, double y, double z, Color color, boolean isMoving) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = System.currentTimeMillis();
            this.color = color;
            this.isMoving = isMoving;
        }
    }
}