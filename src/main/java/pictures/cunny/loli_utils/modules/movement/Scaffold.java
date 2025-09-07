package pictures.cunny.loli_utils.modules.movement;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.modules.LoliModule;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.render.LoliRendering;
import pictures.cunny.loli_utils.utility.render.RenderWrap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Scaffold extends LoliModule {
    // ========== SETTINGS GROUPS ==========
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlacement = settings.createGroup("Placement");
    private final SettingGroup sgSafety = settings.createGroup("Safety");
    private final SettingGroup sgRotations = settings.createGroup("Rotations");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgAdvanced = settings.createGroup("Advanced");

    // ========== GENERAL SETTINGS ==========
    private final Setting<List<Block>> whitelist = sgGeneral.add(
            new BlockListSetting.Builder()
                    .name("whitelist")
                    .description("Only places blocks in this list.")
                    .build());

    private final Setting<Integer> dedicatedSlot = sgGeneral.add(new IntSetting.Builder()
            .name("dedicated-slot")
            .description("The hotbar slot to use for blocks.")
            .sliderRange(0, 8)
            .defaultValue(7)
            .build());

    private final Setting<Integer> blocksPerTick = sgGeneral.add(new IntSetting.Builder()
            .name("blocks-per-tick")
            .description("How many blocks to place per tick.")
            .sliderRange(1, 12)
            .defaultValue(2)
            .build());

    private final Setting<Integer> blocksPerSecond = sgGeneral.add(new IntSetting.Builder()
            .name("blocks-per-second")
            .description("Maximum blocks to place per second.")
            .sliderRange(10, 100)
            .defaultValue(50)
            .build());

    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
            .name("place-delay")
            .description("Delay between placements in ticks.")
            .sliderRange(0, 10)
            .defaultValue(0)
            .build());

    private final Setting<Double> placeRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("place-range")
            .description("Maximum range to place blocks.")
            .defaultValue(4.5)
            .sliderRange(3.0, 6.0)
            .build());

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
            .name("radius")
            .description("Horizontal radius to check for placeable blocks.")
            .sliderRange(1, 8)
            .defaultValue(4)
            .build());

    // ========== PLACEMENT SETTINGS ==========
    private final Setting<PlaceMode> placeMode = sgPlacement.add(new EnumSetting.Builder<PlaceMode>()
            .name("place-mode")
            .description("How to prioritize block placement.")
            .defaultValue(PlaceMode.CLOSEST)
            .build());

    private final Setting<Integer> depth = sgPlacement.add(new IntSetting.Builder()
            .name("depth")
            .description("How many layers down to place blocks.")
            .sliderRange(1, 8)
            .defaultValue(3)
            .build());

    private final Setting<Boolean> tower = sgPlacement.add(new BoolSetting.Builder()
            .name("tower")
            .description("Enable tower mode when looking up.")
            .defaultValue(false)
            .build());

    private final Setting<Double> towerAngle = sgPlacement.add(new DoubleSetting.Builder()
            .name("tower-angle")
            .description("Minimum pitch angle to activate tower mode.")
            .defaultValue(70.0)
            .sliderRange(45.0, 90.0)
            .visible(tower::get)
            .build());

    private final Setting<Boolean> diagonals = sgPlacement.add(new BoolSetting.Builder()
            .name("diagonals")
            .description("Also place blocks diagonally.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> extend = sgPlacement.add(new BoolSetting.Builder()
            .name("extend")
            .description("Extend scaffold in movement direction.")
            .defaultValue(true)
            .build());

    private final Setting<Integer> extendDistance = sgPlacement.add(new IntSetting.Builder()
            .name("extend-distance")
            .description("How far to extend scaffold.")
            .sliderRange(1, 5)
            .defaultValue(2)
            .visible(extend::get)
            .build());

    private final Setting<Boolean> airPlace = sgPlacement.add(new BoolSetting.Builder()
            .name("air-place")
            .description("Allow placing blocks in air when needed.")
            .defaultValue(false)
            .build());

    // ========== SAFETY SETTINGS ==========
    private final Setting<Boolean> lockToY = sgSafety.add(new BoolSetting.Builder()
            .name("lock-to-y")
            .description("Prevents placing blocks unless at a specific Y level.")
            .defaultValue(false)
            .build());

    private final Setting<Integer> yLevel = sgSafety.add(new IntSetting.Builder()
            .name("y-level")
            .description("Y Level to lock to.")
            .sliderRange(-64, 320)
            .defaultValue(319)
            .visible(lockToY::get)
            .build());

    private final Setting<Boolean> orAbove = sgSafety.add(new BoolSetting.Builder()
            .name("or-above")
            .description("Makes it so you can place over the locked Y.")
            .defaultValue(true)
            .visible(lockToY::get)
            .build());

    private final Setting<Boolean> safeWalk = sgSafety.add(new BoolSetting.Builder()
            .name("safe-walk")
            .description("Prevents walking off edges.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> pauseOnEat = sgSafety.add(new BoolSetting.Builder()
            .name("pause-on-eat")
            .description("Pause when eating or using items.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> pauseOnCombat = sgSafety.add(new BoolSetting.Builder()
            .name("pause-on-combat")
            .description("Pause when in combat.")
            .defaultValue(true)
            .build());

    private final Setting<Double> minHealth = sgSafety.add(new DoubleSetting.Builder()
            .name("min-health")
            .description("Minimum health to continue scaffolding.")
            .defaultValue(5.0)
            .sliderRange(0.0, 20.0)
            .build());

    // ========== ROTATION SETTINGS ==========
    private final Setting<Boolean> rotate = sgRotations.add(new BoolSetting.Builder()
            .name("rotate")
            .description("Rotate when placing blocks.")
            .defaultValue(true)
            .build());

    private final Setting<RotationMode> rotationMode = sgRotations.add(new EnumSetting.Builder<RotationMode>()
            .name("rotation-mode")
            .description("How to handle rotations.")
            .defaultValue(RotationMode.PACKET)
            .visible(rotate::get)
            .build());

    private final Setting<Integer> rotationSpeed = sgRotations.add(new IntSetting.Builder()
            .name("rotation-speed")
            .description("Speed of rotation interpolation.")
            .defaultValue(8)
            .sliderRange(1, 20)
            .visible(() -> rotate.get() && rotationMode.get() == RotationMode.SMOOTH)
            .build());

    private final Setting<Boolean> yawStep = sgRotations.add(new BoolSetting.Builder()
            .name("yaw-step")
            .description("Limit yaw rotation speed.")
            .defaultValue(false)
            .visible(rotate::get)
            .build());

    private final Setting<Double> maxYawStep = sgRotations.add(new DoubleSetting.Builder()
            .name("max-yaw-step")
            .description("Maximum yaw change per tick.")
            .defaultValue(45.0)
            .sliderRange(10.0, 180.0)
            .visible(() -> rotate.get() && yawStep.get())
            .build());

    // ========== RENDER SETTINGS ==========
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
            .name("render")
            .description("Render placed blocks.")
            .defaultValue(true)
            .build());

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .visible(render::get)
            .build());

    private final Setting<RenderStyle> renderStyle = sgRender.add(new EnumSetting.Builder<RenderStyle>()
            .name("render-style")
            .description("Style of block rendering.")
            .defaultValue(RenderStyle.FADE)
            .visible(render::get)
            .build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("Color of the sides.")
            .defaultValue(new SettingColor(195, 126, 234, 60))
            .visible(render::get)
            .build());

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("Color of the lines.")
            .defaultValue(new SettingColor(195, 126, 234, 180))
            .visible(render::get)
            .build());

    private final Setting<Integer> fadeTime = sgRender.add(new IntSetting.Builder()
            .name("fade-time")
            .description("How long blocks stay rendered (ticks).")
            .defaultValue(120)
            .sliderRange(20, 300)
            .visible(() -> render.get() && renderStyle.get() == RenderStyle.FADE)
            .build());

    private final Setting<Boolean> renderQueue = sgRender.add(new BoolSetting.Builder()
            .name("render-queue")
            .description("Show upcoming placement positions.")
            .defaultValue(true)
            .visible(render::get)
            .build());

    private final Setting<SettingColor> queueColor = sgRender.add(new ColorSetting.Builder()
            .name("queue-color")
            .description("Color for queued positions.")
            .defaultValue(new SettingColor(255, 255, 0, 80))
            .visible(() -> render.get() && renderQueue.get())
            .build());

    // ========== ADVANCED SETTINGS ==========
    private final Setting<Boolean> smartDelay = sgAdvanced.add(new BoolSetting.Builder()
            .name("smart-delay")
            .description("Dynamically adjust delays based on server performance.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> predictive = sgAdvanced.add(new BoolSetting.Builder()
            .name("predictive")
            .description("Predict movement and place blocks ahead.")
            .defaultValue(true)
            .build());

    private final Setting<Integer> predictionTicks = sgAdvanced.add(new IntSetting.Builder()
            .name("prediction-ticks")
            .description("How many ticks ahead to predict.")
            .defaultValue(3)
            .sliderRange(1, 10)
            .visible(predictive::get)
            .build());

    private final Setting<Boolean> adaptiveRadius = sgAdvanced.add(new BoolSetting.Builder()
            .name("adaptive-radius")
            .description("Adjust search radius based on movement speed.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> multiThreaded = sgAdvanced.add(new BoolSetting.Builder()
            .name("multi-threaded")
            .description("Use multiple threads for position calculations.")
            .defaultValue(false)
            .build());

    // ========== INTERNAL VARIABLES ==========
    private final Map<BlockPos, Long> placedBlocks = new ConcurrentHashMap<>();
    private final List<RenderWrap> renderWraps = Collections.synchronizedList(new ArrayList<>());
    private final List<BlockPos> placementQueue = Collections.synchronizedList(new ArrayList<>());
    private final Deque<Vec3> movementHistory = new ArrayDeque<>(10);

    private int delayTimer = 0;
    private int blocksThisSecond = 0;
    private long lastSecond = System.currentTimeMillis() / 1000;
    private float lastYaw = 0;
    private float lastPitch = 0;
    private Vec3 lastPosition = Vec3.ZERO;
    private double averageMovementSpeed = 0.0;

    // Performance tracking
    private long avgCalculationTime = 0;
    private int calculationSamples = 0;

    public enum PlaceMode {
        CLOSEST("Closest"),
        FURTHEST("Furthest"),
        SMART("Smart"),
        SAFEST("Safest");

        private final String title;

        PlaceMode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public enum RotationMode {
        PACKET("Packet"),
        CLIENT("Client"),
        SMOOTH("Smooth");

        private final String title;

        RotationMode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public enum RenderStyle {
        STATIC("Static"),
        FADE("Fade"),
        SHRINK("Shrink"),
        PULSE("Pulse");

        private final String title;

        RenderStyle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public Scaffold() {
        super(LoliUtilsMeteor.CATEGORY, "loli-scaffold", "Automatically places blocks under/around your feet with advanced features.");
        this.timedTicks = 1; // More frequent updates for better responsiveness
    }

    @Override
    public void safeOnActivate() {
        placedBlocks.clear();
        renderWraps.clear();
        placementQueue.clear();
        movementHistory.clear();
        delayTimer = 0;
        blocksThisSecond = 0;
        lastSecond = System.currentTimeMillis() / 1000;

        if (mc.player != null) {
            lastPosition = mc.player.position();
            lastYaw = mc.player.getYRot();
            lastPitch = mc.player.getXRot();
        }
    }

    @Override
    public void onDeactivate() {
        placedBlocks.clear();
        renderWraps.clear();
        placementQueue.clear();
    }

    @Override
    public void update() {
        if (mc.gameMode == null || mc.player == null) return;

        long startTime = System.nanoTime();

        // Update performance tracking
        updatePerformanceMetrics();

        // Tick render wrappers
        LoliRendering.tickFadeTime(renderWraps);

        // Clean old placed blocks
        cleanupOldBlocks();

        // Handle delays
        if (delayTimer > 0) {
            delayTimer--;
            return;
        }

        // Safety checks
        if (!passesSafetyChecks()) {
            return;
        }

        // Rate limiting
        if (!checkRateLimits()) {
            return;
        }

        // Update movement tracking
        updateMovementTracking();

        // Find and sort placement positions
        List<BlockPos> positions = findPlacementPositions();
        if (positions.isEmpty()) return;

        // Sort positions based on mode
        sortPositions(positions);

        // Place blocks
        int placed = 0;
        for (BlockPos pos : positions) {
            if (placed >= blocksPerTick.get()) break;

            if (placeBlock(pos)) {
                placed++;
                blocksThisSecond++;

                if (smartDelay.get()) {
                    delayTimer = calculateSmartDelay();
                } else if (placeDelay.get() > 0) {
                    delayTimer = placeDelay.get();
                }
            }
        }

        // Update calculation time tracking
        long endTime = System.nanoTime();
        updateCalculationTime(endTime - startTime);
    }

    private void updatePerformanceMetrics() {
        long currentSecond = System.currentTimeMillis() / 1000;
        if (currentSecond != lastSecond) {
            lastSecond = currentSecond;
            blocksThisSecond = 0;
        }
    }

    private void cleanupOldBlocks() {
        long currentTime = System.currentTimeMillis();
        placedBlocks.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > (fadeTime.get() * 50L));
    }

    private boolean passesSafetyChecks() {
        if (pauseOnEat.get() && (Modules.get().get(AutoEat.class).eating
                || Modules.get().get(AutoGap.class).isEating())) {
            return false;
        }

        if (pauseOnCombat.get() && Modules.get().get(KillAura.class).getTarget() != null) {
            return false;
        }

        return !(mc.player.getHealth() + mc.player.getAbsorptionAmount() < minHealth.get());
    }

    private boolean checkRateLimits() {
        return blocksThisSecond < blocksPerSecond.get();
    }

    private void updateMovementTracking() {
        if (mc.player == null) return;

        Vec3 currentPos = mc.player.position();
        movementHistory.offerFirst(currentPos);
        if (movementHistory.size() > 10) {
            movementHistory.removeLast();
        }

        // Calculate average movement speed
        if (movementHistory.size() >= 2) {
            double totalDistance = 0;
            Vec3 prev = null;
            for (Vec3 pos : movementHistory) {
                if (prev != null) {
                    totalDistance += pos.distanceTo(prev);
                }
                prev = pos;
            }
            averageMovementSpeed = totalDistance / Math.max(1, movementHistory.size() - 1);
        }

        lastPosition = currentPos;
    }

    private List<BlockPos> findPlacementPositions() {
        if (mc.player == null) return new ArrayList<>();

        List<BlockPos> positions = new ArrayList<>();
        BlockPos playerPos = mc.player.blockPosition();

        // Calculate effective radius
        int effectiveRadius = radius.get();
        if (adaptiveRadius.get()) {
            effectiveRadius = Math.max(2, (int) (radius.get() + averageMovementSpeed * 2));
        }

        // Tower mode
        if (tower.get() && mc.player.getXRot() < -towerAngle.get()) {
            BlockPos towerPos = playerPos.below();
            if (canPlaceAt(towerPos)) {
                positions.add(towerPos);
                return positions;
            }
        }

        // Main scaffolding logic
        for (int d = 0; d <= depth.get(); d++) {
            for (int x = -effectiveRadius; x <= effectiveRadius; x++) {
                for (int z = -effectiveRadius; z <= effectiveRadius; z++) {
                    // Skip center unless it's depth 0
                    if (x == 0 && z == 0 && d == 0) continue;

                    // Skip non-diagonal positions if diagonals is off
                    if (!diagonals.get() && Math.abs(x) > 0 && Math.abs(z) > 0) continue;

                    BlockPos pos = playerPos.offset(x, -d, z);

                    // Distance check
                    if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > placeRange.get() * placeRange.get()) {
                        continue;
                    }

                    if (canPlaceAt(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }

        // Add predictive positions
        if (predictive.get()) {
            addPredictivePositions(positions);
        }

        // Add extension positions
        if (extend.get()) {
            addExtensionPositions(positions);
        }

        return positions;
    }

    private void addPredictivePositions(List<BlockPos> positions) {
        if (mc.player == null || movementHistory.size() < 2) return;

        Vec3 velocity = calculateVelocity();
        if (velocity.length() < 0.1) return; // Player not moving much

        Vec3 currentPos = mc.player.position();
        for (int i = 1; i <= predictionTicks.get(); i++) {
            Vec3 predictedPos = currentPos.add(velocity.scale(i));
            BlockPos blockPos = BlockPos.containing(predictedPos).below();

            if (mc.player.distanceToSqr(Vec3.atCenterOf(blockPos)) <= placeRange.get() * placeRange.get()) {
                if (canPlaceAt(blockPos) && !positions.contains(blockPos)) {
                    positions.add(blockPos);
                }
            }
        }
    }

    private void addExtensionPositions(List<BlockPos> positions) {
        if (mc.player == null) return;

        Vec3 lookVec = mc.player.getLookAngle();
        Vec3 playerPos = mc.player.position();

        for (int i = 1; i <= extendDistance.get(); i++) {
            Vec3 extendedPos = playerPos.add(lookVec.scale(i));
            BlockPos blockPos = BlockPos.containing(extendedPos).below();

            if (mc.player.distanceToSqr(Vec3.atCenterOf(blockPos)) <= placeRange.get() * placeRange.get()) {
                if (canPlaceAt(blockPos) && !positions.contains(blockPos)) {
                    positions.add(blockPos);
                }
            }
        }
    }

    private Vec3 calculateVelocity() {
        if (movementHistory.size() < 2) return Vec3.ZERO;

        List<Vec3> history = new ArrayList<>(movementHistory);
        Vec3 current = history.get(0);
        Vec3 previous = history.get(1);

        return current.subtract(previous);
    }

    private void sortPositions(List<BlockPos> positions) {
        if (mc.player == null) return;

        Vec3 playerPos = mc.player.position();

        switch (placeMode.get()) {
            case CLOSEST -> positions.sort(Comparator.comparingDouble(pos ->
                    playerPos.distanceToSqr(Vec3.atCenterOf(pos))));

            case FURTHEST -> positions.sort(Comparator.comparingDouble((BlockPos pos) ->
                    playerPos.distanceToSqr(Vec3.atCenterOf(pos))).reversed());

            case SMART -> positions.sort(this::smartComparator);

            case SAFEST -> positions.sort(this::safestComparator);
        }
    }

    private int smartComparator(BlockPos a, BlockPos b) {
        if (mc.player == null) return 0;

        Vec3 playerPos = mc.player.position();
        double distA = playerPos.distanceToSqr(Vec3.atCenterOf(a));
        double distB = playerPos.distanceToSqr(Vec3.atCenterOf(b));

        // Prioritize positions directly below player
        boolean aIsBelow = a.getX() == mc.player.getBlockX() && a.getZ() == mc.player.getBlockZ();
        boolean bIsBelow = b.getX() == mc.player.getBlockX() && b.getZ() == mc.player.getBlockZ();

        if (aIsBelow && !bIsBelow) return -1;
        if (!aIsBelow && bIsBelow) return 1;

        // Then by distance
        return Double.compare(distA, distB);
    }

    private int safestComparator(BlockPos a, BlockPos b) {
        if (mc.player == null) return 0;

        // Calculate safety score (higher is safer)
        double safetyA = calculateSafetyScore(a);
        double safetyB = calculateSafetyScore(b);

        return Double.compare(safetyB, safetyA); // Higher safety first
    }

    private double calculateSafetyScore(BlockPos pos) {
        if (mc.player == null) return 0;

        double score = 0;

        // Prefer positions with solid blocks nearby
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (BlockUtils.isNotAir(pos.relative(dir))) {
                score += 1.0;
            }
        }

        // Prefer positions closer to player
        double distance = mc.player.position().distanceToSqr(Vec3.atCenterOf(pos));
        score += Math.max(0, 10 - distance);

        return score;
    }

    private boolean placeBlock(BlockPos pos) {
        if (mc.gameMode == null || mc.player == null) return false;

        // Find suitable block
        FindItemResult item = InvUtils.find(stack ->
                whitelist.get().stream().anyMatch(block -> block.asItem() == stack.getItem()));

        if (!item.found()) return false;

        // Handle inventory switching
        if (!handleInventory(item)) return false;

        // Handle rotations
        if (rotate.get() && !handleRotations(pos)) return false;

        // Place the block
        boolean placed = mc.gameMode.useItemOn(mc.player, item.getHand(),
                BlockUtils.getSafeHitResult(pos)).consumesAction();

        if (placed) {
            // Track placed block
            placedBlocks.put(pos, System.currentTimeMillis());

            // Add render
            if (render.get()) {
                Color renderColor = sideColor.get();
                renderWraps.add(new RenderWrap(pos, fadeTime.get(), 0, 0, renderColor));
            }
        }

        return placed;
    }

    private boolean handleInventory(FindItemResult item) {
        if (item.isOffhand()) return true;

        if (!item.isHotbar()) {
            InventoryUtils.swapToHotbar(item.slot(), dedicatedSlot.get());
            delayTimer = 2; // Small delay after inventory operation
            return false;
        }

        if (mc.player.getInventory().getSelectedSlot() != item.slot()) {
            InventoryUtils.swapSlot(item.slot());
            delayTimer = 1;
            return false;
        }

        return true;
    }

    private boolean handleRotations(BlockPos pos) {
        if (mc.player == null) return true;

        float[] rotations = calculateRotations(pos);
        float targetYaw = rotations[0];
        float targetPitch = rotations[1];

        switch (rotationMode.get()) {
            case PACKET -> {
                mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(
                        targetYaw, targetPitch, mc.player.onGround(), mc.player.horizontalCollision));
                return true;
            }

            case CLIENT -> {
                mc.player.setYRot(targetYaw);
                mc.player.setXRot(targetPitch);
                return true;
            }

            case SMOOTH -> {
                float yawDiff = Mth.wrapDegrees(targetYaw - lastYaw);
                float pitchDiff = targetPitch - lastPitch;

                if (yawStep.get()) {
                    yawDiff = Mth.clamp(yawDiff, -maxYawStep.get().floatValue(),
                            maxYawStep.get().floatValue());
                }

                float newYaw = lastYaw + yawDiff / rotationSpeed.get();
                float newPitch = lastPitch + pitchDiff / rotationSpeed.get();

                mc.player.setYRot(newYaw);
                mc.player.setXRot(newPitch);

                lastYaw = newYaw;
                lastPitch = newPitch;

                // Check if we're close enough to target rotation
                return Math.abs(Mth.wrapDegrees(targetYaw - newYaw)) < 5.0 &&
                        Math.abs(targetPitch - newPitch) < 5.0;
            }
        }

        return true;
    }

    private float[] calculateRotations(BlockPos pos) {
        if (mc.player == null) return new float[]{0, 0};

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 targetPos = Vec3.atCenterOf(pos);

        Vec3 diff = targetPos.subtract(eyePos);
        double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distance));

        return new float[]{yaw, pitch};
    }

    private boolean canPlaceAt(BlockPos pos) {
        if (mc.player == null) return false;

        // Y-level lock check
        if (lockToY.get()) {
            if (orAbove.get()) {
                if (pos.getY() < yLevel.get()) return false;
            } else if (pos.getY() != yLevel.get()) return false;
        }

        // Basic placement check
        if (!BlockUtils.canPlace(pos, placeRange.get())) return false;

        // Air placement check
        if (!airPlace.get() && BlockUtils.shouldAirPlace(pos)) return false;

        // Safe walk check
        return !safeWalk.get() || !isUnsafePosition(pos);
    }

    private boolean isUnsafePosition(BlockPos pos) {
        if (mc.player == null) return false;

        // Check if placing this block would create an unsafe walking surface
        BlockPos playerPos = mc.player.blockPosition();

        // If block is directly under player and would create a gap, it's unsafe
        if (pos.equals(playerPos.below())) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adjacent = pos.relative(dir);
                if (BlockUtils.isReplaceable(adjacent) && BlockUtils.isReplaceable(adjacent.below())) {
                    return true;
                }
            }
        }

        return false;
    }

    private int calculateSmartDelay() {
        // Adjust delay based on server performance and placement success
        double avgTime = avgCalculationTime / 1_000_000.0; // Convert to milliseconds

        if (avgTime > 10) return 2; // High calculation time
        if (avgTime > 5) return 1;  // Medium calculation time

        return 0; // Low calculation time
    }

    private void updateCalculationTime(long nanoTime) {
        avgCalculationTime = (avgCalculationTime * calculationSamples + nanoTime) / (calculationSamples + 1);
        calculationSamples = Math.min(calculationSamples + 1, 100); // Keep last 100 samples
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Additional per-tick updates if needed
        placementQueue.clear(); // Clear queue each tick for fresh calculations
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!render.get() || mc.player == null) return;

        // Render placed blocks
        synchronized (renderWraps) {
            for (RenderWrap wrap : renderWraps) {
                renderBlock(event, wrap);
            }
        }

        // Render queued positions
        if (renderQueue.get()) {
            renderPlacementQueue(event);
        }
    }

    private void renderBlock(Render3DEvent event, RenderWrap wrap) {
        Color color = sideColor.get();
        Color lineColor = this.lineColor.get();

        switch (renderStyle.get()) {
            case STATIC -> {
                event.renderer.box(wrap.blockPos(), color, lineColor, shapeMode.get(), 0);
            }

            case FADE -> {
                double progress = 1.0 - ((double) wrap.fadeTime() / wrap.maxFadeTime());
                int alpha = (int) (color.a * progress);
                int lineAlpha = (int) (lineColor.a * progress);

                Color fadeColor = new Color(color.r, color.g, color.b, alpha);
                Color fadeLineColor = new Color(lineColor.r, lineColor.g, lineColor.b, lineAlpha);

                event.renderer.box(wrap.blockPos(), fadeColor, fadeLineColor, shapeMode.get(), 0);
            }

            case SHRINK -> {
                double progress = 1.0 - ((double) wrap.fadeTime() / wrap.maxFadeTime());
                double shrink = 0.1 * (1.0 - progress);

                event.renderer.box(
                        wrap.blockPos().getX() + shrink, wrap.blockPos().getY() + shrink, wrap.blockPos().getZ() + shrink,
                        wrap.blockPos().getX() + 1 - shrink, wrap.blockPos().getY() + 1 - shrink, wrap.blockPos().getZ() + 1 - shrink,
                        color, lineColor, shapeMode.get(), 0
                );
            }

            case PULSE -> {
                double progress = ((double) wrap.fadeTime() / wrap.maxFadeTime());
                double pulse = 0.05 * Math.sin(progress * Math.PI * 4);

                event.renderer.box(
                        wrap.blockPos().getX() - pulse, wrap.blockPos().getY() - pulse, wrap.blockPos().getZ() - pulse,
                        wrap.blockPos().getX() + 1 + pulse, wrap.blockPos().getY() + 1 + pulse, wrap.blockPos().getZ() + 1 + pulse,
                        color, lineColor, shapeMode.get(), 0
                );
            }
        }
    }

    private void renderPlacementQueue(Render3DEvent event) {
        List<BlockPos> positions = findPlacementPositions();
        positions = positions.subList(0, Math.min(positions.size(), 5)); // Show only next 5

        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            Color color = queueColor.get();

            // Make further positions more transparent
            int alpha = Math.max(20, color.a - (i * 30));
            Color renderColor = new Color(color.r, color.g, color.b, alpha);

            event.renderer.box(pos, renderColor, renderColor, ShapeMode.Lines, 0);
        }
    }

    @Override
    public String getInfoString() {
        return String.format("%d B/s", blocksThisSecond);
    }
}