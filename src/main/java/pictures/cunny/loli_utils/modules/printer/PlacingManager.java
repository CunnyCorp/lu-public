package pictures.cunny.loli_utils.modules.printer;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.PlaceableBlock;
import pictures.cunny.loli_utils.utility.modules.McDataCache;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;
import pictures.cunny.loli_utils.utility.render.RenderWrap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PlacingManager {
    public static List<int[]> chunkScanningOrder = new ArrayList<>();

    public static void reorderChunks(int range) {
        chunkScanningOrder.clear();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                chunkScanningOrder.add(new int[]{x, z});
            }
        }

        chunkScanningOrder.sort(Comparator.comparingInt(ints -> Math.abs(ints[0]) + Math.abs(ints[1])));
    }

    public static boolean sortPlaceableBlocks() {
        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null || mc.level == null || mc.player == null) {
            return false;
        }

        List<int[]> printerBlocks = new ArrayList<>(PrinterUtils.PRINTER.placeRadius.get() * PrinterUtils.PRINTER.placeRadius.get());

        if (!PrinterUtils.PRINTER.anchor.get()) {
            for (int i = -PrinterUtils.PRINTER.placeRadius.get(); i <= PrinterUtils.PRINTER.placeRadius.get(); i++) {
                if (DataManager.getRenderLayerRange().isPositionWithinRange(mc.player.blockPosition().relative(Direction.Axis.Y, i))) {
                    printerBlocks.addAll(getBlocksForYLevel(worldSchematic, mc.player.getBlockY() + i));
                }
            }
        } else {
            printerBlocks.addAll(getBlocksForYLevel(worldSchematic, PrinterUtils.PRINTER.yLevel.get()));
        }

        PrinterUtils.PRINTER.toSort.clear();

        int printerMaxSorting = 0;

        printerBlocks.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

        int placingLimit = PrinterUtils.PRINTER.placingLimit.get();

        for (int[] posVec : printerBlocks) {
            if (printerMaxSorting > placingLimit) {
                break;
            }

            printerMaxSorting++;
            PrinterUtils.PRINTER.toSort.add(posVec);

        }

        printerBlocks.clear();

        if (PrinterUtils.PRINTER.firstAlgorithm.get() != Printer.SortAlgorithm.Closest) {
            if (PrinterUtils.PRINTER.firstAlgorithm.get().applySecondSorting) {
                if (PrinterUtils.PRINTER.secondAlgorithm.get() != Printer.SortingSecond.None) {
                    PrinterUtils.PRINTER.toSort.sort(PrinterUtils.PRINTER.secondAlgorithm.get().algorithm);
                }
            }
            PrinterUtils.PRINTER.toSort.sort(PrinterUtils.PRINTER.firstAlgorithm.get().algorithm);
        }

        return true;
    }

    private static List<int[]> getBlocksForYLevel(WorldSchematic worldSchematic, int y) {
        if (worldSchematic == null || mc.level == null || mc.player == null) {
            return new ArrayList<>();
        }

        BlockPos.MutableBlockPos srcBlock = new BlockPos.MutableBlockPos(0, 0, 0);

        return BlockUtils.findNearBlocksByRadius(mc.player.blockPosition().mutable().setY(srcBlock.getY() + y),
                PrinterUtils.PRINTER.placeRadius.get(),
                (pos) -> {
                    srcBlock.set(pos[0], pos[1], pos[2]);

                    BlockState blockState = mc.level.getBlockState(srcBlock);

                    BlockState required = worldSchematic.getBlockState(srcBlock);

                    if (mc.player.blockPosition().closerThan(srcBlock, PrinterUtils.PRINTER.placeRadius.get())
                            && blockState.canBeReplaced()
                            && !required.isAir()
                            && blockState.getBlock() != required.getBlock()
                            && (BlockUtils.canPlace(srcBlock, PrinterUtils.PRINTER.placeDistance.get()) || (PrinterUtils.PRINTER.liquidPlace.get() && BlockUtils.canPlace(srcBlock, PrinterUtils.PRINTER.placeDistance.get(), true)))
                            && !mc.player
                            .getBoundingBox()
                            .intersects(Vec3.atLowerCornerOf(srcBlock), Vec3.atLowerCornerOf(srcBlock).add(1, 1, 1))) {

                        return (!PrinterUtils.PRINTER.strictNoColor.get()
                                && PrinterUtils.PRINTER.containedColors.contains(
                                McDataCache.getColor(required.getBlock().asItem())))
                                || (PrinterUtils.PRINTER.strictNoColor.get()
                                && PrinterUtils.PRINTER.containedBlocks.contains(required.getBlock().asItem()));
                    }

                    return false;
                }
        );
    }

    protected static List<int[]> getBlocksForYLevelBasic(WorldSchematic worldSchematic, int y) {
        if (worldSchematic == null || mc.level == null || mc.player == null) {
            return new ArrayList<>();
        }

        BlockPos.MutableBlockPos srcBlock = new BlockPos.MutableBlockPos(0, 0, 0);

        srcBlock.set(mc.player.blockPosition());

        srcBlock.setY(srcBlock.getY() + y);

        return BlockUtils.findNearBlocksByRadius(srcBlock,
                PrinterUtils.PRINTER.placeRadius.get(),
                (pos) -> {
                    srcBlock.set(pos[0], pos[1], pos[2]);

                    BlockState blockState = mc.level.getBlockState(srcBlock);

                    BlockState required = worldSchematic.getBlockState(srcBlock);

                    return mc.player.blockPosition().closerThan(srcBlock, PrinterUtils.PRINTER.placeRadius.get())
                            && blockState.canBeReplaced()
                            && !required.isAir()
                            && blockState.getBlock() != required.getBlock()
                            && (BlockUtils.canPlace(srcBlock, PrinterUtils.PRINTER.placeDistance.get()) || (PrinterUtils.PRINTER.liquidPlace.get() && BlockUtils.canPlace(srcBlock, PrinterUtils.PRINTER.placeDistance.get(), true)))
                            && !mc.player
                            .getBoundingBox()
                            .intersects(new Vec3(srcBlock), new Vec3(srcBlock).add(1, 1, 1));
                }
        );
    }

    public static void tryPlacingBlocks() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null || !sortPlaceableBlocks()) {
            return;
        }

        int placed = 0;

        if (PrinterUtils.PRINTER.placeTimer < PrinterUtils.PRINTER.delay.get()) {
            return;
        }

        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        List<int[]> waterPlaceable = new ArrayList<>();

        List<PlaceableBlock> placeableBlocks = new ArrayList<>();

        for (int[] pos : PrinterUtils.PRINTER.toSort) {
            if (Modules.get().get(AutoEat.class).eating
                    || Modules.get().get(AutoGap.class).isEating()
                    || Modules.get().get(KillAura.class).getTarget() != null) {
                PrinterUtils.PRINTER.interestPoint = 8;
                break;
            }

            if (placed >= PrinterUtils.PRINTER.blocksPerTick.get()
                    || PrinterUtils.PRINTER.blocksPlacedThisSec >= PrinterUtils.PRINTER.blocksPerSec.get()) {
                PrinterUtils.PRINTER.interestPoint = 9;
                break;
            }

            blockPos.set(pos[0], pos[1], pos[2]);

            BlockState state = worldSchematic.getBlockState(blockPos);
            Item item = state.getBlock().asItem();

            FindItemResult itemResult =
                    InvUtils.find(
                            (stack) ->
                                    PrinterUtils.PRINTER.strictNoColor.get()
                                            ? stack.getItem() == item
                                            : McDataCache.getColor(stack) == McDataCache.getColor(item)
                                            && PrinterUtils.PRINTER.blockExclusion.get().stream()
                                            .noneMatch((block -> stack.getItem() == block.asItem())));

            if (!itemResult.found()) continue;

            InteractionHand hand = InteractionHand.MAIN_HAND;

            if (itemResult.isOffhand()) hand = InteractionHand.OFF_HAND;

            if (PrinterUtils.PRINTER.swapTimer > 0) {
                PrinterUtils.PRINTER.interestPoint = 10;
                break;
            }

            if ((!PrinterUtils.PRINTER.strictNoColor.get()
                    && McDataCache.getColor(mc.player.getMainHandItem())
                    != McDataCache.getColor(item)
                    || (PrinterUtils.PRINTER.strictNoColor.get()
                    && mc.player.getMainHandItem().getItem() != item))
                    && hand != InteractionHand.OFF_HAND) {
                PrinterUtils.PRINTER.swapTimer = PrinterUtils.PRINTER.swapDelay.get();
                if (itemResult.isHotbar()) {
                    InventoryUtils.swapSlot(itemResult.slot());
                    InventoryUtils.pickup(itemResult.slot());
                    InventoryUtils.placeItem(itemResult.slot());
                } else {
                    InventoryUtils.swapSlot(PrinterUtils.PRINTER.dedicatedSlot.get());
                    InventoryUtils.swapToHotbar(itemResult.slot(), PrinterUtils.PRINTER.dedicatedSlot.get());
                    InventoryUtils.pickup(PrinterUtils.PRINTER.dedicatedSlot.get());
                    InventoryUtils.placeItem(PrinterUtils.PRINTER.dedicatedSlot.get());
                }

                break;
            }

            if (BlockUtils.canPlace(blockPos, PrinterUtils.PRINTER.placeDistance.get())) {
                placeableBlocks.add(new PlaceableBlock(hand, itemResult, blockPos.immutable()));
                PrinterUtils.PRINTER.placeTimer = 0;
                placed++;
                PrinterUtils.PRINTER.blocksPlacedThisSec++;
            } else if (PrinterUtils.PRINTER.liquidPlace.get() && BlockUtils.canPlace(blockPos, PrinterUtils.PRINTER.placeDistance.get(), true)) {
                waterPlaceable.add(pos);
            } else {
                LoliUtilsMeteor.LOGGER.info("Failed liquid place check & Air: {} - {}", blockPos.toShortString(), BlockUtils.shouldLiquidPlace(blockPos));
            }
        }

        for (PlaceableBlock placeable : placeableBlocks) {
            if (PrinterUtils.placeBlock(placeable.hand(), placeable.itemResult(), placeable.blockPos())) {
                PrinterUtils.PRINTER.placeFading.removeIf((wrap) -> wrap.blockPos().equals(placeable.blockPos()));
                PrinterUtils.PRINTER.placeFading.add(new RenderWrap(blockPos.immutable(), 20, PrinterUtils.PRINTER.fadeTime.get(), 0, PrinterUtils.PRINTER.preserveColor.get() ? PrinterUtils.PRINTER.getNextPlaceColor(placeable.blockPos()).copy() : PrinterUtils.PRINTER.getNextPlaceColor(placeable.blockPos())));

                Printer.lastPlacedBlock.set(placeable.blockPos());
            }
        }

        for (int[] pos : waterPlaceable) {
            if (PrinterUtils.PRINTER.lastLiquidPlace > 0) {
                break;
            }

            if (PrinterUtils.PRINTER.placeTimer < PrinterUtils.PRINTER.delay.get()
                    || placed >= PrinterUtils.PRINTER.blocksPerTick.get()
                    || PrinterUtils.PRINTER.blocksPlacedThisSec >= PrinterUtils.PRINTER.blocksPerSec.get()) {
                PrinterUtils.PRINTER.interestPoint = 9;
                break;
            }

            blockPos.set(pos[0], pos[1], pos[2]);

            BlockState state = worldSchematic.getBlockState(blockPos);
            Item item = state.getBlock().asItem();

            FindItemResult itemResult =
                    InvUtils.find(
                            (stack) ->
                                    PrinterUtils.PRINTER.strictNoColor.get()
                                            ? stack.getItem() == item
                                            : McDataCache.getColor(stack) == McDataCache.getColor(item)
                                            && PrinterUtils.PRINTER.blockExclusion.get().stream()
                                            .noneMatch((block -> stack.getItem() == block.asItem())));

            if (!itemResult.found()) continue;

            InteractionHand hand = InteractionHand.MAIN_HAND;

            if (itemResult.isOffhand()) hand = InteractionHand.OFF_HAND;

            if (PrinterUtils.PRINTER.swapTimer > 0) {
                PrinterUtils.PRINTER.interestPoint = 10;
                break;
            }

            if ((!PrinterUtils.PRINTER.strictNoColor.get()
                    && McDataCache.getColor(mc.player.getMainHandItem())
                    != McDataCache.getColor(item)
                    || (PrinterUtils.PRINTER.strictNoColor.get()
                    && mc.player.getMainHandItem().getItem() != item))
                    && hand != InteractionHand.OFF_HAND) {
                PrinterUtils.PRINTER.swapTimer = PrinterUtils.PRINTER.swapDelay.get();
                if (itemResult.isHotbar()) {
                    InventoryUtils.swapSlot(itemResult.slot());
                } else {
                    int emptySlot = InventoryUtils.findEmptySlotInHotbar(7);
                    InventoryUtils.swapSlot(emptySlot);
                    InventoryUtils.swapToHotbar(itemResult.slot(), emptySlot);
                }

                break;
            }

            PrinterUtils.PRINTER.lastLiquidPlace = PrinterUtils.PRINTER.liquidPlaceTimeout.get();

            BlockPos lowerPos = blockPos.relative(Direction.DOWN);
            LoliUtilsMeteor.LOGGER.info("Trying to liquid place on {}", lowerPos.toShortString());

            PacketUtils.send(new ServerboundUseItemOnPacket(hand, new BlockHitResult(BlockUtils.clickOffset(lowerPos, Direction.UP), Direction.UP, lowerPos, false), 0));
            //mc.gameMode.useItemOn(mc.player, hand, new BlockHitResult(BlockUtils.clickOffset(blockPos, Direction.UP), Direction.UP, lowerPos, false));
            if (!PrinterUtils.PRINTER.noSwing.get()) {
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
            PrinterUtils.placeBlock(hand, itemResult, blockPos);

            Printer.lastPlacedBlock.set(blockPos);

            PrinterUtils.PRINTER.placeFading.removeIf((wrap) -> wrap.blockPos().equals(blockPos));
            PrinterUtils.PRINTER.placeFading.add(new RenderWrap(blockPos.immutable(), 20, PrinterUtils.PRINTER.fadeTime.get(), 0, PrinterUtils.PRINTER.preserveColor.get() ? PrinterUtils.PRINTER.getNextPlaceColor(blockPos).copy() : PrinterUtils.PRINTER.getNextPlaceColor(blockPos)));

            PrinterUtils.PRINTER.placeTimer = 0;
            placed++;
            PrinterUtils.PRINTER.blocksPlacedThisSec++;
        }

        waterPlaceable.clear();
    }
}
