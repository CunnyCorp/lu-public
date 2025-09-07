package pictures.cunny.loli_utils.modules.printer;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.ArrayList;
import java.util.List;

public class BuildPrinter extends Module {

    private final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
    private final double[] rotationLock = new double[]{0, 0};
    private final BlockPos.MutableBlockPos lockedBlockPos = new BlockPos.MutableBlockPos();
    private final int rotationLockTime = 0;

    public BuildPrinter(Category category, String name, String desc) {
        super(LoliUtilsMeteor.CATEGORY, "build-printer", "Automatically place blocks in a schematic, for building.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        WorldSchematic worldSchematic = SchematicWorldHandler.getSchematicWorld();

        if (worldSchematic == null) {
            return;
        }

        if (rotationLockTime > 0) {
            PacketUtils.rotate((float) rotationLock[0], (float) rotationLock[1], true);
            return;
        }

        if (lockedBlockPos.getY() != -999) {
            return;
        }

        List<int[]> blockPositions = new ArrayList<>();

        for (int i = -3; i < 3; i++) {
            blockPositions.addAll(PlacingManager.getBlocksForYLevelBasic(worldSchematic, i));
        }

        blockPositions.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

        for (int[] pos : blockPositions) {
            mutableBlockPos.set(pos[0], pos[1], pos[2]);

            BlockState state = worldSchematic.getBlockState(mutableBlockPos);

            if (state.getBlock() instanceof StairBlock) {
                Direction facing = state.getValue(StairBlock.FACING);

                mutableBlockPos.set(pos[0] + facing.getStepZ(), pos[1] + facing.getStepY(), pos[2] + facing.getStepZ());

                rotationLock[0] = Rotations.getPitch(mutableBlockPos);
                rotationLock[1] = Rotations.getYaw(mutableBlockPos);

                lockedBlockPos.set(pos[0], pos[1], pos[2]);
            }
        }

    }
}
