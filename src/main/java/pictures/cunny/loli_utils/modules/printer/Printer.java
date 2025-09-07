package pictures.cunny.loli_utils.modules.printer;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalNear;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.config.settings.ButtonHCKSetting;
import pictures.cunny.loli_utils.modules.LoliModule;
import pictures.cunny.loli_utils.modules.printer.movesets.AdvancedMove;
import pictures.cunny.loli_utils.modules.printer.movesets.MoveSets;
import pictures.cunny.loli_utils.modules.printer.themes.*;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.MathUtils;
import pictures.cunny.loli_utils.utility.modules.McDataCache;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;
import pictures.cunny.loli_utils.utility.render.RenderWrap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

import static pictures.cunny.loli_utils.modules.printer.PrinterUtils.PRINTER;

public class Printer extends LoliModule {
    // anchoringTo must be here.
    public static BlockPos.MutableBlockPos anchoringTo = new BlockPos.MutableBlockPos();
    public static BlockPos.MutableBlockPos lastPlacedBlock = new BlockPos.MutableBlockPos();
    public final List<int[]> toSort = new ArrayList<>();
    public final List<MapColor> containedColors = new ArrayList<>();
    public final List<Item> containedBlocks = new ArrayList<>();
    // Render
    public final List<RenderWrap> placeFading = new ArrayList<>();
    final List<int[]> anchorToSort = new ArrayList<>();
    // Settings
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> swapDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("switch-delay")
                            .description("How long to wait before placing after switching.")
                            .defaultValue(4)
                            .sliderRange(0, 10)
                            .build());
    public final Setting<Integer> dedicatedSlot =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("dedicated-slot")
                            .description("The hotbar slot to use for blocks.")
                            .defaultValue(7)
                            .sliderRange(0, 8)
                            .build());
    public final Setting<Integer> placeRadius =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("place-radius")
                            .description("The range to check for placeable blocks.")
                            .defaultValue(5)
                            .sliderRange(1, 5)
                            .build());
    public final Setting<Double> placeDistance =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("place-distance")
                            .description("The max distance to place blocks.")
                            .defaultValue(3.75)
                            .sliderRange(3.2, 5.0)
                            .build());
    public final Setting<Integer> delay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("printing-delay")
                            .description("Delay between printing blocks in ticks.")
                            .defaultValue(0)
                            .sliderRange(0, 20)
                            .build());
    public final Setting<Integer> blocksPerTick =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("blocks/tick")
                            .description("How many blocks place per tick.")
                            .defaultValue(3)
                            .sliderRange(1, 4)
                            .build());
    public final Setting<Integer> blocksPerSec =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("blocks/sec")
                            .description("The maximum blocks per second.")
                            .defaultValue(60)
                            .sliderRange(10, 80)
                            .build());
    public final Setting<Boolean> packetPlace =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("packet-place")
                            .description("Places with packets to limit ghost blocks.")
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> noSwing =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("no-swing")
                            .description("Disables swinging.")
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> noRotations =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("no-rotations")
                            .description("Completely disables rotations, for lenient Grim.")
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> skipUnneededRotations =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("skip-rotations")
                            .description("Skips rotations in a certain Yaw/Pitch distance to place more, causes camera to snap around.")
                            .defaultValue(true)
                            .build());
    public final Setting<Double> rotationTolerance =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("rotation-tolerance")
                            .description("How much space to tolerate before rotating.")
                            .defaultValue(15)
                            .sliderRange(0.1, 30)
                            .visible(skipUnneededRotations::get)
                            .build());
    public final Setting<Boolean> experimentalRotations =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("experimental-rotations")
                            .description("An experimental rotation change, makes it much more aggressive.")
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> raytraceCarpet =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("raytrace-carpet")
                            .description("Raytracing for carpet, is likely not needed and will decrease speed.")
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> raytraceFull =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("raytrace-full")
                            .description("Raytracing for full-blocks, not required on grim.")
                            .defaultValue(true)
                            .build());
    public final Setting<SortAlgorithm> firstAlgorithm =
            sgDefault.add(
                    new EnumSetting.Builder<SortAlgorithm>()
                            .name("first-sorting-mode")
                            .description("The blocks you want to place first.")
                            .defaultValue(SortAlgorithm.Closest)
                            .build());
    public final Setting<SortingSecond> secondAlgorithm =
            sgDefault.add(
                    new EnumSetting.Builder<SortingSecond>()
                            .name("second-sorting-mode")
                            .description(
                                    "Second pass of sorting eg. place first blocks higher and closest to you.")
                            .defaultValue(SortingSecond.None)
                            .visible(() -> firstAlgorithm.get().applySecondSorting)
                            .build());
    public final Setting<Boolean> liquidPlace =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("liquid-place")
                            .description("Places inside of liquids if it would let you place a target block.")
                            .defaultValue(true)
                            .build());
    public final Setting<Integer> liquidPlaceTimeout =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("liquid-place-timeout")
                            .description("Timeout between liquid placing, 0 to effectively disable.")
                            .defaultValue(10)
                            .sliderRange(0, 40)
                            .build());
    public final Setting<Boolean> onlyOnGround =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("on-ground")
                            .description("Only places if the player is on-ground.")
                            .defaultValue(true)
                            .build());
    public final Setting<Boolean> notInLiquid =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("out-of-liquid")
                            .description("Only places if the player is not in a liquid.")
                            .defaultValue(true)
                            .build());
    // Color Swapping
    private final SettingGroup sgColorSwapping = settings.createGroup("Color Swapping");
    public final Setting<Boolean> strictNoColor =
            sgColorSwapping.add(
                    new BoolSetting.Builder()
                            .name("strict-no-color")
                            .description("Prevents using color swapping at all.")
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> refreshFaked = sgColorSwapping.add(new ButtonHCKSetting("Refresh", McDataCache::refresh));
    public final Setting<List<Block>> blockExclusion =
            sgColorSwapping.add(
                    new BlockListSetting.Builder()
                            .name("block-exclusion")
                            .description("Excludes blocks.")
                            .build());
    // Anchoring
    private final SettingGroup sgAnchor = settings.createGroup("Anchoring");
    public final Setting<Boolean> anchor =
            sgAnchor.add(
                    new BoolSetting.Builder()
                            .name("anchor")
                            .description("Anchors player to placeable blocks.")
                            .defaultValue(false)
                            .onChanged((b) -> anchoringTo.set(0, -999, 0))
                            .build());
    public final Setting<Integer> yLevel =
            sgAnchor.add(
                    new IntSetting.Builder()
                            .name("y-level")
                            .description("The Y level to scan")
                            .defaultValue(64)
                            .sliderRange(-64, 320)
                            .build());
    public final Setting<AnchorMovement> anchorMove =
            sgAnchor.add(
                    new EnumSetting.Builder<AnchorMovement>()
                            .name("anchor-movement")
                            .description("How you move to destinations")
                            .defaultValue(AnchorMovement.Vanilla)
                            .build());
    public final Setting<Boolean> alwaysSprint =
            sgAnchor.add(
                    new BoolSetting.Builder()
                            .name("always-sprint")
                            .description("Advanced move will always sprint.")
                            .visible(() -> anchorMove.get() == AnchorMovement.Vanilla)
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> safeWalk =
            sgAnchor.add(
                    new BoolSetting.Builder()
                            .name("safe-walk")
                            .description("Attempts to walk safely.")
                            .visible(() -> anchorMove.get() == AnchorMovement.Vanilla)
                            .defaultValue(true)
                            .build());
    public final Setting<Integer> backHoldTime =
            sgAnchor.add(
                    new IntSetting.Builder()
                            .name("back-hold")
                            .description("How many ticks to hold back for safe walk.")
                            .visible(() -> anchorMove.get() == AnchorMovement.Vanilla && safeWalk.get())
                            .defaultValue(3)
                            .sliderRange(0, 5)
                            .build());
    public final Setting<Boolean> useElytra =
            sgAnchor.add(
                    new BoolSetting.Builder()
                            .name("use-elytra")
                            .description("Will fly to land near blocks if it is available.")
                            .visible(() -> anchorMove.get() == AnchorMovement.Vanilla)
                            .defaultValue(false)
                            .build());
    public final Setting<Integer> distanceBeforeFlight =
            sgAnchor.add(
                    new IntSetting.Builder()
                            .name("distance-before-flight")
                            .description("How far from a anchor pos before resorting to using efly.")
                            .visible(() -> anchorMove.get() == AnchorMovement.Vanilla && useElytra.get())
                            .defaultValue(12)
                            .sliderRange(12, 64)
                            .build());
    public final Setting<Integer> landScanRadius =
            sgAnchor.add(
                    new IntSetting.Builder()
                            .name("elytra-scan-radius")
                            .description("How far to scan for land to use for elytra landing.")
                            .visible(() -> anchorMove.get() == AnchorMovement.Vanilla && useElytra.get())
                            .defaultValue(16)
                            .sliderRange(16, 64)
                            .build());
    public final Setting<AdvancedMove.PitchMode> pitchMode =
            sgAnchor.add(
                    new EnumSetting.Builder<AdvancedMove.PitchMode>()
                            .name("pitch-mode")
                            .description("How anchoring should handle pitches.")
                            .visible(() -> anchorMove.get() == AnchorMovement.Vanilla)
                            .defaultValue(AdvancedMove.PitchMode.NONE)
                            .build());
    public final Setting<Boolean> differing =
            sgAnchor.add(
                    new BoolSetting.Builder()
                            .name("differing")
                            .description("Re-routes when certain conditions are met.")
                            .defaultValue(true)
                            .build());
    public final Setting<Double> differDistance =
            sgAnchor.add(
                    new DoubleSetting.Builder()
                            .name("differ-distance")
                            .description("How close to a block you can be before re-routing.")
                            .defaultValue(0.75)
                            .sliderRange(0.1, 1.5)
                            .build());
    public final Setting<AnchorSortAlgorithm> anchorAlgorithm =
            sgAnchor.add(
                    new EnumSetting.Builder<AnchorSortAlgorithm>()
                            .name("anchor-sorting-mode")
                            .description("The blocks you want to place first.")
                            .defaultValue(AnchorSortAlgorithm.ClosestToLastBlock)
                            .build());
    protected final Setting<Integer> sortingDivisionFactor =
            sgAnchor.add(
                    new IntSetting.Builder()
                            .name("sort-division")
                            .description("The division factor for direction based sorting.")
                            .defaultValue(16)
                            .sliderRange(1, 16)
                            .build());
    public final Setting<Integer> anchorRange =
            sgAnchor.add(
                    new IntSetting.Builder()
                            .name("anchor-range")
                            .description("The range to anchor to blocks, by chunks.")
                            .defaultValue(5)
                            .sliderRange(2, 32)
                            .range(1, 128)
                            .onChanged(PlacingManager::reorderChunks)
                            .onModuleActivated((setting) -> PlacingManager.reorderChunks(setting.get()))
                            .build());
    public final Setting<Integer> anchorResetDelay =
            sgAnchor.add(
                    new IntSetting.Builder()
                            .name("anchor-reset-delay")
                            .description("Delay between resetting the anchor.")
                            .defaultValue(10)
                            .sliderRange(5, 1200)
                            .range(5, 1200)
                            .build());
    public final Setting<Integer> anchorSortDelay =
            sgAnchor.add(
                    new IntSetting.Builder()
                            .name("anchor-sort-delay")
                            .description("Delay between re-sorting the anchor list.")
                            .defaultValue(10)
                            .sliderRange(1, 1200)
                            .range(1, 1200)
                            .build());
    // Auto Swim
    private final SettingGroup sgAutoSwim = settings.createGroup("Auto Swim (WIP)");
    public final Setting<Boolean> autoSwim =
            sgAutoSwim.add(
                    new BoolSetting.Builder()
                            .name("auto-swim")
                            .description("Navigate out of water.")
                            .defaultValue(true)
                            .build());
    protected final Setting<Integer> savingGraceDelay =
            sgAutoSwim.add(
                    new IntSetting.Builder()
                            .name("saving-grace-delay")
                            .description("How often to look for suitable land.")
                            .defaultValue(5)
                            .sliderRange(0, 20)
                            .build());
    protected final Setting<Integer> savingGraceRadius =
            sgAutoSwim.add(
                    new IntSetting.Builder()
                            .name("saving-grace-radius")
                            .description("The distance around the selected position to look for suitable land.")
                            .defaultValue(64)
                            .sliderRange(16, 64)
                            .build());
    protected final Setting<Integer> o2Radius =
            sgAutoSwim.add(
                    new IntSetting.Builder()
                            .name("o2-radius")
                            .description(
                                    "The distance around the selected position to look for an opening of air.")
                            .defaultValue(24)
                            .sliderRange(8, 64)
                            .build());
    // Auto Sleep
    private final SettingGroup sgAutoSleep = settings.createGroup("Auto Sleep (WIP)");
    public final Setting<Boolean> autoSleep =
            sgAutoSleep.add(
                    new BoolSetting.Builder()
                            .name("auto-sleep")
                            .description("Sleep after a certain periods of time.")
                            .defaultValue(true)
                            .build());
    public final Setting<Integer> sleepLastRest =
            sgAutoSleep.add(
                    new IntSetting.Builder()
                            .name("since-last-rest")
                            .description("THe minimum last rest before sleeping.")
                            .defaultValue(36000)
                            .sliderRange(12000, 128000)
                            .build());
    public final Setting<BlockPos> bedPos =
            sgAutoSleep.add(
                    new BlockPosSetting.Builder()
                            .name("bed-pos")
                            .description("The position of the bed.")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    // Auto Return
    private final SettingGroup sgAutoReturn = settings.createGroup("Auto Return");
    public final Setting<Boolean> autoReturn =
            sgAutoReturn.add(
                    new BoolSetting.Builder()
                            .name("auto-return")
                            .description("Return to a set position once out of materials.")
                            .defaultValue(true)
                            .build());
    public final Setting<MoveSets> returnMove =
            sgAutoReturn.add(
                    new EnumSetting.Builder<MoveSets>()
                            .name("return-movement")
                            .description("How you return to the 'home' position.")
                            .defaultValue(MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> takeOffPitch =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("take-off-pitch")
                            .description("Pitch for taking off.")
                            .defaultValue(-7.4)
                            .decimalPlaces(1)
                            .sliderRange(-14, -5)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> descendRange =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("descend-range")
                            .description("Range from return pos before descending.")
                            .defaultValue(5)
                            .decimalPlaces(1)
                            .sliderRange(2, 10)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> minDistanceAbove =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("min-stay-above")
                            .description("Distance to stay over while flying.")
                            .defaultValue(3)
                            .decimalPlaces(1)
                            .sliderRange(3, 12)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> maxDistanceAbove =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("max-stay-above")
                            .description("Distance to decline if reached over return pos, adds over minimum.")
                            .defaultValue(3)
                            .decimalPlaces(1)
                            .sliderRange(1, 12)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> constantPitch =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("constant-pitch")
                            .description("Pitch for flying.")
                            .defaultValue(-6.7)
                            .decimalPlaces(1)
                            .sliderRange(-14, -5)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> dramaticPitchStep =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("dramatic-pitch-step")
                            .description("The step for urgent pitch changes.")
                            .defaultValue(0.34)
                            .decimalPlaces(1)
                            .sliderRange(0.1, 3)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> subtlePitchStep =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("subtle-pitch-step")
                            .description("The step for subtle pitch changes.")
                            .defaultValue(0.05)
                            .decimalPlaces(1)
                            .sliderRange(0.1, 3)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> elytraSpeed =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("elytra-speed")
                            .description("The speed to fly in bps.")
                            .sliderRange(20, 44)
                            .defaultValue(24)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> elytraSpeedStep =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("elytra-speed-step-up")
                            .description("The step for elytra speed changes.")
                            .defaultValue(1)
                            .decimalPlaces(1)
                            .sliderRange(0.5, 6)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<Double> elytraSpeedStepDec =
            sgAutoReturn.add(
                    new DoubleSetting.Builder()
                            .name("elytra-speed-step-down")
                            .description("The step for elytra speed changes.")
                            .defaultValue(1.5)
                            .decimalPlaces(1)
                            .sliderRange(0.5, 6)
                            .visible(() -> returnMove.get() == MoveSets.CONST_EFLY)
                            .build());
    public final Setting<BlockPos> returnPos =
            sgAutoReturn.add(
                    new BlockPosSetting.Builder()
                            .name("return-pos")
                            .description("The return 'home' position.")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    private final SettingGroup sgRendering = settings.createGroup("Rendering");
    public final Setting<Double> contraction =
            sgRendering.add(
                    new DoubleSetting.Builder()
                            .name("contraction")
                            .description("The rate of contraction.")
                            .sliderRange(0, 5)
                            .decimalPlaces(4)
                            .defaultValue(0.085)
                            .build());
    public final Setting<Double> strokeOffset =
            sgRendering.add(
                    new DoubleSetting.Builder()
                            .name("stroke-offset")
                            .description("The offset for stroke lines.")
                            .sliderRange(0, 0.15)
                            .decimalPlaces(4)
                            .defaultValue(0.085)
                            .build());
    public final Setting<Boolean> preserveColor =
            sgRendering.add(
                    new BoolSetting.Builder()
                            .name("preserve-color")
                            .description("Uses color from the RenderWrapper instead of constant.")
                            .defaultValue(false)
                            .build());
    public final Setting<RenderMode> renderModePlacing =
            sgRendering.add(
                    new EnumSetting.Builder<RenderMode>()
                            .name("placing-render")
                            .description("The mode for rendering placed blocks.")
                            .defaultValue(RenderMode.Static)
                            .build());
    public final Setting<PlacingThemeMode> placingTheme =
            sgRendering.add(
                    new EnumSetting.Builder<PlacingThemeMode>()
                            .name("placing-theme")
                            .description("The mode for rendering placed blocks.")
                            .defaultValue(PlacingThemeMode.Sorted)
                            .build());
    public final Setting<AsciiTheme.Preset> asciiPreset =
            sgRendering.add(
                    new EnumSetting.Builder<AsciiTheme.Preset>()
                            .name("ascii-preset")
                            .description("The preset to load for ascii theme.")
                            .visible(() -> placingTheme.get() == PlacingThemeMode.Ascii)
                            .defaultValue(AsciiTheme.Preset.Heart)
                            .build());
    public final Setting<List<SettingColor>> placingColors =
            sgRendering.add(
                    new ColorListSetting.Builder()
                            .name("placing-colors")
                            .defaultValue(List.of(new Color(255, 59, 59, 255).toSetting()))
                            .build());
    public final Setting<SettingColor> placingStrokeColor =
            sgRendering.add(
                    new ColorSetting.Builder()
                            .name("placing-stroke")
                            .defaultValue(new Color(255, 59, 59, 255))
                            .build());
    public final Setting<RenderMode> renderModeDestination =
            sgRendering.add(
                    new EnumSetting.Builder<RenderMode>()
                            .name("destination-render")
                            .description("The mode for rendering placed blocks.")
                            .defaultValue(RenderMode.Static)
                            .build());
    public final Setting<SettingColor> destinationColor =
            sgRendering.add(
                    new ColorSetting.Builder()
                            .name("destination-color")
                            .defaultValue(new Color(255, 59, 59, 255))
                            .build());
    public final Setting<SettingColor> destinationStrokeColor =
            sgRendering.add(
                    new ColorSetting.Builder()
                            .name("destination-stroke")
                            .defaultValue(new Color(255, 59, 59, 255))
                            .build());
    public final Setting<Integer> fadeTime =
            sgRendering.add(
                    new IntSetting.Builder()
                            .name("fade-time")
                            .description("Time for the rendering to fade, in ticks.")
                            .defaultValue(3)
                            .range(1, 1000)
                            .sliderRange(1, 20)
                            .build());
    // Patches
    private final SettingGroup sgPatches = settings.createGroup("Patches (WIP)");
    public final Setting<Boolean> schematicStayLoadedPatch =
            sgPatches.add(
                    new BoolSetting.Builder()
                            .name("keep-schematic-loaded")
                            .description("Prevents schematics from unloading chunks, applies even if Printer is disabled.")
                            .defaultValue(false)
                            .build());
    // Debug
    private final SettingGroup sgDebug = settings.createGroup("Debug");
    public final Setting<Integer> anchorBreakLimit =
            sgDebug.add(
                    new IntSetting.Builder()
                            .name("anchor-break-limit")
                            .description(".")
                            .defaultValue(5)
                            .sliderRange(1, 256)
                            .build());
    public final Setting<Integer> placingLimit =
            sgDebug.add(
                    new IntSetting.Builder()
                            .name("placing-limit")
                            .description(".")
                            .defaultValue(32)
                            .sliderRange(1, 256)
                            .build());
    public final Setting<Integer> anchorSoftCap =
            sgDebug.add(
                    new IntSetting.Builder()
                            .name("anchor-soft-cap")
                            .description(".")
                            .defaultValue(32)
                            .sliderRange(1, 256)
                            .build());
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    public int lastLiquidPlace = 0;
    public int nextColorIndex = 0;
    public int swapTimer = 0;
    public int placeTimer;
    public int jumpTimer = 0;
    public int blocksPlacedThisSec = 0;
    public int interestPoint = 0;
    protected double renderOffset = 0;
    protected boolean isGoingUp = true;
    // Swimming ?
    protected BlockPos airOpeningTemp;
    protected boolean wasSwimmingUp = false;
    protected BlockPos savingGrace;
    protected int savingGraceTimer = 0;
    // Sleeping
    protected boolean sleepJob = false;
    protected int sleepAttemptTimer = 0;
    protected BlockPos sleepReturnTo;
    protected long tickTimestamp = -1;
    int anchorRefreshTimer = 0;
    int anchorSortTimer = 0;
    private boolean pauseTillRefilled = false;
    private int anchorResetTimer;
    private BlockPos returnTo;
    private int lastSecond = 0;
    private int lastMessageSecond = -1;

    public Printer() {
        super(LoliUtilsMeteor.CATEGORY, "printer", "Places litematica schematics, designed for mapart.");
        PrinterUtils.PRINTER = this;
        this.timedTicks = 5;
    }

    @Override
    public void safeOnActivate() {
        onDeactivate();
    }

    @Override
    public void onDeactivate() {
        placeFading.clear();
        anchorToSort.clear();
        toSort.clear();
        anchoringTo.set(0, -999, 0);
        lastPlacedBlock.set(0, 0, 0);
        pauseTillRefilled = false;
        if (baritone.getPathingBehavior().hasPath()) {
            baritone.getPathingBehavior().cancelEverything();
        }
        mc.options.keyUp.setDown(false);
        if (wasSwimmingUp) {
            wasSwimmingUp = false;
            mc.options.keyJump.setDown(false);
        }
        airOpeningTemp = null;
        savingGrace = null;
        sleepJob = false;
        sleepAttemptTimer = 0;
        sleepReturnTo = null;

        anchorRefreshTimer = 749;

        returnMove.get().movement.cancel(BlockPos.ZERO);
        ((AsciiTheme) PlacingThemeMode.Ascii.theme).loadAscii();
    }

    @Override
    public String getInfoString() {
        String infoString = this.placingTheme.get().name();

        if (this.placingTheme.get() == PlacingThemeMode.Ascii) {
            infoString += " - " + this.asciiPreset.get();
        }

        return infoString;
    }

    @Override
    public void update() {
        interestPoint = 0;

        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            placeFading.clear();
            interestPoint = 1;
            return;
        }

        int second = LocalDateTime.now().getSecond();

        if (lastSecond != second) {
            lastSecond = second;
            blocksPlacedThisSec = 0;
        }

        lastLiquidPlace--;

        tickTimestamp = System.currentTimeMillis();

        renderingTick();

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) {
            placeFading.clear();
            toggle();
            interestPoint = 2;
            return;
        }

        if (swapTimer > 0) {
            swapTimer--;
        }

        containedColors.clear();
        containedBlocks.clear();

        for (ItemStack stack : mc.player.getInventory().getNonEquipmentItems()) {
            if (InventoryUtils.IS_BLOCK.test(stack)) {
                if (strictNoColor.get()) {
                    containedBlocks.add(stack.getItem());
                } else if (blockExclusion.get().stream()
                        .noneMatch((block -> stack.getItem() == block.asItem()))) {
                    containedColors.add(McDataCache.getColor(stack));
                }
            }
        }

        if (autoSleep.get()) {
            if (PrinterUtils.isNight() && PrinterUtils.getTimeSinceLastRest() >= sleepLastRest.get()) {
                if (!bedPos.get().equals(BlockPos.ZERO)) {
                    if (sleepReturnTo == null) {
                        sleepReturnTo = mc.player.blockPosition();
                    }

                    sleepJob = true;

                    if (MathUtils.xzDistanceBetween(mc.player.getEyePosition(), bedPos.get()) > 2) {
                        returnMovement(bedPos.get());
                    } else {
                        returnMove.get().movement.cancel(bedPos.get());

                        if (sleepAttemptTimer > 0) {
                            sleepAttemptTimer--;
                            interestPoint = 3;
                            return;
                        }

                        if (baritone.getPathingBehavior().hasPath()) {
                            baritone.getPathingBehavior().cancelEverything();
                        }

                        var rot = BlockUtils.getRotation(true, bedPos.get());

                        if (BlockUtils.canRaycast(bedPos.get(), rot.getValue(), rot.getKey())) {
                            PacketUtils.rotate(rot.getKey(), rot.getValue());

                            mc.gameMode.useItemOn(
                                    mc.player,
                                    InteractionHand.OFF_HAND,
                                    new BlockHitResult(Vec3.atCenterOf(bedPos.get()), Direction.UP, bedPos.get(), false));
                            sleepAttemptTimer = 20;
                        }
                    }
                }
            } else if (sleepJob) {
                if (mc.player.isSleeping()) {
                    if (PrinterUtils.getTimeSinceLastRest() < sleepLastRest.get()) {
                        PacketUtils.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
                    }
                } else {
                    if (pauseTillRefilled) {
                        sleepJob = false;
                        sleepAttemptTimer = 0;
                        sleepReturnTo = null;
                    } else {
                        returnMovement(sleepReturnTo);

                        if (MathUtils.xzDistanceBetween(sleepReturnTo, mc.player.blockPosition()) <= 2) {
                            sleepJob = false;
                            sleepAttemptTimer = 0;
                            sleepReturnTo = null;
                            returnMove.get().movement.cancel(sleepReturnTo);
                        }
                    }
                }
            } else {
                sleepAttemptTimer = 0;
                sleepReturnTo = null;
            }

            if (sleepJob) {
                interestPoint = 4;
                return;
            }
        }

        if (autoSwim.get()) {
            StuckFixManager.runSwimTask();

            if (mc.player.onGround()) {
                if (wasSwimmingUp) {
                    mc.options.keyJump.setDown(false);
                    mc.options.keyUp.setDown(false);
                    PRINTER.wasSwimmingUp = false;
                    PRINTER.savingGrace = null;
                }
            }

            if (StuckFixManager.shouldCancelForSwimmingTask()) {
                interestPoint = 5;
                return;
            }
        }

        if (pauseTillRefilled && autoReturn.get()) {
            placeFading.clear();
            if (mc.player.getInventory().getFreeSlot() == -1) {
                returnMovement(returnTo);
                if (MathUtils.xzDistanceBetween(returnTo, mc.player.blockPosition()) <= 0.35) {
                    if (mc.player.onGround()) {
                        pauseTillRefilled = false;
                    }

                    anchorRefreshTimer = 749;
                    returnMove.get().movement.cancel(returnTo);
                }
            } else {
                returnMovement(returnPos.get());

                anchorRefreshTimer = 749;

                if (MathUtils.xzDistanceBetween(returnPos.get(), mc.player.blockPosition()) <= 0.35) {
                    returnMove.get().movement.cancel(returnPos.get());
                }
            }
            interestPoint = 6;
            return;
        }

        if (anchor.get()) {
            if (swapTimer <= 0) {
                if ((anchoringTo.getY() != -999 && (isDiffered(anchoringTo) || BlockUtils.isNotAir(anchoringTo) || BlockUtils.hasEntitiesInside(anchoringTo)))
                        || anchorResetTimer >= anchorResetDelay.get()) {
                    anchoringTo.set(0, -999, 0);

                    interestPoint = 10;

                    anchorResetTimer = 1;
                    switch (anchorMove.get()) {
                        case Vanilla -> MoveSets.ADVANCED.movement.cancel(BlockPos.ZERO);
                        case Baritone -> {
                            if (baritone.getPathingBehavior().hasPath())
                                baritone.getPathingBehavior().cancelEverything();
                        }
                    }
                } else if (anchoringTo.getY() == -999) {
                    switch (anchorMove.get()) {
                        case Vanilla -> MoveSets.ADVANCED.movement.cancel(BlockPos.ZERO);
                        case Baritone -> {
                            if (baritone.getPathingBehavior().hasPath())
                                baritone.getPathingBehavior().cancelEverything();
                        }
                    }
                } else {
                    if (anchorSortDelay.get() >= anchorSortTimer) {
                        anchorToSort.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

                        anchorSortTimer = 0;
                    }

                    anchorSortTimer++;

                    switch (anchorMove.get()) {
                        case Vanilla -> MoveSets.ADVANCED.movement.tick(anchoringTo);
                        case Baritone -> {
                            if (!baritone.getPathingBehavior().hasPath()) {
                                baritoneTo(anchoringTo);
                            }
                        }
                    }
                }

                anchorRefreshTimer++;

                if (anchoringTo.getY() == -999) {
                    if (anchorRefreshTimer >= 60) {
                        BlockPos.MutableBlockPos srcBlock = new BlockPos.MutableBlockPos(0, 0, 0);

                        List<int[]> anchoredBlocks = PrinterUtils.findNearBlocksByChunk(mc.player.blockPosition().mutable().setY(yLevel.get()).immutable(),
                                anchorRange.get(),
                                (pos) -> {
                                    srcBlock.set(pos[0], yLevel.get(), pos[2]);

                                    BlockState blockState = mc.level.getBlockState(srcBlock);
                                    BlockState required = worldSchematic.getBlockState(srcBlock);

                                    if (blockState.isAir() && !required.isAir() && !BlockUtils.hasEntitiesInside(srcBlock)) {
                                        return ((!strictNoColor.get()
                                                && containedColors.contains(
                                                McDataCache.getColor(required.getBlock().asItem())))
                                                || (strictNoColor.get()
                                                && containedBlocks.contains(required.getBlock().asItem())))
                                                && !isDiffered(srcBlock);
                                    }

                                    return false;
                                });

                        anchorToSort.clear();

                        anchoredBlocks.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

                        int maxAnchoredBlocks = 0;

                        int anchorLimit = anchorSoftCap.get();

                        for (int[] posVec : anchoredBlocks) {
                            if (maxAnchoredBlocks >= anchorLimit) {
                                break;
                            }

                            maxAnchoredBlocks++;
                            anchorToSort.add(posVec);
                        }

                        anchoredBlocks.clear();

                        anchorRefreshTimer = 0;
                        anchorSortTimer = 0;
                    }

                    if (!anchorToSort.isEmpty()) {
                        anchoringTo = anchoringTo.set(anchorToSort.getFirst()[0], anchorToSort.getFirst()[1], anchorToSort.getFirst()[2]);
                        anchorToSort.removeFirst();
                    } else if ((!pauseTillRefilled && anchorRefreshTimer == 0) || containedColors.isEmpty()) {
                        if (lastMessageSecond != lastSecond) {
                            info("No block to anchor to, going to return position.");
                            lastMessageSecond = lastSecond;
                        }

                        returnTo = mc.player.blockPosition();
                        pauseTillRefilled = true;
                    }
                }

                anchorResetTimer++;
            } else {
                interestPoint = 11;
                mc.options.keyUp.setDown(false);
                if (baritone.getPathingBehavior().hasPath())
                    baritone.getPathingBehavior().cancelEverything();
            }
        }

        placeFading.forEach(
                s -> {
                    if (renderModePlacing.get() == RenderMode.Breath) {
                        s.breath(s.breath() - 1);
                    } else {
                        s.fadeTime(s.fadeTime() - 1);
                    }
                });
        placeFading.removeIf(
                s -> s.fadeTime() <= 0 || s.breath() * contraction.get() <= -1);

        toSort.clear();

        if (placeTimer >= delay.get()) {
            if (mc.player.isUsingItem() || !passesChecks()) {
                interestPoint = 7;
                return;
            }

            PlacingManager.tryPlacingBlocks();

        } else placeTimer++;
    }

    public boolean passesChecks() {
        if (mc.player == null) {
            return false;
        }

        if (onlyOnGround.get() && !mc.player.onGround()) {
            return false;
        }

        // Don't simplify these in case we add more! The IDE does not know the vision!

        return !notInLiquid.get() || !mc.player.isInLiquid();
    }

    public boolean isDiffered(BlockPos pos) {
        if (mc.player == null || !differing.get()) return false;

        return MathUtils.xzDistanceBetween(mc.player.getEyePosition(), pos) <= differDistance.get();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        assert mc.player != null : "Player has not joined the game.";
        try {
            placeFading.forEach(
                    s -> renderBlock(
                            event.renderer,
                            s,
                            RenderType.Placing,
                            renderModePlacing.get()));
        } catch (ConcurrentModificationException ignored) {
        }

        if (airOpeningTemp != null && mc.player.getEyeY() < airOpeningTemp.getY()) {
            renderBlock(
                    event.renderer,
                    airOpeningTemp.getX(),
                    airOpeningTemp.getY(),
                    airOpeningTemp.getZ(),
                    airOpeningTemp.getX() + 1,
                    airOpeningTemp.getY() + 1,
                    airOpeningTemp.getZ() + 1,
                    renderModeDestination.get(),
                    0);
        } else if (savingGrace != null) {
            renderBlock(
                    event.renderer,
                    savingGrace.getX(),
                    savingGrace.getY(),
                    savingGrace.getZ(),
                    savingGrace.getX() + 1,
                    savingGrace.getY() + 1,
                    savingGrace.getZ() + 1,
                    renderModeDestination.get(),
                    0);
        } else if (anchoringTo.getY() != -999) {
            renderBlock(
                    event.renderer,
                    anchoringTo.getX(),
                    anchoringTo.getY(),
                    anchoringTo.getZ(),
                    anchoringTo.getX() + 1,
                    anchoringTo.getY() + 1,
                    anchoringTo.getZ() + 1,
                    renderModeDestination.get(),
                    0);
        }
    }

    public void returnMovement(BlockPos pos) {
        returnMove.get().movement.tick(pos);
    }

    private void baritoneTo(BlockPos pos) {
        if (baritone.getPathingBehavior().hasPath()) baritone.getPathingBehavior().cancelEverything();
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(pos, 2));
    }

    public void renderingTick() {
        if (renderModePlacing.get() != RenderMode.Static
                || renderModeDestination.get() != RenderMode.Static) {
            if (renderOffset <= 0) {
                isGoingUp = true;
            } else if (renderOffset >= 1) {
                isGoingUp = false;
            }

            renderOffset += isGoingUp ? contraction.get() : -contraction.get();
        } else {
            renderOffset = 0;
        }
    }

    public void renderBlock(Renderer3D renderer, RenderWrap wrap, RenderType type, RenderMode mode) {
        Color color = wrap.color();

        int origAlpha = color.a;

        switch (mode) {
            case Static -> {
                renderer.boxLines(wrap.blockPos().getX(), wrap.blockPos().getY(), wrap.blockPos().getZ(), wrap.blockPos().getX() + 1, wrap.blockPos().getY() + 1, wrap.blockPos().getZ() + 1, color, 0);
                color.a(60);
                renderer.boxSides(wrap.blockPos().getX(), wrap.blockPos().getY(), wrap.blockPos().getZ(), wrap.blockPos().getX() + 1, wrap.blockPos().getY() + 1, wrap.blockPos().getZ() + 1, color, 0);
                color.a(origAlpha);
            }

            case Breath -> {
                renderer.boxLines(wrap.blockPos().getX(), wrap.blockPos().getY(), wrap.blockPos().getZ(), wrap.blockPos().getX() + 1, wrap.blockPos().getY() + 1 + (wrap.breath() * contraction.get()), wrap.blockPos().getZ() + 1, color, 0);
                color.a(60);
                renderer.boxSides(wrap.blockPos().getX(), wrap.blockPos().getY(), wrap.blockPos().getZ(), wrap.blockPos().getX() + 1, wrap.blockPos().getY() + 1 + (wrap.breath() * contraction.get()), wrap.blockPos().getZ() + 1, color, 0);
                color.a(origAlpha);
            }

            case Stroke -> {
                renderer.boxLines(wrap.blockPos().getX(), wrap.blockPos().getY(), wrap.blockPos().getZ(), wrap.blockPos().getX() + 1, wrap.blockPos().getY() + 1, wrap.blockPos().getZ() + 1, color, 0);
                color.a(60);
                renderer.boxSides(wrap.blockPos().getX(), wrap.blockPos().getY(), wrap.blockPos().getZ(), wrap.blockPos().getX() + 1, wrap.blockPos().getY() + 1, wrap.blockPos().getZ() + 1, color, 0);
                color.a(140);
                renderer.boxLines(
                        wrap.blockPos().getX() - strokeOffset.get(),
                        wrap.blockPos().getY() + renderOffset,
                        wrap.blockPos().getZ() - strokeOffset.get(),
                        wrap.blockPos().getX() + 1 + strokeOffset.get(),
                        wrap.blockPos().getY() + renderOffset,
                        wrap.blockPos().getZ() + 1 + strokeOffset.get(),
                        switch (type) {
                            case Placing -> placingStrokeColor.get();
                            case Destination -> destinationStrokeColor.get();
                        },
                        0);
                renderer.boxLines(
                        wrap.blockPos().getX() - strokeOffset.get(),
                        wrap.blockPos().getY() + Math.min(Math.max(0, renderOffset / 1.7), 1),
                        wrap.blockPos().getZ() - strokeOffset.get(),
                        wrap.blockPos().getX() + 1 + strokeOffset.get(),
                        wrap.blockPos().getY() + renderOffset,
                        wrap.blockPos().getZ() + 1 + strokeOffset.get(),
                        switch (type) {
                            case Placing -> placingStrokeColor.get();
                            case Destination -> destinationStrokeColor.get();
                        },
                        0);
                color.a(origAlpha);
            }
        }
    }

    public void renderBlock(
            Renderer3D renderer,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            RenderMode mode,
            int breath) {
        Color color = destinationColor.get();

        int origAlpha = color.a;

        switch (mode) {
            case Static -> {
                renderer.boxLines(x1, y1, z1, x2, y2, z2, color, 0);
                color.a(60);
                renderer.boxSides(x1, y1, z1, x2, y2, z2, color, 0);
                color.a(origAlpha);
            }

            case Breath -> {
                renderer.boxLines(x1, y1, z1, x2, y2 + (breath * contraction.get()), z2, color, 0);
                color.a(60);
                renderer.boxSides(x1, y1, z1, x2, y2 + (breath * contraction.get()), z2, color, 0);
                color.a(origAlpha);
            }

            case Stroke -> {
                renderer.boxLines(x1, y1, z1, x2, y2, z2, color, 0);
                color.a(60);
                renderer.boxSides(x1, y1, z1, x2, y2, z2, color, 0);
                color.a(140);
                renderer.boxLines(
                        x1 - strokeOffset.get(),
                        y1 + renderOffset,
                        z1 - strokeOffset.get(),
                        x2 + strokeOffset.get(),
                        y1 + renderOffset,
                        z2 + strokeOffset.get(),
                        destinationStrokeColor.get(),
                        0);
                renderer.boxLines(
                        x1 - strokeOffset.get(),
                        y1 + Math.min(Math.max(0, renderOffset / 1.7), 1),
                        z1 - strokeOffset.get(),
                        x2 + strokeOffset.get(),
                        y1 + renderOffset,
                        z2 + strokeOffset.get(), destinationStrokeColor.get(),
                        0);
                color.a(origAlpha);
            }
        }
    }

    public SettingColor getNextPlaceColor(BlockPos pos) {
        if (placingColors.get().isEmpty()) {
            return SettingColor.CYAN.toSetting();
        }

        return placingTheme.get().theme.getNextColor(pos);
    }

    public enum AnchorMovement {
        Baritone,
        Vanilla
    }

    @SuppressWarnings("unused")
    public enum AnchorSortAlgorithm {
        RSNorth(
                SortAlgorithm.Closest,
                Comparator.comparingDouble(
                        value -> {
                            int v =
                                    Math.toIntExact(value.getZ() / PrinterUtils.PRINTER.sortingDivisionFactor.get());

                            assert MeteorClient.mc.player != null;
                            float altDist =
                                    Math.toIntExact(value.getX() / PrinterUtils.PRINTER.sortingDivisionFactor.get())
                                            - Math.toIntExact(
                                            MeteorClient.mc.player.getBlockX()
                                                    / PrinterUtils.PRINTER.sortingDivisionFactor.get());
                            return v + ((double) Mth.sqrt(altDist * altDist) / 64);
                        })),
        RSEast(
                SortAlgorithm.Closest,
                Comparator.comparingDouble(
                        value -> {
                            int v =
                                    Math.toIntExact(value.getX() / PrinterUtils.PRINTER.sortingDivisionFactor.get());

                            assert MeteorClient.mc.player != null;
                            float altDist =
                                    Math.toIntExact(value.getZ() / PrinterUtils.PRINTER.sortingDivisionFactor.get())
                                            - Math.toIntExact(
                                            MeteorClient.mc.player.getBlockZ()
                                                    / PrinterUtils.PRINTER.sortingDivisionFactor.get());
                            return -v + ((double) Mth.sqrt(altDist * altDist) / 64);
                        })),
        RSSouth(
                SortAlgorithm.Closest,
                Comparator.comparingDouble(
                        value -> {
                            int v =
                                    Math.toIntExact(value.getZ() / PrinterUtils.PRINTER.sortingDivisionFactor.get());

                            assert MeteorClient.mc.player != null;
                            float altDist =
                                    Math.toIntExact(value.getX() / PrinterUtils.PRINTER.sortingDivisionFactor.get())
                                            - Math.toIntExact(
                                            MeteorClient.mc.player.getBlockX()
                                                    / PrinterUtils.PRINTER.sortingDivisionFactor.get());
                            return -v + ((double) Mth.sqrt(altDist * altDist) / 64);
                        })),
        RSWest(
                SortAlgorithm.Closest,
                Comparator.comparingDouble(
                        value -> {
                            int v =
                                    Math.toIntExact(value.getX() / PrinterUtils.PRINTER.sortingDivisionFactor.get());

                            assert MeteorClient.mc.player != null;
                            float altDist =
                                    Math.toIntExact(value.getZ() / PrinterUtils.PRINTER.sortingDivisionFactor.get())
                                            - Math.toIntExact(
                                            MeteorClient.mc.player.getBlockZ()
                                                    / PrinterUtils.PRINTER.sortingDivisionFactor.get());
                            return v + ((double) Mth.sqrt(altDist * altDist) / 64);
                        })),
        ClosestToLastBlock(
                SortAlgorithm.Closest,
                Comparator.comparingDouble(
                        value ->
                                MeteorClient.mc.player != null
                                        ? Utils.squaredDistance(
                                        anchoringTo.getY() != -999 ? anchoringTo.getX() : MeteorClient.mc.player.getX(),
                                        anchoringTo.getY() != -999 ? anchoringTo.getY() : MeteorClient.mc.player.getY(),
                                        anchoringTo.getY() != -999 ? anchoringTo.getZ() : MeteorClient.mc.player.getZ(),
                                        value.getX() + 0.5,
                                        value.getY() + 0.5,
                                        value.getZ() + 0.5)
                                        : 0));

        final SortAlgorithm secondaryAlgorithm;
        final Comparator<BlockPos> algorithm;

        AnchorSortAlgorithm(SortAlgorithm secondaryAlgorithm, Comparator<BlockPos> algorithm) {
            this.secondaryAlgorithm = secondaryAlgorithm;
            this.algorithm = algorithm;
        }
    }

    @SuppressWarnings("unused")
    public enum PlacingThemeMode {
        X(new XTheme()),
        Z(new ZTheme()),
        XZ(new XZTheme()),
        Sorted(new SortedTheme()),
        Random(new RandomTheme()),
        Ascii(new AsciiTheme());

        final PlacingTheme theme;

        PlacingThemeMode(PlacingTheme theme) {
            this.theme = theme;
        }
    }

    @SuppressWarnings("unused")
    public enum SortAlgorithm {
        Closest(
                false,
                Comparator.comparingDouble(
                        value ->
                                MeteorClient.mc.player != null
                                        ? Utils.squaredDistance(
                                        MeteorClient.mc.player.getX(),
                                        MeteorClient.mc.player.getY(),
                                        MeteorClient.mc.player.getZ(),
                                        value[0] + 0.5,
                                        value[1] + 0.5,
                                        value[2] + 0.5)
                                        : 0)),
        ClosestToLastBlock(
                false,
                Comparator.comparingDouble(
                        value ->
                                MeteorClient.mc.player != null
                                        ? Utils.squaredDistance(
                                        lastPlacedBlock.getX(), lastPlacedBlock.getY(), lastPlacedBlock.getZ(),
                                        value[0] + 0.5, value[1] + 0.5, value[2] + 0.5
                                )
                                        : 0)),
        Furthest(
                false,
                Comparator.comparingDouble(
                        value ->
                                MeteorClient.mc.player != null
                                        ? -(Utils.squaredDistance(
                                        MeteorClient.mc.player.getX(),
                                        MeteorClient.mc.player.getY(),
                                        MeteorClient.mc.player.getZ(),
                                        value[0] + 0.5,
                                        value[1] + 0.5,
                                        value[2] + 0.5))
                                        : 0));

        final boolean applySecondSorting;
        final Comparator<int[]> algorithm;

        SortAlgorithm(boolean applySecondSorting, Comparator<int[]> algorithm) {
            this.applySecondSorting = applySecondSorting;
            this.algorithm = algorithm;
        }
    }

    public enum RenderType {
        Placing,
        Destination
    }

    public enum RenderMode {
        Breath,
        Stroke,
        Static
    }

    @SuppressWarnings("unused")
    public enum SortingSecond {
        None(SortAlgorithm.Closest.algorithm),
        Nearest(SortAlgorithm.Closest.algorithm),
        Furthest(SortAlgorithm.Furthest.algorithm);

        final Comparator<int[]> algorithm;

        SortingSecond(Comparator<int[]> algorithm) {
            this.algorithm = algorithm;
        }
    }


}
