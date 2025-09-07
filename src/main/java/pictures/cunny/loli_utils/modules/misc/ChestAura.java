package pictures.cunny.loli_utils.modules.misc;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalNear;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.events.CloseScreenEvent;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;
import pictures.cunny.loli_utils.utility.render.LoliRendering;

import java.util.ArrayList;
import java.util.List;

public class ChestAura extends Module {
    private final List<int[]> knownEmptyChests = new ArrayList<>();

    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<List<Item>> items =
            sgDefault.add(
                    new ItemListSetting.Builder()
                            .name("items")
                            .description("A list of items to consider valid.")
                            .defaultValue(Items.SHULKER_BOX)
                            .build());
    public final Setting<BlockPos> nearPos =
            sgDefault.add(
                    new BlockPosSetting.Builder()
                            .name("near-pos")
                            .description("The 'home' position where you will be jewing items.")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    public final Setting<Boolean> updateOnBed =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("update-on-bed")
                            .description("Updates near pos when setting bed.")
                            .defaultValue(true)
                            .build());
    public final Setting<Boolean> useBaritone =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("baritone")
                            .description("Uses baritone to automate jewing further.")
                            .defaultValue(true)
                            .build());
    public final Setting<Integer> maxDistance =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("max-distance")
                            .description("Maximum distance from near-pos to open chests.")
                            .defaultValue(32)
                            .sliderRange(1, 128)
                            .build());
    public final Setting<Integer> range =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("range")
                            .description("How far around the player to check for chests.")
                            .defaultValue(4)
                            .sliderRange(2, 48)
                            .build());
    public final Setting<Integer> yRange =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("y-range")
                            .description("How far around the player to check for chests.")
                            .defaultValue(4)
                            .sliderRange(2, 6)
                            .build());

    public final Setting<Double> useDistance =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("use-distance")
                            .description("The max distance to use containers.")
                            .defaultValue(5)
                            .sliderRange(3.2, 5.0)
                            .build());
    public final Setting<Integer> delay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("delay")
                            .description("How long between opening chest's.")
                            .defaultValue(10)
                            .sliderRange(1, 20)
                            .build());
    private final SettingGroup sgRender = settings.createGroup("Render");
    public final Setting<Boolean> rendering =
            sgRender.add(
                    new BoolSetting.Builder()
                            .name("rendering")
                            .description("Enables module rendering.")
                            .defaultValue(true)
                            .build());
    public final Setting<Boolean> renderEmpty =
            sgRender.add(
                    new BoolSetting.Builder()
                            .name("render-empty")
                            .description("Renders known empty containers.")
                            .defaultValue(true)
                            .build());
    private final Setting<SettingColor> emptyColor =
            sgRender.add(
                    new ColorSetting.Builder()
                            .name("empty-color")
                            .defaultValue(new Color(255, 0, 20, 200))
                            .visible(renderEmpty::get)
                            .build());
    public final Setting<Boolean> renderOutline =
            sgRender.add(
                    new BoolSetting.Builder()
                            .name("render-outline")
                            .description("Renders usable scan outline.")
                            .defaultValue(true)
                            .build());
    private final Setting<SettingColor> outlineColor =
            sgRender.add(
                    new ColorSetting.Builder()
                            .name("outline-color")
                            .defaultValue(new Color(195, 126, 234, 111))
                            .visible(renderOutline::get)
                            .build());
    public final Setting<Boolean> renderTracers =
            sgRender.add(
                    new BoolSetting.Builder()
                            .name("render-tracers")
                            .description("Renders tracers to actively/last used container.")
                            .defaultValue(true)
                            .build());
    private final Setting<SettingColor> tracerColor =
            sgRender.add(
                    new ColorSetting.Builder()
                            .name("tracer-color")
                            .defaultValue(new Color(195, 126, 234, 111))
                            .visible(renderTracers::get)
                            .build());
    public final Setting<Integer> tracerLines =
            sgRender.add(
                    new IntSetting.Builder()
                            .name("tracer-lines")
                            .description("How many tracer lines to make.")
                            .defaultValue(5)
                            .sliderRange(3, 20)
                            .visible(renderTracers::get)
                            .build());


    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    private BlockPos lastClickedContainer = BlockPos.ZERO;
    private boolean waitingForClose = false;
    private int ticks = 0;
    private boolean isFindingChest = false;
    private BlockPos gotoChest = BlockPos.ZERO;

    public ChestAura() {
        super(LoliUtilsMeteor.CATEGORY, "chest-user", "Uses nearby chests if your inventory has free space.");
    }

    @Override
    public void onActivate() {
        waitingForClose = false;
        lastClickedContainer = BlockPos.ZERO;
        baritone.getPathingBehavior().cancelEverything();
    }

    @EventHandler
    public void onScreenClose(CloseScreenEvent event) {
        if (mc.player == null) {
            return;
        }

        try {
            if (waitingForClose && mc.player.containerMenu.getType() == MenuType.GENERIC_9x6) {
                boolean containerHasItems = false;

                for (int i = 0; i < SlotUtils.indexToId(SlotUtils.MAIN_START); i++) {
                    if (mc.player.containerMenu.getSlot(i).hasItem() && items.get().contains(mc.player.containerMenu.getSlot(i).getItem().getItem())) {
                        containerHasItems = true;
                        break;
                    }
                }

                if (!containerHasItems) {
                    knownEmptyChests.removeIf(ints -> lastClickedContainer.getX() == ints[0] && lastClickedContainer.getY() == ints[1] && lastClickedContainer.getZ() == ints[2]);
                    knownEmptyChests.add(new int[]{lastClickedContainer.getX(), lastClickedContainer.getY(), lastClickedContainer.getZ()});
                    Direction direction = ChestBlock.getConnectedDirection(mc.level.getBlockState(lastClickedContainer));
                    BlockPos side = lastClickedContainer.relative(direction);
                    knownEmptyChests.add(new int[]{side.getX(), side.getY(), side.getZ()});
                }
            }
        } catch (UnsupportedOperationException exception) {
            LoliUtilsMeteor.LOGGER.error("Screen issue?", exception);
        }

        waitingForClose = false;
    }

    @EventHandler
    public void onPacketSent(PacketEvent.Sent event) {
        if (updateOnBed.get() && event.packet instanceof ServerboundUseItemOnPacket packet) {
            if (mc.level.getBlockState(packet.getHitResult().getBlockPos()).getBlock() instanceof BedBlock) {
                nearPos.set(packet.getHitResult().getBlockPos());
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        assert mc.level != null;

        if (!mc.player.blockPosition().closerThan(nearPos.get(), maxDistance.get())) {
            waitingForClose = false;
            isFindingChest = false;
            baritone.getPathingBehavior().cancelEverything();
            ticks = delay.get();
            return;
        }

        if (isFindingChest && useBaritone.get()) {
            if (mc.player.getEyePosition().closerThan(gotoChest.getCenter(), useDistance.get())) {
                isFindingChest = false;
                baritone.getPathingBehavior().cancelEverything();
                return;
            } else {

                if (!baritone.getPathingBehavior().hasPath()) {
                    BlockPos blockPos = gotoChest.atY(mc.player.getBlockY());

                    for (Direction dir : BlockUtils.HORIZONTALS) {
                        if (!BlockUtils.isNotAir(blockPos.relative(dir))) {
                            blockPos = blockPos.relative(dir);
                            break;
                        }
                    }

                    baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(blockPos, 2));
                }
                return;
            }
        }

        if (ticks > 0) {
            ticks--;
            return;
        }

        if (mc.screen == null && !InventoryUtils.isInventoryFull()) {
            List<int[]> blockPositions = new ArrayList<>();

            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for (int i = -yRange.get(); i <= yRange.get(); i++) {
                blockPositions.addAll(BlockUtils.findNearBlocksByRadius(mc.player.blockPosition().offset(0, i, 0).mutable(), range.get(), (pos) -> {
                    blockPos.set(pos[0], pos[1], pos[2]);
                    return isDoubleChest(blockPos) && BlockUtils.isExposedToAir(blockPos);
                }));
            }

            // Remove known empty chests!
            blockPositions.removeIf(ints -> {
                boolean isEmpty = false;

                for (int[] block : knownEmptyChests) {
                    if (block[0] == ints[0] && block[1] == ints[1] && block[2] == ints[2]) {
                        isEmpty = true;
                        break;
                    }
                }

                return isEmpty;
            });

            blockPositions.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

            if (!blockPositions.isEmpty()) {
                int[] chestPos = blockPositions.getFirst();
                blockPos.set(chestPos[0], chestPos[1], chestPos[2]);

                if (!blockPos.closerThan(nearPos.get(), maxDistance.get())) {
                    waitingForClose = false;
                    ticks = delay.get();
                    return;
                }

                if (!mc.player.getEyePosition().closerThan(blockPos.getCenter(), useDistance.get())) {
                    if (useBaritone.get()) {
                        isFindingChest = true;
                        gotoChest = blockPos.immutable();
                    }

                    return;
                }

                PacketUtils.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(
                        Vec3.atCenterOf(blockPos),
                        Direction.UP,
                        blockPos,
                        false
                ), 0));
                lastClickedContainer = blockPos.immutable();
                waitingForClose = true;
                ticks = delay.get();
            }
        }
    }

    @EventHandler
    public void render3DEvent(Render3DEvent event) {
        assert mc.player != null;
        if (!rendering.get()) {
            return;
        }

        BlockPos.MutableBlockPos mutableRenderPos = new BlockPos.MutableBlockPos();

        if (renderTracers.get()) {
            Vec3 vec3 = lastClickedContainer.getCenter();

            for (int i = -tracerLines.get(); i <= tracerLines.get(); i++) {
                if (i == 0) {
                    event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, vec3.x, vec3.y, vec3.z, tracerColor.get());
                    continue;
                }

                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, vec3.x + (i * 0.002), vec3.y + (i * 0.002), vec3.z + (i * 0.002), tracerColor.get());
            }
        }

        if (renderEmpty.get()) {
            for (int[] posVec : knownEmptyChests) {
                LoliRendering.renderBlock(event.renderer, mutableRenderPos.set(posVec[0], posVec[1], posVec[2]), emptyColor.get(), emptyColor.get().copy().a(Math.round((float) outlineColor.get().a / 3)));
            }
        }

        if (renderOutline.get()) {
            event.renderer.boxLines(mc.player.blockPosition().getX() - range.get(), mc.player.blockPosition().getY() - range.get(), mc.player.blockPosition().getZ() - range.get(), mc.player.blockPosition().getX() + range.get(), mc.player.blockPosition().getY() + range.get(), mc.player.blockPosition().getZ() + range.get(), outlineColor.get(), 0);
            event.renderer.boxSides(mc.player.blockPosition().getX() - range.get(), mc.player.blockPosition().getY() - range.get(), mc.player.blockPosition().getZ() - range.get(), mc.player.blockPosition().getX() + range.get(), mc.player.blockPosition().getY() + range.get(), mc.player.blockPosition().getZ() + range.get(), outlineColor.get().copy().a(Math.round((float) outlineColor.get().a / 3)), 0);

        }
    }

    public boolean isDoubleChest(BlockPos blockPos) {
        return mc.level != null && mc.level.getBlockState(blockPos).getValueOrElse(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE) != ChestType.SINGLE;
    }
}
