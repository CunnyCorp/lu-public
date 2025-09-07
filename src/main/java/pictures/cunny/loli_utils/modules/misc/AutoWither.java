package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.render.LoliRendering;
import pictures.cunny.loli_utils.utility.render.RenderWrap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoWither extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> radius = sgDefault.add(
            new IntSetting.Builder()
                    .name("radius")
                    .description("How far to check for valid spawns.")
                    .sliderRange(1, 5)
                    .defaultValue(3)
                    .build()
    );
    public final Setting<Integer> spawnDelay = sgDefault.add(
            new IntSetting.Builder()
                    .name("spawn-delay")
                    .description("How long to wait between spawns.")
                    .sliderRange(1, 40)
                    .defaultValue(5)
                    .build()
    );
    public final Setting<Integer> placeDelay = sgDefault.add(
            new IntSetting.Builder()
                    .name("place-delay")
                    .description("How long to wait between placing blocks.")
                    .sliderRange(1, 40)
                    .defaultValue(2)
                    .build()
    );
    public final Setting<Integer> swapDelay = sgDefault.add(
            new IntSetting.Builder()
                    .name("swap-delay")
                    .description("How long to wait before swapping items.")
                    .sliderRange(1, 40)
                    .defaultValue(5)
                    .build()
    );
    public final Setting<Double> placeDistance = sgDefault.add(
            new DoubleSetting.Builder()
                    .name("place-distance")
                    .description("The max distance to place blocks.")
                    .defaultValue(4.5)
                    .sliderRange(3.2, 5.0)
                    .build()
    );
    public final Setting<Boolean> rotate = sgDefault.add(
            new BoolSetting.Builder()
                    .name("rotate")
                    .description("Rotate towards blocks when placing.")
                    .defaultValue(true)
                    .build()
    );
    public final Setting<Boolean> checkSpace = sgDefault.add(
            new BoolSetting.Builder()
                    .name("check-space")
                    .description("Check for free space around wither spawn.")
                    .defaultValue(true)
                    .build()
    );
    // New setting for orientation
    public final Setting<Orientation> orientation = sgDefault.add(
            new EnumSetting.Builder<Orientation>()
                    .name("orientation")
                    .description("How to orient the wither when placing.")
                    .defaultValue(Orientation.UPRIGHT)
                    .build()
    );
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<SettingColor> renderColor = sgRender.add(
            new ColorSetting.Builder()
                    .name("color")
                    .description("Color for placed blocks.")
                    .defaultValue(new Color(195, 126, 234, 111))
                    .build()
    );

    private final Setting<SettingColor> renderOriginColor = sgRender.add(
            new ColorSetting.Builder()
                    .name("origin-color")
                    .description("Color for the origin position.")
                    .defaultValue(new Color(195, 126, 234, 111))
                    .build()
    );

    private final Setting<SettingColor> renderPreviewColor = sgRender.add(
            new ColorSetting.Builder()
                    .name("preview-color")
                    .description("Color for the wither preview.")
                    .defaultValue(new Color(255, 0, 0, 80))
                    .build()
    );

    private final Setting<Integer> fadeTime = sgRender.add(
            new IntSetting.Builder()
                    .name("fade-time")
                    .description("How fast to fade out.")
                    .defaultValue(160)
                    .sliderRange(20, 1000)
                    .build()
    );

    private final Setting<Boolean> renderPreview = sgRender.add(
            new BoolSetting.Builder()
                    .name("render-preview")
                    .description("Render wither structure preview.")
                    .defaultValue(true)
                    .build()
    );
    private final List<RenderWrap> renderWrapping = new ArrayList<>();
    private int swapCooldown = 0;
    private int placeCooldown = 0;
    private int spawnCooldown = 0;
    private WitherSpawn witherSpawn = null;
    private Direction currentDirection = Direction.NORTH;

    public AutoWither() {
        super(LoliUtilsMeteor.CATEGORY, "auto-wither", "Automatically spawns withers.");
    }

    @EventHandler
    public void render3DEvent(Render3DEvent event) {
        if (witherSpawn != null) {
            // Render origin point
            Color originColor = renderOriginColor.get().copy();
            BlockPos origin = witherSpawn.origin();
            event.renderer.boxLines(
                    origin.getX() + 0.3, origin.getY(), origin.getZ() + 0.3,
                    origin.getX() + 0.6, origin.getY() + 1.3, origin.getZ() + 0.6,
                    originColor, 2
            );
            originColor.a(60);
            event.renderer.boxSides(
                    origin.getX() + 0.3, origin.getY(), origin.getZ() + 0.3,
                    origin.getX() + 0.6, origin.getY() + 1.3, origin.getZ() + 0.6,
                    originColor, 2
            );

            // Render wither preview
            if (renderPreview.get()) {
                Color previewColor = renderPreviewColor.get().copy();
                for (BlockPos pos : witherSpawn.allPositions()) {
                    event.renderer.boxSides(
                            pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                            previewColor, 2
                    );
                }
            }
        }

        // Render placed blocks
        if (!renderWrapping.isEmpty()) {
            renderWrapping.removeIf(wrap -> wrap.fadeTime() <= 0);
            for (RenderWrap wrap : renderWrapping) {
                LoliRendering.renderRetractingCube(event.renderer, wrap);
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        LoliRendering.tickFadeTime(renderWrapping);

        if (spawnCooldown > 0) spawnCooldown--;
        if (swapCooldown > 0) swapCooldown--;
        if (placeCooldown > 0) placeCooldown--;

        if (spawnCooldown > 0 || swapCooldown > 0 || placeCooldown > 0) {
            return;
        }

        if (witherSpawn == null) {
            findWitherSpawn();
        } else {
            if (Math.abs(mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(witherSpawn.origin()))) >= 8) {
                LoliUtilsMeteor.LOGGER.info("Resetting spawn - too far away.");
                witherSpawn = null;
                return;
            }
            placeSpawn();
        }
    }

    private void findWitherSpawn() {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        List<WitherSpawn> witherSpawns = new ArrayList<>();

        for (int x = -radius.get(); x <= radius.get(); x++) {
            for (int y = -radius.get(); y <= radius.get(); y++) {
                for (int z = -radius.get(); z <= radius.get(); z++) {
                    mutable.set(mc.player.getBlockX() + x, mc.player.getBlockY() + y, mc.player.getBlockZ() + z);

                    // Try all directions for optimal placement based on orientation
                    for (Direction direction : getValidDirections()) {
                        if (isValidSpawn(mutable, direction)) {
                            witherSpawns.add(createWitherSpawn(mutable.immutable(), direction));
                        }
                    }
                }
            }
        }

        if (!witherSpawns.isEmpty()) {
            // Sort by distance and space availability
            witherSpawns.sort(Comparator.comparingDouble(wither ->
                    wither.origin().distSqr(mc.player.blockPosition()) +
                            (checkSpace.get() ? calculateSpaceScore(wither) : 0)
            ));
            witherSpawn = witherSpawns.getFirst();
            currentDirection = witherSpawn.direction();
            LoliUtilsMeteor.LOGGER.info("Found valid spawn at {} facing {}", witherSpawn.origin(), currentDirection);
        } else {
            LoliUtilsMeteor.LOGGER.info("No valid spawn found.");
        }
    }

    private List<Direction> getValidDirections() {
        List<Direction> directions = new ArrayList<>();

        switch (orientation.get()) {
            case UPRIGHT:
                // Only horizontal directions for upright wither
                directions.addAll(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
                break;
            case SIDEWAYS:
                // All directions including up/down for sideways wither
                directions.addAll(List.of(Direction.values()));
                break;
        }

        return directions;
    }

    private WitherSpawn createWitherSpawn(BlockPos origin, Direction direction) {
        List<BlockPos> body = new ArrayList<>();
        List<BlockPos> skulls = new ArrayList<>();
        List<BlockPos> allPositions = new ArrayList<>();

        if (orientation.get() == Orientation.UPRIGHT) {
            // Upright wither (original implementation)
            body.add(origin);
            body.add(origin.above());
            body.add(origin.relative(direction.getClockWise()));
            body.add(origin.above().relative(direction.getClockWise()));
            body.add(origin.relative(direction.getCounterClockWise()));
            body.add(origin.above().relative(direction.getCounterClockWise()));

            // Skulls
            skulls.add(origin.above(2));
            skulls.add(origin.above(2).relative(direction.getClockWise()));
            skulls.add(origin.above(2).relative(direction.getCounterClockWise()));
        } else {
            // Sideways wither - orientation depends on the direction
            if (direction.getAxis().isHorizontal()) {
                // Horizontal placement (on side)
                body.add(origin);
                body.add(origin.relative(direction));
                body.add(origin.relative(direction.getClockWise()));
                body.add(origin.relative(direction).relative(direction.getClockWise()));
                body.add(origin.relative(direction.getCounterClockWise()));
                body.add(origin.relative(direction).relative(direction.getCounterClockWise()));

                // Skulls
                skulls.add(origin.relative(direction, 2));
                skulls.add(origin.relative(direction, 2).relative(direction.getClockWise()));
                skulls.add(origin.relative(direction, 2).relative(direction.getCounterClockWise()));
            } else {
                // Vertical placement (up/down)
                Direction horizontalDir = Direction.NORTH; // Default horizontal direction
                body.add(origin);
                body.add(origin.relative(direction));
                body.add(origin.relative(horizontalDir.getClockWise()));
                body.add(origin.relative(direction).relative(horizontalDir.getClockWise()));
                body.add(origin.relative(horizontalDir.getCounterClockWise()));
                body.add(origin.relative(direction).relative(horizontalDir.getCounterClockWise()));

                // Skulls
                skulls.add(origin.relative(direction, 2));
                skulls.add(origin.relative(direction, 2).relative(horizontalDir.getClockWise()));
                skulls.add(origin.relative(direction, 2).relative(horizontalDir.getCounterClockWise()));
            }
        }

        allPositions.addAll(body);
        allPositions.addAll(skulls);

        return new WitherSpawn(origin, skulls, body, allPositions, direction);
    }

    private double calculateSpaceScore(WitherSpawn spawn) {
        double score = 0;
        // Add penalty for each obstructed block around the spawn
        for (BlockPos pos : spawn.allPositions()) {
            for (Direction dir : Direction.values()) {
                BlockPos checkPos = pos.relative(dir);
                if (!mc.level.getBlockState(checkPos).isAir()) {
                    score += 0.5;
                }
            }
        }
        return score;
    }

    public void placeSpawn() {
        if (witherSpawn == null) return;

        // Place body parts first
        for (BlockPos bodyPos : witherSpawn.body()) {
            if (!mc.level.getBlockState(bodyPos).getBlock().equals(Blocks.SOUL_SAND)) {
                if (placeBlock(bodyPos, Items.SOUL_SAND)) {
                    return;
                }
            }
        }

        // Place skulls after body is complete
        int skullsPlaced = 0;
        for (BlockPos headPos : witherSpawn.skulls()) {
            if (mc.level.getBlockState(headPos).getBlock().equals(Blocks.WITHER_SKELETON_SKULL)) {
                skullsPlaced++;
            } else if (placeBlock(headPos, Items.WITHER_SKELETON_SKULL)) {
                return;
            }
        }

        if (skullsPlaced == 3) {
            LoliUtilsMeteor.LOGGER.info("Wither spawn completed at {}", witherSpawn.origin());
            witherSpawn = null;
            spawnCooldown = spawnDelay.get();
        }
    }

    private boolean placeBlock(BlockPos pos, net.minecraft.world.item.Item item) {
        if (!BlockUtils.canPlace(pos, placeDistance.get())) {
            return false;
        }

        if (mc.player.getMainHandItem().getItem() != item) {
            FindItemResult foundItem = InvUtils.find(stack -> stack.getItem() == item);
            if (!foundItem.found()) return false;

            swapCooldown = swapDelay.get();
            if (!foundItem.isHotbar()) {
                InventoryUtils.swapToHotbar(foundItem.slot(), mc.player.getInventory().getSelectedSlot());
            } else if (mc.player.getInventory().getSelectedSlot() != foundItem.slot()) {
                InventoryUtils.swapSlot(foundItem.slot());
            }
            return true;
        }

        placeCooldown = placeDelay.get();
        mc.player.swing(InteractionHand.MAIN_HAND);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, BlockUtils.getSafeHitResult(pos));
        renderWrapping.add(new RenderWrap(pos.immutable(), fadeTime.get(), 0, 0, renderColor.get()));

        return true;
    }

    private boolean isValidSpawn(BlockPos blockPos, Direction direction) {
        // Check foundation based on orientation
        if (orientation.get() == Orientation.UPRIGHT) {
            // Upright wither needs solid block below
            if (!BlockUtils.isReplaceable(blockPos.below())) {
                return false;
            }
        } else {
            // Sideways wither needs solid block opposite to the direction
            if (!BlockUtils.isReplaceable(blockPos.relative(direction.getOpposite()))) {
                return false;
            }
        }

        // Check all wither blocks
        WitherSpawn testSpawn = createWitherSpawn(blockPos, direction);
        for (BlockPos pos : testSpawn.allPositions()) {
            if (BlockUtils.isNotAir(pos) || BlockUtils.hasEntitiesInside(pos)) {
                return false;
            }
        }

        return true;
    }

    public enum Orientation {
        UPRIGHT,
        SIDEWAYS
    }

    private record WitherSpawn(
            BlockPos origin,
            List<BlockPos> skulls,
            List<BlockPos> body,
            List<BlockPos> allPositions,
            Direction direction
    ) {
    }
}