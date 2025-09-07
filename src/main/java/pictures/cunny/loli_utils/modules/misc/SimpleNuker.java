package pictures.cunny.loli_utils.modules.misc;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.render.LoliRendering;
import pictures.cunny.loli_utils.utility.render.RenderWrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleNuker extends Module {
    public final BlockPos.MutableBlockPos lastTargetedBlock = new BlockPos.MutableBlockPos();
    public final List<SkippedBlock> skippedBlocks = new ArrayList<>();
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> radius =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("radius")
                            .description("How far to check for breakable blocks.")
                            .sliderRange(1, 5)
                            .defaultValue(3)
                            .build());
    public final Setting<Integer> negYDepth =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("neg-y-depth")
                            .description("How far down to look for blocks to break.")
                            .sliderRange(0, 5)
                            .defaultValue(0)
                            .build());
    public final Setting<Integer> posYDepth =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("pos-y-depth")
                            .description("How up down to look for blocks to break.")
                            .sliderRange(0, 5)
                            .defaultValue(2)
                            .build());
    public final Setting<Double> reach =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("reach")
                            .description("The max distance to mine blocks.")
                            .defaultValue(3.75)
                            .sliderRange(3.2, 5.0)
                            .build());
    public final Setting<FilterMode> filterMode =
            sgDefault.add(
                    new EnumSetting.Builder<FilterMode>()
                            .name("filter")
                            .description("The mode for filtering blocks.")
                            .defaultValue(FilterMode.Whitelist)
                            .build());
    public final Setting<List<Block>> whitelist =
            sgDefault.add(
                    new BlockListSetting.Builder()
                            .name("whitelist")
                            .description("Only destroys blocks in this list.")
                            .visible(() -> filterMode.get() == FilterMode.Whitelist)
                            .build());
    public final Setting<List<Block>> blacklist =
            sgDefault.add(
                    new BlockListSetting.Builder()
                            .name("blacklist")
                            .description("Only destroys blocks NOT in this list.")
                            .visible(() -> filterMode.get() == FilterMode.Blacklist)
                            .build());
    public final Setting<Boolean> skipAir =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("skip-air")
                            .description("Skip blocks that would be air in the schematic, disable to clear out build areas!")
                            .defaultValue(true)
                            .visible(() -> filterMode.get() == FilterMode.Litematica)
                            .build());
    public final Setting<LockMode> lockMode =
            sgDefault.add(
                    new EnumSetting.Builder<LockMode>()
                            .name("lock-mode")
                            .description("How to lock destroying blocks.")
                            .defaultValue(LockMode.None)
                            .build());
    public final Setting<Integer> yLevel =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("y-level")
                            .description("The set Y level for destroying blocks.")
                            .sliderRange(-64, 320)
                            .defaultValue(63)
                            .visible(() -> lockMode.get() != LockMode.None && lockMode.get() != LockMode.CurrentChunk)
                            .build());
    public final Setting<Boolean> noDestroyDelay =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("no-destroy-delay")
                            .description("Removes the destroy delay.")
                            .defaultValue(true)
                            .build());
    public final Setting<Double> fastDestroy =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("fast-destroy")
                            .description("How fast to accelerate destroying blocks.")
                            .defaultValue(0.1)
                            .sliderRange(0.0, 1.0)
                            .build());
    public final Setting<Boolean> skipBroken =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("skip-broken")
                            .description("Attempts to skip breaking when one broken.")
                            .defaultValue(false)
                            .build());
    public final Setting<Integer> skipTimeout =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("skip-timeout")
                            .description("How long before removing a block from the skipped list.")
                            .sliderRange(1, 20)
                            .defaultValue(5)
                            .build());
    private final SettingGroup sgRender = settings.createGroup("Render");
    public final Setting<Boolean> rendering =
            sgRender.add(
                    new BoolSetting.Builder()
                            .name("rendering")
                            .description("Render a box when placing blocks.")
                            .defaultValue(true)
                            .build());
    private final Setting<SettingColor> renderColor =
            sgRender.add(
                    new ColorSetting.Builder()
                            .name("color")
                            .defaultValue(new Color(195, 126, 234, 111))
                            .build());
    private final Setting<Integer> fadeTime =
            sgRender.add(
                    new IntSetting.Builder()
                            .name("fade-time")
                            .description("How fast to fade out.")
                            .defaultValue(160)
                            .sliderRange(20, 1000)
                            .build());
    private final List<RenderWrap> renderWrapping = new ArrayList<>();

    public SimpleNuker() {
        super(LoliUtilsMeteor.CATEGORY, "simple-nuker", "A simplified version of nuker with less frickery.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        assert mc.gameMode != null;

        LoliRendering.tickFadeTime(renderWrapping);

        List<SkippedBlock> removedBlocks = new ArrayList<>();

        for (SkippedBlock skippedBlock : skippedBlocks) {
            if (System.currentTimeMillis() - skippedBlock.timestamp() >= skipTimeout.get()) {
                removedBlocks.add(skippedBlock);
            }
        }

        skippedBlocks.removeAll(removedBlocks);

        BlockPos nextBlock = getNextBlock();

        if (nextBlock != null) {
            if (this.noDestroyDelay.get()) {
                mc.gameMode.destroyDelay = 0;
            }

            mc.gameMode.destroyProgress += fastDestroy.get();

            if (lastTargetedBlock.equals(nextBlock)) {
                mc.gameMode.continueDestroyBlock(nextBlock, Direction.UP);
            } else {
                mc.gameMode.startDestroyBlock(nextBlock, Direction.UP);
                if (rendering.get()) {
                    renderWrapping.add(new RenderWrap(nextBlock, fadeTime.get(), 0, 0, renderColor.get()));
                }
            }

            lastTargetedBlock.set(nextBlock);
        }
    }


    @EventHandler
    public void render3DEvent(Render3DEvent event) {
        assert mc.player != null;

        if (rendering.get() && !renderWrapping.isEmpty()) {
            for (RenderWrap wrap : renderWrapping) {
                LoliRendering.renderRetractingCube(event.renderer, wrap);
            }
        }
    }

    private BlockPos getNextBlock() {
        assert mc.player != null;
        assert mc.level != null;

        List<int[]> blockPositions = new ArrayList<>();

        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        AtomicReference<BlockPos> matchedBlock = new AtomicReference<>();

        for (int i = -negYDepth.get(); i <= posYDepth.get(); i++) {
            blockPositions.addAll(BlockUtils.findNearBlocksByRadius(mc.player.blockPosition().offset(0, i, 0).mutable(), radius.get(), (pos) -> {
                blockPos.set(pos[0], pos[1], pos[2]);

                Block block = mc.level.getBlockState(blockPos).getBlock();

                if (filterMode.get() == FilterMode.Blacklist) {
                    if (blacklist.get().contains(block)) {
                        return false;
                    }
                } else if (filterMode.get() == FilterMode.Whitelist) {
                    if (!whitelist.get().contains(block)) {
                        return false;
                    }
                }

                if (lockMode.get() != LockMode.None) {
                    if (failsLockCheck(blockPos)) {
                        return false;
                    }
                }

                if (blockPos.equals(lastTargetedBlock)) {
                    matchedBlock.set(blockPos.immutable());
                }

                return canBreakBlock(blockPos);
            }));
        }

        if (matchedBlock.get() != null) {
            if (canBreakBlock(matchedBlock.get())) {
                return matchedBlock.get();
            }
        }

        if (blockPositions.isEmpty()) {
            return null;
        }

        blockPositions.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

        int[] posVec = blockPositions.getFirst();

        return new BlockPos(posVec[0], posVec[1], posVec[2]);
    }

    private boolean canBreakBlock(BlockPos blockPos) {
        assert mc.player != null;
        assert mc.level != null;

        if (!BlockUtils.isNotAir(blockPos)) {
            return false;
        }

        if (!mc.player.getEyePosition().closerThan(blockPos.getCenter(), reach.get())) {
            return false;
        }

        if (mc.level.getBlockState(blockPos).getDestroySpeed(mc.level, blockPos) == -1.0f) {
            return false;
        }

        if (BlockUtils.isLiquid(blockPos)) {
            return false;
        }

        if (filterMode.get() == FilterMode.Litematica) {
            WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

            if (worldSchematic == null) {
                return false;
            }

            if (skipAir.get() && worldSchematic.getBlockState(blockPos).isAir()) {
                return false;
            }

            if (worldSchematic.getBlockState(blockPos).getBlock() == mc.level.getBlockState(blockPos).getBlock()) {
                return false;
            }
        }

        return skippedBlocks.stream().noneMatch(skippedBlock -> skippedBlock.blockPos().equals(blockPos));
    }

    public boolean failsLockCheck(BlockPos.MutableBlockPos blockPos) {
        switch (lockMode.get()) {
            case SetY -> {
                return blockPos.getY() == yLevel.get();
            }

            case AtOrAbove -> {
                return blockPos.getY() >= yLevel.get();
            }

            case AtOrBelow -> {
                return blockPos.getY() <= yLevel.get();
            }

            case CurrentChunk -> {
                ChunkPos chunkPos = new ChunkPos(blockPos);

                return chunkPos.equals(mc.player.chunkPosition());
            }

            default -> {
                return true;
            }
        }
    }

    public enum FilterMode {
        Whitelist,
        Blacklist,
        Litematica,
        None
    }

    public enum LockMode {
        CurrentChunk,
        SetY,
        AtOrAbove,
        AtOrBelow,
        None
    }

    public record SkippedBlock(long timestamp, BlockPos blockPos) {

    }
}
