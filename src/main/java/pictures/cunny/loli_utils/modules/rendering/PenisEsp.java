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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PenisEsp extends LoliModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSelf = settings.createGroup("Self");
    private final SettingGroup sgOthers = settings.createGroup("Others");
    private final SettingGroup sgVisual = settings.createGroup("Visual");
    private final SettingGroup sgShaft = settings.createGroup("Shaft");
    private final SettingGroup sgGlans = settings.createGroup("Glans");
    private final SettingGroup sgBalls = settings.createGroup("Balls");
    private final SettingGroup sgRendering = settings.createGroup("Rendering"); // New group

    // General Settings
    private final Setting<Double> maxDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("max-distance")
            .description("Maximum distance to render penis ESP.")
            .defaultValue(64.0)
            .min(1.0)
            .max(512.0)
            .sliderMax(128.0)
            .build()
    );

    private final Setting<Double> offsetStanding = sgGeneral.add(new DoubleSetting.Builder()
            .name("offset-standing")
            .description("Offset the penis when standing.")
            .defaultValue(-0.6)
            .sliderRange(-1, 1)
            .build()
    );

    private final Setting<Double> offsetSneaking = sgGeneral.add(new DoubleSetting.Builder()
            .name("offset-sneaking")
            .description("Offset the penis when sneaking.")
            .defaultValue(-0.6)
            .sliderRange(-1, 1)
            .build()
    );

    private final Setting<Boolean> sizeVariation = sgGeneral.add(new BoolSetting.Builder()
            .name("size-variation")
            .description("Enable size variation based on player UUID.")
            .defaultValue(true)
            .build()
    );

    // Self Settings
    private final Setting<Boolean> renderSelf = sgSelf.add(new BoolSetting.Builder()
            .name("render-self")
            .description("Render penis on yourself.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> selfSize = sgSelf.add(new DoubleSetting.Builder()
            .name("self-size")
            .description("Size of your penis.")
            .defaultValue(1.0)
            .min(0.1)
            .max(3.0)
            .visible(renderSelf::get)
            .build()
    );

    // Others Settings
    private final Setting<Boolean> renderOthers = sgOthers.add(new BoolSetting.Builder()
            .name("render-others")
            .description("Render penis on other players.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> othersSize = sgOthers.add(new DoubleSetting.Builder()
            .name("others-size")
            .description("Size of other players' penis.")
            .defaultValue(1.0)
            .min(0.1)
            .max(3.0)
            .visible(renderOthers::get)
            .build()
    );

    // Visual Settings
    private final Setting<Boolean> showBalls = sgVisual.add(new BoolSetting.Builder()
            .name("show-balls")
            .description("Render balls with the penis.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> glowEffect = sgVisual.add(new BoolSetting.Builder()
            .name("glow-effect")
            .description("Add a glowing effect to the penis.")
            .defaultValue(false)
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

    private final Setting<Boolean> enableTexture = sgVisual.add(new BoolSetting.Builder()
            .name("enable-texture")
            .description("Add texture details to the skin.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> textureDetail = sgVisual.add(new DoubleSetting.Builder()
            .name("texture-detail")
            .description("Level of texture detail.")
            .defaultValue(0.5)
            .min(0.1)
            .max(1.0)
            .visible(enableTexture::get)
            .build()
    );

    // Rendering Settings (NEW)
    private final Setting<Boolean> filledRendering = sgRendering.add(new BoolSetting.Builder()
            .name("filled-rendering")
            .description("Render filled shapes instead of wireframes.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> smoothingQuality = sgRendering.add(new IntSetting.Builder()
            .name("smoothing-quality")
            .description("Quality of smoothing (number of polygons).")
            .defaultValue(24)
            .min(8)
            .max(64)
            .sliderRange(8, 48)
            .build()
    );

    private final Setting<Boolean> useQuadRendering = sgRendering.add(new BoolSetting.Builder()
            .name("use-quad-rendering")
            .description("Use quad-based rendering for smoother surfaces.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> quadDensity = sgRendering.add(new IntSetting.Builder()
            .name("quad-density")
            .description("Density of quad rendering (higher = smoother).")
            .defaultValue(16)
            .min(4)
            .max(32)
            .sliderRange(4, 24)
            .visible(useQuadRendering::get)
            .build()
    );

    // Shaft Settings
    private final Setting<Double> shaftLength = sgShaft.add(new DoubleSetting.Builder()
            .name("shaft-length")
            .description("Length of the penis shaft.")
            .defaultValue(0.9)
            .min(0.1)
            .max(2.0)
            .sliderMax(1.5)
            .build()
    );

    private final Setting<Double> shaftBaseRadius = sgShaft.add(new DoubleSetting.Builder()
            .name("shaft-base-radius")
            .description("Radius at the base of the shaft.")
            .defaultValue(0.12)
            .min(0.05)
            .max(0.3)
            .sliderMax(0.2)
            .build()
    );

    private final Setting<Double> shaftTipRadius = sgShaft.add(new DoubleSetting.Builder()
            .name("shaft-tip-radius")
            .description("Radius at the tip of the shaft.")
            .defaultValue(0.08)
            .min(0.03)
            .max(0.2)
            .sliderMax(0.15)
            .build()
    );

    private final Setting<SettingColor> shaftColor = sgShaft.add(new ColorSetting.Builder()
            .name("shaft-color")
            .description("Color of the shaft.")
            .defaultValue(new SettingColor(200, 160, 220, 200))
            .build()
    );

    private final Setting<Integer> shaftSegments = sgShaft.add(new IntSetting.Builder()
            .name("shaft-segments")
            .description("Number of segments for shaft rendering.")
            .defaultValue(16)
            .min(8)
            .max(32)
            .build()
    );

    private final Setting<Boolean> renderVeins = sgShaft.add(new BoolSetting.Builder()
            .name("render-veins")
            .description("Render veins on the shaft.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> veinCount = sgShaft.add(new IntSetting.Builder()
            .name("vein-count")
            .description("Number of veins to render.")
            .defaultValue(3)
            .min(1)
            .max(6)
            .visible(renderVeins::get)
            .build()
    );

    private final Setting<Double> veinSize = sgShaft.add(new DoubleSetting.Builder()
            .name("vein-size")
            .description("Size of the veins.")
            .defaultValue(0.015)
            .min(0.005)
            .max(0.03)
            .visible(renderVeins::get)
            .build()
    );

    private final Setting<SettingColor> veinColor = sgShaft.add(new ColorSetting.Builder()
            .name("vein-color")
            .description("Color of the veins.")
            .defaultValue(new SettingColor(170, 130, 190, 180)) // Slightly darker purple
            .visible(renderVeins::get)
            .build()
    );

    private final Setting<Boolean> shaftSizeVariation = sgShaft.add(new BoolSetting.Builder()
            .name("shaft-size-variation")
            .description("Enable shaft size variation based on player UUID.")
            .defaultValue(true)
            .visible(sizeVariation::get)
            .build()
    );

    // Glans Settings
    private final Setting<Boolean> renderGlans = sgGlans.add(new BoolSetting.Builder()
            .name("render-glans")
            .description("Render the glans (tip).")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> glansLength = sgGlans.add(new DoubleSetting.Builder()
            .name("glans-length")
            .description("Length of the glans.")
            .defaultValue(0.15)
            .min(0.05)
            .max(0.4)
            .sliderMax(0.3)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<Double> glansBaseRadius = sgGlans.add(new DoubleSetting.Builder()
            .name("glans-base-radius")
            .description("Radius at the base of the glans.")
            .defaultValue(0.112)
            .min(0.05)
            .max(0.3)
            .sliderMax(0.2)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<SettingColor> glansColor = sgGlans.add(new ColorSetting.Builder()
            .name("glans-color")
            .description("Color of the glans.")
            .defaultValue(new SettingColor(220, 180, 240, 220))
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<Integer> glansSegments = sgGlans.add(new IntSetting.Builder()
            .name("glans-segments")
            .description("Number of segments for glans rendering.")
            .defaultValue(12)
            .min(8)
            .max(24)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<Boolean> renderCorona = sgGlans.add(new BoolSetting.Builder()
            .name("render-corona")
            .description("Render the corona (ridge) of the glans.")
            .defaultValue(true)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<Double> coronaProminence = sgGlans.add(new DoubleSetting.Builder()
            .name("corona-prominence")
            .description("Prominence of the corona ridge.")
            .defaultValue(0.05)
            .min(0.01)
            .max(0.15)
            .visible(() -> renderGlans.get() && renderCorona.get())
            .build()
    );

    private final Setting<Boolean> renderUrethra = sgGlans.add(new BoolSetting.Builder()
            .name("render-urethra")
            .description("Render the urethral opening.")
            .defaultValue(true)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<Double> urethraSize = sgGlans.add(new DoubleSetting.Builder()
            .name("urethra-size")
            .description("Size of the urethral opening.")
            .defaultValue(0.02)
            .min(0.005)
            .max(0.05)
            .visible(() -> renderGlans.get() && renderUrethra.get())
            .build()
    );

    private final Setting<SettingColor> urethraColor = sgGlans.add(new ColorSetting.Builder()
            .name("urethra-color")
            .description("Color of the urethral opening.")
            .defaultValue(new SettingColor(160, 100, 180, 220))
            .visible(() -> renderGlans.get() && renderUrethra.get())
            .build()
    );

    private final Setting<Boolean> glansSizeVariation = sgGlans.add(new BoolSetting.Builder()
            .name("glans-size-variation")
            .description("Enable glans size variation based on player UUID.")
            .defaultValue(true)
            .visible(sizeVariation::get)
            .build()
    );

    private final Setting<Boolean> renderGlansDetail = sgGlans.add(new BoolSetting.Builder()
            .name("render-glans-detail")
            .description("Render detailed anatomical features of the glans.")
            .defaultValue(true)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<Double> glansRoundness = sgGlans.add(new DoubleSetting.Builder()
            .name("glans-roundness")
            .description("How rounded the glans appears.")
            .defaultValue(0.8)
            .min(0.3)
            .max(1.2)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<Double> coronaDefinition = sgGlans.add(new DoubleSetting.Builder()
            .name("corona-definition")
            .description("Definition sharpness of the corona ridge.")
            .defaultValue(0.7)
            .min(0.1)
            .max(1.5)
            .visible(() -> renderGlans.get() && renderCorona.get())
            .build()
    );

    private final Setting<Boolean> renderFrenulum = sgGlans.add(new BoolSetting.Builder()
            .name("render-frenulum")
            .description("Render the frenulum (banjo string).")
            .defaultValue(true)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<SettingColor> frenulumColor = sgGlans.add(new ColorSetting.Builder()
            .name("frenulum-color")
            .description("Color of the frenulum.")
            .defaultValue(new SettingColor(150, 100, 170, 240))
            .visible(() -> renderGlans.get() && renderFrenulum.get())
            .build()
    );

    private final Setting<Boolean> renderMeatus = sgGlans.add(new BoolSetting.Builder()
            .name("render-meatus")
            .description("Render detailed meatus (urethral opening) structure.")
            .defaultValue(true)
            .visible(renderGlans::get)
            .build()
    );

    private final Setting<Double> meatusDepth = sgGlans.add(new DoubleSetting.Builder()
            .name("meatus-depth")
            .description("Depth of the urethral opening.")
            .defaultValue(0.03)
            .min(0.01)
            .max(0.08)
            .visible(() -> renderGlans.get() && renderMeatus.get())
            .build()
    );

    // Balls Settings
    private final Setting<Double> ballsRadius = sgBalls.add(new DoubleSetting.Builder()
            .name("balls-radius")
            .description("Radius of the balls.")
            .defaultValue(0.18)
            .min(0.05)
            .max(0.4)
            .sliderMax(0.3)
            .visible(showBalls::get)
            .build()
    );

    private final Setting<Double> ballsSeparation = sgBalls.add(new DoubleSetting.Builder()
            .name("balls-separation")
            .description("Separation between balls.")
            .defaultValue(0.15)
            .min(0.05)
            .max(0.3)
            .visible(showBalls::get)
            .build()
    );

    private final Setting<Double> leftBallSize = sgBalls.add(new DoubleSetting.Builder()
            .name("left-ball-size")
            .description("Size multiplier for the left ball.")
            .defaultValue(1.0)
            .min(0.5)
            .max(1.5)
            .visible(showBalls::get)
            .build()
    );

    private final Setting<Double> rightBallSize = sgBalls.add(new DoubleSetting.Builder()
            .name("right-ball-size")
            .description("Size multiplier for the right ball.")
            .defaultValue(0.95)
            .min(0.5)
            .max(1.5)
            .visible(showBalls::get)
            .build()
    );

    private final Setting<SettingColor> ballsColor = sgBalls.add(new ColorSetting.Builder()
            .name("balls-color")
            .description("Color of the balls.")
            .defaultValue(new SettingColor(190, 150, 210, 200))
            .visible(showBalls::get)
            .build()
    );

    private final Setting<Integer> ballsSegments = sgBalls.add(new IntSetting.Builder()
            .name("balls-segments")
            .description("Number of segments for ball rendering.")
            .defaultValue(12)
            .min(8)
            .max(20)
            .visible(showBalls::get)
            .build()
    );

    private final Setting<Boolean> renderScrotum = sgBalls.add(new BoolSetting.Builder()
            .name("render-scrotum")
            .description("Render the scrotum sack.")
            .defaultValue(true)
            .visible(showBalls::get)
            .build()
    );

    private final Setting<Double> scrotumSag = sgBalls.add(new DoubleSetting.Builder()
            .name("scrotum-sag")
            .description("Amount of scrotum sagging.")
            .defaultValue(0.1)
            .min(0.0)
            .max(0.3)
            .visible(() -> showBalls.get() && renderScrotum.get())
            .build()
    );

    private final Setting<Boolean> ballsSizeVariation = sgBalls.add(new BoolSetting.Builder()
            .name("balls-size-variation")
            .description("Enable balls size variation based on player UUID.")
            .defaultValue(true)
            .visible(sizeVariation::get)
            .build()
    );

    private final Setting<SettingColor> scrotumColor = sgBalls.add(new ColorSetting.Builder()
            .name("scrotum-color")
            .description("Color of the scrotum.")
            .defaultValue(new SettingColor(180, 140, 200, 180))
            .visible(() -> showBalls.get() && renderScrotum.get())
            .build()
    );

    private final Map<UUID, PenisData> playerPenisData = new HashMap<>();

    public PenisEsp() {
        super(LoliUtilsMeteor.CATEGORY, "penis-esp", "Renders a penis on players.");
    }

    @Override
    public void safeOnActivate() {
        playerPenisData.clear();
    }

    @Override
    public void onDeactivate() {
        playerPenisData.clear();
    }

    private void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        playerPenisData.entrySet().removeIf(entry ->
                currentTime - entry.getValue().lastUpdate > 5000
        );
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.level == null || mc.player == null) return;

        if (renderSelf.get()) {
            updatePlayerPenisData(mc.player.getUUID(), mc.player);
        }

        if (renderOthers.get()) {
            for (AbstractClientPlayer player : mc.level.players()) {
                if (player == mc.player) continue;

                double distance = mc.player.distanceTo(player);
                if (distance > maxDistance.get()) continue;

                updatePlayerPenisData(player.getUUID(), player);
            }
        }

        cleanupOldData();
    }

    private void updatePlayerPenisData(UUID playerId, Player player) {
        Vec3 playerPos = player.position();
        float yaw = player.getYRot();
        float bodyYaw = player.yBodyRot;
        float pitch = player.getXRot();
        boolean isMoving = player.getDeltaMovement().lengthSqr() > 0.001;

        PenisData data = playerPenisData.get(playerId);
        if (data == null) {
            data = new PenisData(playerId, playerPos, yaw, bodyYaw, pitch, isMoving);
            playerPenisData.put(playerId, data);
        } else {
            data.updatePosition(playerPos, yaw, bodyYaw, pitch, isMoving);
        }
    }

    private Vec3 calculateBasePosition(PenisData data) {
        Player player = mc.level.getPlayerByUUID(data.playerId);
        if (player == null) return data.position;

        double x = data.position.x;
        double y = data.position.y;
        double z = data.position.z;

        if (player.isCrouching()) {
            y += player.getEyeHeight() + offsetSneaking.get();
        } else {
            y += player.getEyeHeight() + offsetStanding.get();
        }

        float yawRad = (float) Math.toRadians(data.bodyYaw);

        double offsetDistance = 0.15 + 0.1;
        x += -Math.sin(yawRad) * offsetDistance;
        z += Math.cos(yawRad) * offsetDistance;

        return new Vec3(x, y, z);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        for (Map.Entry<UUID, PenisData> entry : playerPenisData.entrySet()) {
            UUID playerId = entry.getKey();
            PenisData data = entry.getValue();

            boolean isSelf = playerId.equals(mc.player.getUUID());
            double distance = mc.player.position().distanceTo(data.position);

            if (distance > maxDistance.get()) continue;

            double baseSize = (isSelf ? selfSize.get() : othersSize.get()) * (1.0 + distance / 64.0 * 0.5);
            double finalSize = sizeVariation.get() ? baseSize * data.sizeMultiplier : baseSize;

            renderPlayerPenis(event, data, finalSize);
        }
    }

    private void renderPlayerPenis(Render3DEvent event, PenisData data, double size) {
        Player player = mc.level.getPlayerByUUID(data.playerId);
        if (player == null) return;

        Vec3 basePosition = calculateBasePosition(data);
        double x = basePosition.x;
        double y = basePosition.y;
        double z = basePosition.z;

        double dirX = data.direction.x;
        double dirY = data.direction.y;
        double dirZ = data.direction.z;

        renderRealisticPenis(event, data, x, y, z, dirX, dirY, dirZ, size);

        if (glowEffect.get()) {
            for (int i = 1; i <= glowLayers.get(); i++) {
                double glowSize = size * (1 + i * 0.1 * glowIntensity.get());
                renderRealisticPenis(event, data, x, y, z, dirX, dirY, dirZ, glowSize);
            }
        }
    }

    private void renderRealisticPenis(Render3DEvent event, PenisData data, double x, double y, double z,
                                      double dirX, double dirY, double dirZ, double size) {
        double shaftSizeMultiplier = shaftSizeVariation.get() ? data.shaftMultiplier : 1.0;
        double glansSizeMultiplier = glansSizeVariation.get() ? data.glansMultiplier : 1.0;
        double ballsSizeMultiplier = ballsSizeVariation.get() ? data.ballsMultiplier : 1.0;

        double shaftLength = this.shaftLength.get() * size * shaftSizeMultiplier;
        double baseRadius = this.shaftBaseRadius.get() * size * shaftSizeMultiplier;
        double tipRadius = this.shaftTipRadius.get() * size * shaftSizeMultiplier;

        if (useQuadRendering.get()) {
            renderTexturedCylinderQuads(event, x, y, z, dirX, dirY, dirZ, shaftLength,
                    baseRadius, tipRadius, shaftColor.get(), quadDensity.get(),
                    enableTexture.get(), textureDetail.get(), filledRendering.get());
        } else {
            renderTexturedCylinder(event, x, y, z, dirX, dirY, dirZ, shaftLength,
                    baseRadius, tipRadius, shaftColor.get(), smoothingQuality.get(),
                    enableTexture.get(), textureDetail.get());
        }

        if (renderVeins.get()) {
            renderShaftVeins(event, x, y, z, dirX, dirY, dirZ, shaftLength,
                    baseRadius, tipRadius, veinSize.get() * size * shaftSizeMultiplier, veinColor.get(),
                    veinCount.get(), smoothingQuality.get());
        }

        if (renderGlans.get()) {
            double tipX = x + dirX * shaftLength;
            double tipY = y + dirY * shaftLength;
            double tipZ = z + dirZ * shaftLength;

            double glansLength = this.glansLength.get() * size * glansSizeMultiplier;
            double glansBaseRadius = this.glansBaseRadius.get() * size * glansSizeMultiplier;

            double glansRoundnessFactor = glansRoundness.get();
            double glansTipRadius = glansBaseRadius * 0.6 * glansRoundnessFactor;

            if (useQuadRendering.get()) {
                renderRoundedGlansQuads(event, tipX, tipY, tipZ, dirX, dirY, dirZ,
                        glansLength, glansBaseRadius, glansTipRadius, glansColor.get(),
                        quadDensity.get(), filledRendering.get());
            } else {
                renderRoundedGlans(event, tipX, tipY, tipZ, dirX, dirY, dirZ,
                        glansLength, glansBaseRadius, glansTipRadius, glansColor.get(),
                        smoothingQuality.get());
            }

            if (renderCorona.get()) {
                double coronaRadius = glansBaseRadius + (coronaProminence.get() * size * glansSizeMultiplier * coronaDefinition.get());
                renderDetailedCorona(event, tipX, tipY, tipZ, dirX, dirY, dirZ,
                        coronaRadius, glansColor.get(), smoothingQuality.get());
            }

            if (renderUrethra.get()) {
                double meatusX = tipX + dirX * glansLength * 0.9;
                double meatusY = tipY + dirY * glansLength * 0.9;
                double meatusZ = tipZ + dirZ * glansLength * 0.9;

                if (renderMeatus.get()) {
                    renderDetailedMeatus(event, meatusX, meatusY, meatusZ,
                            dirX, dirY, dirZ, urethraSize.get() * size * glansSizeMultiplier,
                            urethraColor.get(), meatusDepth.get() * size);
                } else {
                    renderUrethralOpening(event, meatusX, meatusY, meatusZ,
                            dirX, dirY, dirZ, urethraSize.get() * size * glansSizeMultiplier, urethraColor.get());
                }
            }

            if (renderFrenulum.get()) {
                renderFrenulum(event, tipX, tipY, tipZ, dirX, dirY, dirZ,
                        glansLength, frenulumColor.get());
            }

            if (renderGlansDetail.get()) {
                renderGlansDetails(event, tipX, tipY, tipZ, dirX, dirY, dirZ,
                        glansLength, glansBaseRadius, glansColor.get());
            }
        }

        if (showBalls.get()) {
            double perpX = -dirZ;
            double perpZ = dirX;
            double perpLength = Math.sqrt(perpX * perpX + perpZ * perpZ);
            perpX /= perpLength;
            perpZ /= perpLength;

            double ballRadius = ballsRadius.get() * size * ballsSizeMultiplier;
            double ballOffset = ballsSeparation.get() * size * ballsSizeMultiplier;

            if (renderScrotum.get()) {
                renderScrotumSack(event, x, y - ballRadius * 0.5, z,
                        ballOffset,
                        scrotumSag.get() * size * ballsSizeMultiplier, scrotumColor.get(), smoothingQuality.get());
            }

            double leftBallFinalSize = leftBallSize.get() * ballsSizeMultiplier;
            if (useQuadRendering.get()) {
                renderTexturedSphereQuads(event, x - perpX * ballOffset,
                        y - ballRadius * (0.9 + scrotumSag.get()),
                        z - perpZ * ballOffset, ballRadius * leftBallFinalSize,
                        ballsColor.get(), quadDensity.get(), enableTexture.get(), textureDetail.get(), filledRendering.get());
            } else {
                renderTexturedSphere(event, x - perpX * ballOffset,
                        y - ballRadius * (0.9 + scrotumSag.get()),
                        z - perpZ * ballOffset, ballRadius * leftBallFinalSize,
                        ballsColor.get(), smoothingQuality.get(), enableTexture.get(), textureDetail.get());
            }

            double rightBallFinalSize = rightBallSize.get() * ballsSizeMultiplier;
            if (useQuadRendering.get()) {
                renderTexturedSphereQuads(event, x + perpX * ballOffset,
                        y - ballRadius * (0.8 + scrotumSag.get()),
                        z + perpZ * ballOffset, ballRadius * rightBallFinalSize,
                        ballsColor.get(), quadDensity.get(), enableTexture.get(), textureDetail.get(), filledRendering.get());
            } else {
                renderTexturedSphere(event, x + perpX * ballOffset,
                        y - ballRadius * (0.8 + scrotumSag.get()),
                        z + perpZ * ballOffset, ballRadius * rightBallFinalSize,
                        ballsColor.get(), smoothingQuality.get(), enableTexture.get(), textureDetail.get());
            }
        }
    }

    // New quad-based rendering methods
    private void renderTexturedCylinderQuads(Render3DEvent event, double x, double y, double z,
                                             double dirX, double dirY, double dirZ,
                                             double length, double baseRadius, double tipRadius,
                                             Color color, int segments, boolean enableTexture,
                                             double textureDetail, boolean filled) {
        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();
        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?
                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();
        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();

        if (filled) {
            // Render filled cylinder with quads
            for (int i = 0; i < segments; i++) {
                double angle1 = 2 * Math.PI * i / segments;
                double angle2 = 2 * Math.PI * (i + 1) / segments;

                // Calculate points for the base
                double baseX1 = x + perpendicular.x * Math.cos(angle1) * baseRadius
                        + perpendicular2.x * Math.sin(angle1) * baseRadius;
                double baseY1 = y + perpendicular.y * Math.cos(angle1) * baseRadius
                        + perpendicular2.y * Math.sin(angle1) * baseRadius;
                double baseZ1 = z + perpendicular.z * Math.cos(angle1) * baseRadius
                        + perpendicular2.z * Math.sin(angle1) * baseRadius;

                double baseX2 = x + perpendicular.x * Math.cos(angle2) * baseRadius
                        + perpendicular2.x * Math.sin(angle2) * baseRadius;
                double baseY2 = y + perpendicular.y * Math.cos(angle2) * baseRadius
                        + perpendicular2.y * Math.sin(angle2) * baseRadius;
                double baseZ2 = z + perpendicular.z * Math.cos(angle2) * baseRadius
                        + perpendicular2.z * Math.sin(angle2) * baseRadius;

                // Calculate points for the tip
                double tipX = x + direction.x * length;
                double tipY = y + direction.y * length;
                double tipZ = z + direction.z * length;

                double tipX1 = tipX + perpendicular.x * Math.cos(angle1) * tipRadius
                        + perpendicular2.x * Math.sin(angle1) * tipRadius;
                double tipY1 = tipY + perpendicular.y * Math.cos(angle1) * tipRadius
                        + perpendicular2.y * Math.sin(angle1) * tipRadius;
                double tipZ1 = tipZ + perpendicular.z * Math.cos(angle1) * tipRadius
                        + perpendicular2.z * Math.sin(angle1) * tipRadius;

                double tipX2 = tipX + perpendicular.x * Math.cos(angle2) * tipRadius
                        + perpendicular2.x * Math.sin(angle2) * tipRadius;
                double tipY2 = tipY + perpendicular.y * Math.cos(angle2) * tipRadius
                        + perpendicular2.y * Math.sin(angle2) * tipRadius;
                double tipZ2 = tipZ + perpendicular.z * Math.cos(angle2) * tipRadius
                        + perpendicular2.z * Math.sin(angle2) * tipRadius;

                // Render side quad
                event.renderer.triangles.ensureQuadCapacity();
                event.renderer.triangles.quad(
                        event.renderer.triangles.vec3(baseX1, baseY1, baseZ1).color(color).next(),
                        event.renderer.triangles.vec3(baseX2, baseY2, baseZ2).color(color).next(),
                        event.renderer.triangles.vec3(tipX2, tipY2, tipZ2).color(color).next(),
                        event.renderer.triangles.vec3(tipX1, tipY1, tipZ1).color(color).next()
                );
            }
        } else {
            // Render wireframe cylinder
            for (int i = 0; i < segments; i++) {
                double angle1 = 2 * Math.PI * i / segments;
                double angle2 = 2 * Math.PI * (i + 1) / segments;

                double baseX1 = x + perpendicular.x * Math.cos(angle1) * baseRadius
                        + perpendicular2.x * Math.sin(angle1) * baseRadius;
                double baseY1 = y + perpendicular.y * Math.cos(angle1) * baseRadius
                        + perpendicular2.y * Math.sin(angle1) * baseRadius;
                double baseZ1 = z + perpendicular.z * Math.cos(angle1) * baseRadius
                        + perpendicular2.z * Math.sin(angle1) * baseRadius;

                double baseX2 = x + perpendicular.x * Math.cos(angle2) * baseRadius
                        + perpendicular2.x * Math.sin(angle2) * baseRadius;
                double baseY2 = y + perpendicular.y * Math.cos(angle2) * baseRadius
                        + perpendicular2.y * Math.sin(angle2) * baseRadius;
                double baseZ2 = z + perpendicular.z * Math.cos(angle2) * baseRadius
                        + perpendicular2.z * Math.sin(angle2) * baseRadius;

                double tipX = x + direction.x * length;
                double tipY = y + direction.y * length;
                double tipZ = z + direction.z * length;

                double tipX1 = tipX + perpendicular.x * Math.cos(angle1) * tipRadius
                        + perpendicular2.x * Math.sin(angle1) * tipRadius;
                double tipY1 = tipY + perpendicular.y * Math.cos(angle1) * tipRadius
                        + perpendicular2.y * Math.sin(angle1) * tipRadius;
                double tipZ1 = tipZ + perpendicular.z * Math.cos(angle1) * tipRadius
                        + perpendicular2.z * Math.sin(angle1) * tipRadius;

                double tipX2 = tipX + perpendicular.x * Math.cos(angle2) * tipRadius
                        + perpendicular2.x * Math.sin(angle2) * tipRadius;
                double tipY2 = tipY + perpendicular.y * Math.cos(angle2) * tipRadius
                        + perpendicular2.y * Math.sin(angle2) * tipRadius;
                double tipZ2 = tipZ + perpendicular.z * Math.cos(angle2) * tipRadius
                        + perpendicular2.z * Math.sin(angle2) * tipRadius;

                // Render base circle
                event.renderer.line(baseX1, baseY1, baseZ1, baseX2, baseY2, baseZ2, color);
                // Render tip circle
                event.renderer.line(tipX1, tipY1, tipZ1, tipX2, tipY2, tipZ2, color);
                // Render connecting lines
                event.renderer.line(baseX1, baseY1, baseZ1, tipX1, tipY1, tipZ1, color);
            }
        }
    }

    private void renderRoundedGlansQuads(Render3DEvent event, double x, double y, double z,
                                         double dirX, double dirY, double dirZ,
                                         double length, double baseRadius, double tipRadius,
                                         Color color, int segments, boolean filled) {
        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();
        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?
                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();
        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();

        if (filled) {
            // Render filled glans with quads
            for (int i = 0; i < segments; i++) {
                double angle1 = 2 * Math.PI * i / segments;
                double angle2 = 2 * Math.PI * (i + 1) / segments;

                // Calculate points for the base
                double baseX1 = x + perpendicular.x * Math.cos(angle1) * baseRadius
                        + perpendicular2.x * Math.sin(angle1) * baseRadius;
                double baseY1 = y + perpendicular.y * Math.cos(angle1) * baseRadius
                        + perpendicular2.y * Math.sin(angle1) * baseRadius;
                double baseZ1 = z + perpendicular.z * Math.cos(angle1) * baseRadius
                        + perpendicular2.z * Math.sin(angle1) * baseRadius;

                double baseX2 = x + perpendicular.x * Math.cos(angle2) * baseRadius
                        + perpendicular2.x * Math.sin(angle2) * baseRadius;
                double baseY2 = y + perpendicular.y * Math.cos(angle2) * baseRadius
                        + perpendicular2.y * Math.sin(angle2) * baseRadius;
                double baseZ2 = z + perpendicular.z * Math.cos(angle2) * baseRadius
                        + perpendicular2.z * Math.sin(angle2) * baseRadius;

                // Calculate points for the tip
                double tipX = x + direction.x * length;
                double tipY = y + direction.y * length;
                double tipZ = z + direction.z * length;

                double tipX1 = tipX + perpendicular.x * Math.cos(angle1) * tipRadius
                        + perpendicular2.x * Math.sin(angle1) * tipRadius;
                double tipY1 = tipY + perpendicular.y * Math.cos(angle1) * tipRadius
                        + perpendicular2.y * Math.sin(angle1) * tipRadius;
                double tipZ1 = tipZ + perpendicular.z * Math.cos(angle1) * tipRadius
                        + perpendicular2.z * Math.sin(angle1) * tipRadius;

                double tipX2 = tipX + perpendicular.x * Math.cos(angle2) * tipRadius
                        + perpendicular2.x * Math.sin(angle2) * tipRadius;
                double tipY2 = tipY + perpendicular.y * Math.cos(angle2) * tipRadius
                        + perpendicular2.y * Math.sin(angle2) * tipRadius;
                double tipZ2 = tipZ + perpendicular.z * Math.cos(angle2) * tipRadius
                        + perpendicular2.z * Math.sin(angle2) * tipRadius;

                // Render side quad
                event.renderer.triangles.ensureQuadCapacity();
                event.renderer.triangles.quad(
                        event.renderer.triangles.vec3(baseX1, baseY1, baseZ1).color(color).next(),
                        event.renderer.triangles.vec3(baseX2, baseY2, baseZ2).color(color).next(),
                        event.renderer.triangles.vec3(tipX2, tipY2, tipZ2).color(color).next(),
                        event.renderer.triangles.vec3(tipX1, tipY1, tipZ1).color(color).next()
                );

                // Render tip cap
                event.renderer.triangles.ensureTriCapacity();
                event.renderer.triangles.triangle(
                        event.renderer.triangles.vec3(tipX1, tipY1, tipZ1).color(color).next(),
                        event.renderer.triangles.vec3(tipX2, tipY2, tipZ2).color(color).next(),
                        event.renderer.triangles.vec3(tipX, tipY, tipZ).color(color).next()
                );
            }
        } else {
            // Render wireframe glans
            for (int i = 0; i < segments; i++) {
                double angle1 = 2 * Math.PI * i / segments;
                double angle2 = 2 * Math.PI * (i + 1) / segments;

                double baseX1 = x + perpendicular.x * Math.cos(angle1) * baseRadius
                        + perpendicular2.x * Math.sin(angle1) * baseRadius;
                double baseY1 = y + perpendicular.y * Math.cos(angle1) * baseRadius
                        + perpendicular2.y * Math.sin(angle1) * baseRadius;
                double baseZ1 = z + perpendicular.z * Math.cos(angle1) * baseRadius
                        + perpendicular2.z * Math.sin(angle1) * baseRadius;

                double baseX2 = x + perpendicular.x * Math.cos(angle2) * baseRadius
                        + perpendicular2.x * Math.sin(angle2) * baseRadius;
                double baseY2 = y + perpendicular.y * Math.cos(angle2) * baseRadius
                        + perpendicular2.y * Math.sin(angle2) * baseRadius;
                double baseZ2 = z + perpendicular.z * Math.cos(angle2) * baseRadius
                        + perpendicular2.z * Math.sin(angle2) * baseRadius;

                double tipX = x + direction.x * length;
                double tipY = y + direction.y * length;
                double tipZ = z + direction.z * length;

                double tipX1 = tipX + perpendicular.x * Math.cos(angle1) * tipRadius
                        + perpendicular2.x * Math.sin(angle1) * tipRadius;
                double tipY1 = tipY + perpendicular.y * Math.cos(angle1) * tipRadius
                        + perpendicular2.y * Math.sin(angle1) * tipRadius;
                double tipZ1 = tipZ + perpendicular.z * Math.cos(angle1) * tipRadius
                        + perpendicular2.z * Math.sin(angle1) * tipRadius;

                double tipX2 = tipX + perpendicular.x * Math.cos(angle2) * tipRadius
                        + perpendicular2.x * Math.sin(angle2) * tipRadius;
                double tipY2 = tipY + perpendicular.y * Math.cos(angle2) * tipRadius
                        + perpendicular2.y * Math.sin(angle2) * tipRadius;
                double tipZ2 = tipZ + perpendicular.z * Math.cos(angle2) * tipRadius
                        + perpendicular2.z * Math.sin(angle2) * tipRadius;

                // Render base circle
                event.renderer.line(baseX1, baseY1, baseZ1, baseX2, baseY2, baseZ2, color);
                // Render tip circle
                event.renderer.line(tipX1, tipY1, tipZ1, tipX2, tipY2, tipZ2, color);
                // Render connecting lines
                event.renderer.line(baseX1, baseY1, baseZ1, tipX1, tipY1, tipZ1, color);
                // Render lines to tip center
                event.renderer.line(tipX1, tipY1, tipZ1, tipX, tipY, tipZ, color);
            }
        }
    }

    private void renderTexturedSphereQuads(Render3DEvent event, double x, double y, double z,
                                           double radius, Color color, int segments,
                                           boolean enableTexture, double textureDetail, boolean filled) {
        if (filled) {
            // Render filled sphere with quads
            for (int i = 0; i < segments; i++) {
                double phi1 = Math.PI * i / segments;
                double phi2 = Math.PI * (i + 1) / segments;

                for (int j = 0; j < segments; j++) {
                    double theta1 = 2 * Math.PI * j / segments;
                    double theta2 = 2 * Math.PI * (j + 1) / segments;

                    // Calculate four points for the quad
                    double x1 = x + radius * Math.sin(phi1) * Math.cos(theta1);
                    double y1 = y + radius * Math.cos(phi1);
                    double z1 = z + radius * Math.sin(phi1) * Math.sin(theta1);

                    double x2 = x + radius * Math.sin(phi1) * Math.cos(theta2);
                    double y2 = y + radius * Math.cos(phi1);
                    double z2 = z + radius * Math.sin(phi1) * Math.sin(theta2);

                    double x3 = x + radius * Math.sin(phi2) * Math.cos(theta2);
                    double y3 = y + radius * Math.cos(phi2);
                    double z3 = z + radius * Math.sin(phi2) * Math.sin(theta2);

                    double x4 = x + radius * Math.sin(phi2) * Math.cos(theta1);
                    double y4 = y + radius * Math.cos(phi2);
                    double z4 = z + radius * Math.sin(phi2) * Math.sin(theta1);

                    // Render the quad
                    event.renderer.triangles.ensureQuadCapacity();
                    event.renderer.triangles.quad(
                            event.renderer.triangles.vec3(x1, y1, z1).color(color).next(),
                            event.renderer.triangles.vec3(x2, y2, z2).color(color).next(),
                            event.renderer.triangles.vec3(x3, y3, z3).color(color).next(),
                            event.renderer.triangles.vec3(x4, y4, z4).color(color).next()
                    );
                }
            }
        } else {
            // Render wireframe sphere
            for (int i = 0; i <= segments; i++) {
                double phi = Math.PI * i / segments;
                for (int j = 0; j < segments; j++) {
                    double theta1 = 2 * Math.PI * j / segments;
                    double theta2 = 2 * Math.PI * (j + 1) / segments;

                    double x1 = x + radius * Math.sin(phi) * Math.cos(theta1);
                    double y1 = y + radius * Math.cos(phi);
                    double z1 = z + radius * Math.sin(phi) * Math.sin(theta1);

                    double x2 = x + radius * Math.sin(phi) * Math.cos(theta2);
                    double y2 = y + radius * Math.cos(phi);
                    double z2 = z + radius * Math.sin(phi) * Math.sin(theta2);

                    event.renderer.line(x1, y1, z1, x2, y2, z2, color);
                }
            }

            for (int j = 0; j < segments; j++) {
                double theta = 2 * Math.PI * j / segments;
                for (int i = 0; i < segments; i++) {
                    double phi1 = Math.PI * i / segments;
                    double phi2 = Math.PI * (i + 1) / segments;

                    double x1 = x + radius * Math.sin(phi1) * Math.cos(theta);
                    double y1 = y + radius * Math.cos(phi1);
                    double z1 = z + radius * Math.sin(phi1) * Math.sin(theta);

                    double x2 = x + radius * Math.sin(phi2) * Math.cos(theta);
                    double y2 = y + radius * Math.cos(phi2);
                    double z2 = z + radius * Math.sin(phi2) * Math.sin(theta);

                    event.renderer.line(x1, y1, z1, x2, y2, z2, color);
                }
            }
        }
    }

    private void renderTexturedCylinder(Render3DEvent event, double x, double y, double z,

                                        double dirX, double dirY, double dirZ,

                                        double length, double baseRadius, double tipRadius,

                                        Color color, int segments, boolean enableTexture, double textureDetail) {

        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();

        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?

                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();

        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();


        for (int i = 0; i < segments; i++) {

            double angle1 = 2 * Math.PI * i / segments;

            double angle2 = 2 * Math.PI * (i + 1) / segments;


            double baseX1 = x + perpendicular.x * Math.cos(angle1) * baseRadius

                    + perpendicular2.x * Math.sin(angle1) * baseRadius;

            double baseY1 = y + perpendicular.y * Math.cos(angle1) * baseRadius

                    + perpendicular2.y * Math.sin(angle1) * baseRadius;

            double baseZ1 = z + perpendicular.z * Math.cos(angle1) * baseRadius

                    + perpendicular2.z * Math.sin(angle1) * baseRadius;


            double baseX2 = x + perpendicular.x * Math.cos(angle2) * baseRadius

                    + perpendicular2.x * Math.sin(angle2) * baseRadius;

            double baseY2 = y + perpendicular.y * Math.cos(angle2) * baseRadius

                    + perpendicular2.y * Math.sin(angle2) * baseRadius;

            double baseZ2 = z + perpendicular.z * Math.cos(angle2) * baseRadius

                    + perpendicular2.z * Math.sin(angle2) * baseRadius;


            double tipX = x + direction.x * length;

            double tipY = y + direction.y * length;

            double tipZ = z + direction.z * length;


            double tipX1 = tipX + perpendicular.x * Math.cos(angle1) * tipRadius

                    + perpendicular2.x * Math.sin(angle1) * tipRadius;

            double tipY1 = tipY + perpendicular.y * Math.cos(angle1) * tipRadius

                    + perpendicular2.y * Math.sin(angle1) * tipRadius;

            double tipZ1 = tipZ + perpendicular.z * Math.cos(angle1) * tipRadius

                    + perpendicular2.z * Math.sin(angle1) * tipRadius;


            double tipX2 = tipX + perpendicular.x * Math.cos(angle2) * tipRadius

                    + perpendicular2.x * Math.sin(angle2) * tipRadius;

            double tipY2 = tipY + perpendicular.y * Math.cos(angle2) * tipRadius

                    + perpendicular2.y * Math.sin(angle2) * tipRadius;

            double tipZ2 = tipZ + perpendicular.z * Math.cos(angle2) * tipRadius

                    + perpendicular2.z * Math.sin(angle2) * tipRadius;


            event.renderer.line(baseX1, baseY1, baseZ1, tipX1, tipY1, tipZ1, color);

            event.renderer.line(baseX1, baseY1, baseZ1, baseX2, baseY2, baseZ2, color);

            event.renderer.line(tipX1, tipY1, tipZ1, tipX2, tipY2, tipZ2, color);

        }


        if (enableTexture) {

            renderCylinderTexture(event, x, y, z, direction, perpendicular, perpendicular2,

                    length, baseRadius, tipRadius, color, segments, textureDetail);

        }

    }

    private void renderCylinderTexture(Render3DEvent event, double x, double y, double z,

                                       Vec3 direction, Vec3 perpendicular, Vec3 perpendicular2,

                                       double length, double baseRadius, double tipRadius,

                                       Color color, int segments, double textureDetail) {

        int textureLines = (int) (segments * textureDetail);

        for (int i = 0; i < textureLines; i++) {

            double progress = (double) i / textureLines;

            double currentRadius = baseRadius + (tipRadius - baseRadius) * progress;


            for (int j = 0; j < 4; j++) {

                double textureAngle = Math.PI * j / 2;

                double offset = Math.sin(progress * Math.PI * 4) * currentRadius * 0.05;


                double currentX = x + direction.x * length * progress;

                double currentY = y + direction.y * length * progress;

                double currentZ = z + direction.z * length * progress;


                double px1 = currentX + perpendicular.x * Math.cos(textureAngle) * (currentRadius + offset)

                        + perpendicular2.x * Math.sin(textureAngle) * (currentRadius + offset);

                double py1 = currentY + perpendicular.y * Math.cos(textureAngle) * (currentRadius + offset)

                        + perpendicular2.y * Math.sin(textureAngle) * (currentRadius + offset);

                double pz1 = currentZ + perpendicular.z * Math.cos(textureAngle) * (currentRadius + offset)

                        + perpendicular2.z * Math.sin(textureAngle) * (currentRadius + offset);


                double nextProgress = Math.min(1.0, progress + 0.1);

                double nextRadius = baseRadius + (tipRadius - baseRadius) * nextProgress;

                double nextX = x + direction.x * length * nextProgress;

                double nextY = y + direction.y * length * nextProgress;

                double nextZ = z + direction.z * length * nextProgress;

                double nextOffset = Math.sin(nextProgress * Math.PI * 4) * nextRadius * 0.05;


                double px2 = nextX + perpendicular.x * Math.cos(textureAngle) * (nextRadius + nextOffset)

                        + perpendicular2.x * Math.sin(textureAngle) * (nextRadius + nextOffset);

                double py2 = nextY + perpendicular.y * Math.cos(textureAngle) * (nextRadius + nextOffset)

                        + perpendicular2.y * Math.sin(textureAngle) * (nextRadius + nextOffset);

                double pz2 = nextZ + perpendicular.z * Math.cos(textureAngle) * (nextRadius + nextOffset)

                        + perpendicular2.z * Math.sin(textureAngle) * (nextRadius + nextOffset);


                event.renderer.line(px1, py1, pz1, px2, py2, pz2, color);

            }

        }

    }


    private void renderShaftVeins(Render3DEvent event, double x, double y, double z,

                                  double dirX, double dirY, double dirZ,

                                  double length, double baseRadius, double tipRadius,

                                  double veinSize, Color veinColor, int veinCount, int segments) {

        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();

        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?

                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();

        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();


        for (int veinIndex = 0; veinIndex < veinCount; veinIndex++) {

            double veinAngle = 2 * Math.PI * veinIndex / veinCount;

            double veinOffset = Math.sin(veinAngle) * 0.1;


            for (int i = 0; i < segments; i++) {

                double progress1 = (double) i / segments;

                double progress2 = (double) (i + 1) / segments;


                double radius1 = baseRadius + (tipRadius - baseRadius) * progress1;

                double radius2 = baseRadius + (tipRadius - baseRadius) * progress2;


                double veinVariation1 = Math.sin(progress1 * Math.PI * 8 + veinOffset) * veinSize;

                double veinVariation2 = Math.sin(progress2 * Math.PI * 8 + veinOffset) * veinSize;


                double x1 = x + direction.x * length * progress1;

                double y1 = y + direction.y * length * progress1;

                double z1 = z + direction.z * length * progress1;


                double x2 = x + direction.x * length * progress2;

                double y2 = y + direction.y * length * progress2;

                double z2 = z + direction.z * length * progress2;


                double veinX1 = x1 + perpendicular.x * Math.cos(veinAngle) * (radius1 + veinVariation1)

                        + perpendicular2.x * Math.sin(veinAngle) * (radius1 + veinVariation1);

                double veinY1 = y1 + perpendicular.y * Math.cos(veinAngle) * (radius1 + veinVariation1)

                        + perpendicular2.y * Math.sin(veinAngle) * (radius1 + veinVariation1);

                double veinZ1 = z1 + perpendicular.z * Math.cos(veinAngle) * (radius1 + veinVariation1)

                        + perpendicular2.z * Math.sin(veinAngle) * (radius1 + veinVariation1);


                double veinX2 = x2 + perpendicular.x * Math.cos(veinAngle) * (radius2 + veinVariation2)

                        + perpendicular2.x * Math.sin(veinAngle) * (radius2 + veinVariation2);

                double veinY2 = y2 + perpendicular.y * Math.cos(veinAngle) * (radius2 + veinVariation2)

                        + perpendicular2.y * Math.sin(veinAngle) * (radius2 + veinVariation2);

                double veinZ2 = z2 + perpendicular.z * Math.cos(veinAngle) * (radius2 + veinVariation2)

                        + perpendicular2.z * Math.sin(veinAngle) * (radius2 + veinVariation2);


                event.renderer.line(veinX1, veinY1, veinZ1, veinX2, veinY2, veinZ2, veinColor);

            }

        }

    }


    private void renderTexturedSphere(Render3DEvent event, double x, double y, double z,

                                      double radius, Color color, int segments,

                                      boolean enableTexture, double textureDetail) {

        for (int i = 0; i < segments / 2; i++) {

            double phi1 = Math.PI * i / ((double) segments / 2);

            double phi2 = Math.PI * (i + 1) / ((double) segments / 2);


            for (int j = 0; j < segments; j++) {

                double theta1 = 2 * Math.PI * j / segments;

                double theta2 = 2 * Math.PI * (j + 1) / segments;


                double x11 = x + radius * Math.sin(phi1) * Math.cos(theta1);

                double y11 = y + radius * Math.cos(phi1);

                double z11 = z + radius * Math.sin(phi1) * Math.sin(theta1);


                double x12 = x + radius * Math.sin(phi1) * Math.cos(theta2);

                double z12 = z + radius * Math.sin(phi1) * Math.sin(theta2);


                double x21 = x + radius * Math.sin(phi2) * Math.cos(theta1);

                double y21 = y + radius * Math.cos(phi2);

                double z21 = z + radius * Math.sin(phi2) * Math.sin(theta1);


                event.renderer.line(x11, y11, z11, x12, y11, z12, color);

                event.renderer.line(x11, y11, z11, x21, y21, z21, color);

            }

        }


        if (enableTexture) {

            renderSphereTexture(event, x, y, z, radius, color, segments, textureDetail);

        }

    }


    private void renderSphereTexture(Render3DEvent event, double x, double y, double z,

                                     double radius, Color color, int segments, double textureDetail) {

        int textureLines = (int) (segments * textureDetail);

        for (int i = 0; i < textureLines; i++) {

            double progress = (double) i / textureLines;

            double textureRadius = radius * (0.9 + 0.1 * Math.sin(progress * Math.PI * 8));


            for (int j = 0; j < 4; j++) {

                double textureAngle = Math.PI * j / 2;


                double currentX = x + textureRadius * Math.sin(progress * Math.PI) * Math.cos(textureAngle);

                double currentY = y + textureRadius * Math.cos(progress * Math.PI);

                double currentZ = z + textureRadius * Math.sin(progress * Math.PI) * Math.sin(textureAngle);


                double nextProgress = Math.min(1.0, progress + 0.05);

                double nextRadius = radius * (0.9 + 0.1 * Math.sin(nextProgress * Math.PI * 8));


                double nextX = x + nextRadius * Math.sin(nextProgress * Math.PI) * Math.cos(textureAngle);

                double nextY = y + nextRadius * Math.cos(nextProgress * Math.PI);

                double nextZ = z + nextRadius * Math.sin(nextProgress * Math.PI) * Math.sin(textureAngle);


                event.renderer.line(currentX, currentY, currentZ, nextX, nextY, nextZ, color);

            }

        }

    }


    private void renderScrotumSack(Render3DEvent event, double x, double y, double z,

                                   double separation, double sag, Color color, int segments) {

        for (int i = 0; i < segments; i++) {

            double angle1 = 2 * Math.PI * i / segments;

            double angle2 = 2 * Math.PI * (i + 1) / segments;


            double sagFactor1 = Math.sin(angle1) * sag;

            double sagFactor2 = Math.sin(angle2) * sag;


            double sepFactor1 = Math.cos(angle1) * separation;

            double sepFactor2 = Math.cos(angle2) * separation;


            double x1 = x + sepFactor1;

            double y1 = y + sagFactor1;


            double x2 = x + sepFactor2;

            double y2 = y + sagFactor2;


            event.renderer.line(x1, y1, z, x2, y2, z, color);

        }

    }


    private void renderRoundedGlans(Render3DEvent event, double x, double y, double z,

                                    double dirX, double dirY, double dirZ,

                                    double length, double baseRadius, double tipRadius,

                                    Color color, int segments) {

        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();

        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?

                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();

        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();


        int detailSegments = segments * 2;


        for (int i = 0; i < detailSegments; i++) {

            double progress1 = (double) i / detailSegments;

            double progress2 = (double) (i + 1) / detailSegments;


            double radius1 = calculateGlansRadius(progress1, baseRadius, tipRadius);

            double radius2 = calculateGlansRadius(progress2, baseRadius, tipRadius);


            double x1 = x + direction.x * length * progress1;

            double y1 = y + direction.y * length * progress1;

            double z1 = z + direction.z * length * progress1;


            double x2 = x + direction.x * length * progress2;

            double y2 = y + direction.y * length * progress2;

            double z2 = z + direction.z * length * progress2;


            for (int k = 0; k < segments; k++) {

                double ringAngle = 2 * Math.PI * k / segments;


                double px1 = x1 + perpendicular.x * Math.cos(ringAngle) * radius1

                        + perpendicular2.x * Math.sin(ringAngle) * radius1;

                double py1 = y1 + perpendicular.y * Math.cos(ringAngle) * radius1

                        + perpendicular2.y * Math.sin(ringAngle) * radius1;

                double pz1 = z1 + perpendicular.z * Math.cos(ringAngle) * radius1

                        + perpendicular2.z * Math.sin(ringAngle) * radius1;


                double px2 = x2 + perpendicular.x * Math.cos(ringAngle) * radius2

                        + perpendicular2.x * Math.sin(ringAngle) * radius2;

                double py2 = y2 + perpendicular.y * Math.cos(ringAngle) * radius2

                        + perpendicular2.y * Math.sin(ringAngle) * radius2;

                double pz2 = z2 + perpendicular.z * Math.cos(ringAngle) * radius2

                        + perpendicular2.z * Math.sin(ringAngle) * radius2;


                event.renderer.line(px1, py1, pz1, px2, py2, pz2, color);

            }


            if (i % 2 == 0) {

                for (int k = 0; k < segments; k++) {

                    double ringAngle1 = 2 * Math.PI * k / segments;

                    double ringAngle2 = 2 * Math.PI * (k + 1) / segments;


                    double px1 = x1 + perpendicular.x * Math.cos(ringAngle1) * radius1

                            + perpendicular2.x * Math.sin(ringAngle1) * radius1;

                    double py1 = y1 + perpendicular.y * Math.cos(ringAngle1) * radius1

                            + perpendicular2.y * Math.sin(ringAngle1) * radius1;

                    double pz1 = z1 + perpendicular.z * Math.cos(ringAngle1) * radius1

                            + perpendicular2.z * Math.sin(ringAngle1) * radius1;


                    double px2 = x1 + perpendicular.x * Math.cos(ringAngle2) * radius1

                            + perpendicular2.x * Math.sin(ringAngle2) * radius1;

                    double py2 = y1 + perpendicular.y * Math.cos(ringAngle2) * radius1

                            + perpendicular2.y * Math.sin(ringAngle2) * radius1;

                    double pz2 = z1 + perpendicular.z * Math.cos(ringAngle2) * radius1

                            + perpendicular2.z * Math.sin(ringAngle2) * radius1;


                    event.renderer.line(px1, py1, pz1, px2, py2, pz2, color);

                }

            }

        }


        double tipX = x + direction.x * length;

        double tipY = y + direction.y * length;

        double tipZ = z + direction.z * length;


        for (int k = 0; k < segments; k++) {

            double ringAngle1 = 2 * Math.PI * k / segments;

            double ringAngle2 = 2 * Math.PI * (k + 1) / segments;


            double px1 = tipX + perpendicular.x * Math.cos(ringAngle1) * tipRadius

                    + perpendicular2.x * Math.sin(ringAngle1) * tipRadius;

            double py1 = tipY + perpendicular.y * Math.cos(ringAngle1) * tipRadius

                    + perpendicular2.y * Math.sin(ringAngle1) * tipRadius;

            double pz1 = tipZ + perpendicular.z * Math.cos(ringAngle1) * tipRadius

                    + perpendicular2.z * Math.sin(ringAngle1) * tipRadius;


            double px2 = tipX + perpendicular.x * Math.cos(ringAngle2) * tipRadius

                    + perpendicular2.x * Math.sin(ringAngle2) * tipRadius;

            double py2 = tipY + perpendicular.y * Math.cos(ringAngle2) * tipRadius

                    + perpendicular2.y * Math.sin(ringAngle2) * tipRadius;

            double pz2 = tipZ + perpendicular.z * Math.cos(ringAngle2) * tipRadius

                    + perpendicular2.z * Math.sin(ringAngle2) * tipRadius;


            event.renderer.line(px1, py1, pz1, px2, py2, pz2, color);

        }

    }


    private void renderDetailedCorona(Render3DEvent event, double x, double y, double z,

                                      double dirX, double dirY, double dirZ,

                                      double radius, Color color, int segments) {

        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();

        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?

                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();

        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();


        for (int i = 0; i < segments; i++) {

            double angle1 = 2 * Math.PI * i / segments;

            double angle2 = 2 * Math.PI * (i + 1) / segments;


            double px1 = x + perpendicular.x * Math.cos(angle1) * radius

                    + perpendicular2.x * Math.sin(angle1) * radius;

            double py1 = y + perpendicular.y * Math.cos(angle1) * radius

                    + perpendicular2.y * Math.sin(angle1) * radius;

            double pz1 = z + perpendicular.z * Math.cos(angle1) * radius

                    + perpendicular2.z * Math.sin(angle1) * radius;


            double px2 = x + perpendicular.x * Math.cos(angle2) * radius

                    + perpendicular2.x * Math.sin(angle2) * radius;

            double py2 = y + perpendicular.y * Math.cos(angle2) * radius

                    + perpendicular2.y * Math.sin(angle2) * radius;

            double pz2 = z + perpendicular.z * Math.cos(angle2) * radius

                    + perpendicular2.z * Math.sin(angle2) * radius;


            event.renderer.line(px1, py1, pz1, px2, py2, pz2, color);

        }

    }

    private double calculateGlansRadius(double progress, double baseRadius, double tipRadius) {

        double easedProgress = 1 - Math.pow(1 - progress, 2);

        return baseRadius + (tipRadius - baseRadius) * easedProgress;

    }

    private void renderDetailedMeatus(Render3DEvent event, double x, double y, double z,

                                      double dirX, double dirY, double dirZ,

                                      double size, Color color, double depth) {

        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();

        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?

                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();

        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();


        int segments = 8;


        for (int i = 0; i < segments; i++) {

            double angle1 = 2 * Math.PI * i / segments;

            double angle2 = 2 * Math.PI * (i + 1) / segments;


            double px1 = x + perpendicular.x * Math.cos(angle1) * size

                    + perpendicular2.x * Math.sin(angle1) * size;

            double py1 = y + perpendicular.y * Math.cos(angle1) * size

                    + perpendicular2.y * Math.sin(angle1) * size;

            double pz1 = z + perpendicular.z * Math.cos(angle1) * size

                    + perpendicular2.z * Math.sin(angle1) * size;


            double px2 = x + perpendicular.x * Math.cos(angle2) * size

                    + perpendicular2.x * Math.sin(angle2) * size;

            double py2 = y + perpendicular.y * Math.cos(angle2) * size

                    + perpendicular2.y * Math.sin(angle2) * size;

            double pz2 = z + perpendicular.z * Math.cos(angle2) * size

                    + perpendicular2.z * Math.sin(angle2) * size;


            event.renderer.line(px1, py1, pz1, px2, py2, pz2, color);

        }


        double innerX = x + direction.x * depth;

        double innerY = y + direction.y * depth;

        double innerZ = z + direction.z * depth;


        for (int i = 0; i < 4; i++) {

            double angle = Math.PI * i / 2;

            double px = x + perpendicular.x * Math.cos(angle) * size * 0.5

                    + perpendicular2.x * Math.sin(angle) * size * 0.5;

            double py = y + perpendicular.y * Math.cos(angle) * size * 0.5

                    + perpendicular2.y * Math.sin(angle) * size * 0.5;

            double pz = z + perpendicular.z * Math.cos(angle) * size * 0.5

                    + perpendicular2.z * Math.sin(angle) * size * 0.5;


            event.renderer.line(px, py, pz, innerX, innerY, innerZ, color);

        }

    }


    private void renderUrethralOpening(Render3DEvent event, double x, double y, double z,

                                       double dirX, double dirY, double dirZ,

                                       double size, Color color) {

        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();

        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?

                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();

        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();


        int segments = 8;

        for (int i = 0; i < segments; i++) {

            double angle1 = 2 * Math.PI * i / segments;

            double angle2 = 2 * Math.PI * (i + 1) / segments;


            double px1 = x + perpendicular.x * Math.cos(angle1) * size

                    + perpendicular2.x * Math.sin(angle1) * size;

            double py1 = y + perpendicular.y * Math.cos(angle1) * size

                    + perpendicular2.y * Math.sin(angle1) * size;

            double pz1 = z + perpendicular.z * Math.cos(angle1) * size

                    + perpendicular2.z * Math.sin(angle1) * size;


            double px2 = x + perpendicular.x * Math.cos(angle2) * size

                    + perpendicular2.x * Math.sin(angle2) * size;

            double py2 = y + perpendicular.y * Math.cos(angle2) * size

                    + perpendicular2.y * Math.sin(angle2) * size;

            double pz2 = z + perpendicular.z * Math.cos(angle2) * size

                    + perpendicular2.z * Math.sin(angle2) * size;


            event.renderer.line(px1, py1, pz1, px2, py2, pz2, color);

        }

    }


    private void renderFrenulum(Render3DEvent event, double x, double y, double z,

                                double dirX, double dirY, double dirZ,

                                double length, Color color) {

        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();

        Vec3 undersideDir = direction.scale(-1);


        double startX = x + undersideDir.x * length * 0.2;

        double startY = y + undersideDir.y * length * 0.2;

        double startZ = z + undersideDir.z * length * 0.2;


        double endX = x + undersideDir.x * length * 0.8;

        double endY = y + undersideDir.y * length * 0.8;

        double endZ = z + undersideDir.z * length * 0.8;


        event.renderer.line(startX, startY, startZ, endX, endY, endZ, color);

    }


    private void renderGlansDetails(Render3DEvent event, double x, double y, double z,

                                    double dirX, double dirY, double dirZ,

                                    double length, double radius, Color color) {

        Vec3 direction = new Vec3(dirX, dirY, dirZ).normalize();

        Vec3 perpendicular = Math.abs(direction.y) > 0.8 ?

                new Vec3(1, 0, 0) : new Vec3(0, 1, 0).cross(direction).normalize();

        Vec3 perpendicular2 = direction.cross(perpendicular).normalize();


        for (int i = 0; i < 4; i++) {

            double detailAngle = Math.PI * i / 2;

            double detailRadius = radius * 0.8;


            for (int j = 0; j < 3; j++) {

                double progress1 = 0.2 + j * 0.3;

                double progress2 = 0.2 + (j + 1) * 0.3;


                double x1 = x + direction.x * length * progress1;

                double y1 = y + direction.y * length * progress1;

                double z1 = z + direction.z * length * progress1;


                double x2 = x + direction.x * length * progress2;

                double y2 = y + direction.y * length * progress2;

                double z2 = z + direction.z * length * progress2;


                double v = perpendicular.x * Math.cos(detailAngle) * detailRadius;

                double v1 = perpendicular2.x * Math.sin(detailAngle) * detailRadius;

                double px1 = x1 + v

                        + v1;

                double v2 = perpendicular2.y * Math.sin(detailAngle) * detailRadius;

                double v4 = perpendicular.y * Math.cos(detailAngle) * detailRadius;

                double py1 = y1 + v4

                        + v2;

                double v3 = perpendicular2.z * Math.sin(detailAngle) * detailRadius;

                double v5 = perpendicular.z * Math.cos(detailAngle) * detailRadius;

                double pz1 = z1 + v5

                        + v3;


                double px2 = x2 + v + v1;
                double py2 = y2 + v4 + v2;
                double pz2 = z2 + v5 + v3;


                event.renderer.line(px1, py1, pz1, px2, py2, pz2, color);

            }

        }

    }


    private static class PenisData {
        public final UUID playerId;
        public Vec3 position;
        public float yaw;
        public float bodyYaw;
        public float pitch;
        public boolean isMoving;
        public long lastUpdate;
        public Vec3 direction;
        public double sizeMultiplier;
        public double shaftMultiplier;
        public double glansMultiplier;
        public double ballsMultiplier;

        public PenisData(UUID playerId, Vec3 position, float yaw, float bodyYaw, float pitch, boolean isMoving) {
            this.playerId = playerId;
            this.position = position;
            this.yaw = yaw;
            this.bodyYaw = bodyYaw;
            this.pitch = pitch;
            this.isMoving = isMoving;
            this.lastUpdate = System.currentTimeMillis();

            Random random = new Random(playerId.hashCode());
            this.sizeMultiplier = 0.8 + random.nextDouble() * 0.4;
            this.shaftMultiplier = 0.9 + random.nextDouble() * 0.2;
            this.glansMultiplier = 0.85 + random.nextDouble() * 0.3;
            this.ballsMultiplier = 0.8 + random.nextDouble() * 0.4;

            updateDirection();
        }

        public void updatePosition(Vec3 position, float yaw, float bodyYaw, float pitch, boolean isMoving) {
            this.position = position;
            this.yaw = yaw;
            this.bodyYaw = bodyYaw;
            this.pitch = pitch;
            this.isMoving = isMoving;
            this.lastUpdate = System.currentTimeMillis();

            updateDirection();
        }

        private void updateDirection() {
            float yawRad = (float) Math.toRadians(yaw);
            float pitchRad = (float) Math.toRadians(pitch);

            double x = -Math.sin(yawRad) * Math.cos(pitchRad);
            double y = -Math.sin(pitchRad);
            double z = Math.cos(yawRad) * Math.cos(pitchRad);

            this.direction = new Vec3(x, y, z).normalize();
        }
    }
}