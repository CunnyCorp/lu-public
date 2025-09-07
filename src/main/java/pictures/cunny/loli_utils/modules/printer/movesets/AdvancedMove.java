package pictures.cunny.loli_utils.modules.printer.movesets;

import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import pictures.cunny.loli_utils.modules.printer.PrinterUtils;
import pictures.cunny.loli_utils.utility.BlockUtils;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class AdvancedMove extends DefaultMove {
    private int holdBackTicks = 0;
    private boolean holdForEfly = false;
    private BlockPos nBlockPos = BlockPos.ZERO;

    @Override
    public MoveSets type() {
        return MoveSets.ADVANCED;
    }

    @Override
    public void tick(BlockPos pos) {
        assert mc.player != null;
        boolean moveForward = true;
        boolean moveBack = false;
        boolean jump = false;
        boolean sprinting = false;
        boolean holdFurtherLogic = false;
        float yaw = (float) Rotations.getYaw(pos);
        float pitch = 0;

        try {
            pitch = PrinterUtils.PRINTER.pitchMode.get().callable.call();
        } catch (Exception ignored) {
        }

        if (PrinterUtils.PRINTER.useElytra.get() && (Utils.distance(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                mc.player.getX(),
                mc.player.getY(),
                mc.player.getZ()
        ) > PrinterUtils.PRINTER.distanceBeforeFlight.get() || holdForEfly)) {

            if (!holdForEfly) {
                List<int[]> blockPosList = PrinterUtils.findSuitableLandListDontCareDidntAskPlusRatio(pos, PrinterUtils.PRINTER.landScanRadius.get());

                if (!blockPosList.isEmpty()) {
                    blockPosList.sort(Comparator.comparingDouble(
                            value ->
                                    mc.player != null
                                            ? Utils.squaredDistance(
                                            pos.getX(),
                                            700,
                                            pos.getZ(),
                                            value[0],
                                            700,
                                            value[1])
                                            : 0));
                    int[] firstBlock = blockPosList.getFirst();
                    holdForEfly = true;
                    nBlockPos = new BlockPos.MutableBlockPos(firstBlock[0], pos.getY(), firstBlock[1]);
                    MoveSets.CONST_EFLY.movement.tick(nBlockPos);
                    return;
                }
            } else {
                if (Utils.distance(nBlockPos.getX(), nBlockPos.getY(), nBlockPos.getZ(),
                        mc.player.getBlockX(), nBlockPos.getY(), mc.player.getBlockZ()) <= 0.35) {
                    if (mc.player.onGround()) {
                        mc.player.setDeltaMovement(0, 0, 0);
                        if (mc.player.isFallFlying()) {
                            mc.player.stopFallFlying();
                        }

                        MoveSets.CONST_EFLY.movement.cancel(nBlockPos);
                        holdForEfly = false;
                    } else if (mc.player.horizontalCollision) {
                        mc.player.setDeltaMovement(0, 0, 0);
                        if (mc.player.isFallFlying()) {
                            mc.player.stopFallFlying();
                        }

                        MoveSets.CONST_EFLY.movement.cancel(nBlockPos);
                        holdForEfly = false;
                    } else {
                        MoveSets.CONST_EFLY.movement.tick(nBlockPos);
                    }
                } else {
                    MoveSets.CONST_EFLY.movement.tick(nBlockPos);
                }

                return;
            }
        }

        if (PrinterUtils.PRINTER.alwaysSprint.get()) {
            sprinting = true;
        }

        if (holdBackTicks > 0) {
            holdBackTicks--;
            moveBack = true;
            moveForward = false;
            holdFurtherLogic = true;
        }

        if (!holdFurtherLogic) {
            if (mc.player.onGround()) {
                int xOffset = mc.player.getMotionDirection().getStepX();
                int zOffset = mc.player.getMotionDirection().getStepZ();
                int xOffsetOpp = mc.player.getMotionDirection().getOpposite().getStepX();
                int zOffsetOpp = mc.player.getMotionDirection().getOpposite().getStepZ();

                BlockPos.MutableBlockPos fwBlock = mc.player.getOnPos().mutable();

                boolean wouldNotBeSafe = false;

                for (int x0 = 0; x0 <= 1; x0++) {
                    if (xOffset != 0) {
                        fwBlock.setX(fwBlock.getX() + (xOffset * x0));
                    } else {
                        fwBlock.setX(fwBlock.getX());
                    }
                    for (int z0 = 0; z0 <= 1; z0++) {
                        if (zOffset != 0) {
                            fwBlock.setZ(fwBlock.getZ() + (zOffset * z0));
                        } else {
                            fwBlock.setZ(fwBlock.getZ());
                        }

                        if (BlockUtils.isReplaceable(fwBlock)) {
                            wouldNotBeSafe = true;
                            break;
                        }
                    }
                }

                fwBlock.setX(fwBlock.getX() + xOffset);
                fwBlock.setZ(fwBlock.getZ() + zOffset);

                BlockPos.MutableBlockPos backBlock = mc.player.blockPosition().mutable();

                backBlock.setX(backBlock.getX() + xOffsetOpp);
                backBlock.setZ(backBlock.getZ() + zOffsetOpp);

                BlockPos.MutableBlockPos ffBlock = fwBlock.mutable();

                ffBlock.setX(ffBlock.getX() + xOffset);
                ffBlock.setY(PrinterUtils.PRINTER.yLevel.get());
                ffBlock.setX(ffBlock.getZ() + zOffset);


                // LoliUtils.LOGGER.info("Block Pos: {}, FW Block: {}, X Offset: {}, Z Offset: {}", mc.player.getBlockPos().toShortString(), fwBlock.toShortString(), xOffset, zOffset);

                assert mc.level != null;
                BlockState fwBlockState = mc.level.getBlockState(fwBlock);

                if (fwBlockState.getInteractionShape(mc.level, fwBlock).max(Direction.Axis.Y) == 0.5) {
                    yaw = Mth.wrapDegrees((float) (yaw + (Rotations.getYaw(fwBlock.getBottomCenter()) * 0.3)));
                } else {
                    if (wouldNotBeSafe) {
                        fwBlock.setY(PrinterUtils.PRINTER.yLevel.get());
                        if (PrinterUtils.PRINTER.safeWalk.get()) {
                            moveBack = true;
                            moveForward = false;
                            holdBackTicks = PrinterUtils.PRINTER.backHoldTime.get();
                        }
                    }
                }
            }
        }

        mc.options.keyUp.setDown(moveForward);
        mc.options.keyDown.setDown(moveBack);
        mc.player.setSprinting(sprinting);
        mc.options.keyJump.setDown(jump);
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }

    @Override
    public void cancel(BlockPos pos) {
        if (mc.player == null) {
            return;
        }

        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        holdForEfly = false;
    }

    @SuppressWarnings("unused")
    public enum PitchMode {
        NONE(() -> mc.player != null ? mc.player.getXRot() : 0),
        DOWN(() -> 90f),
        UP(() -> -90f);

        public final Callable<Float> callable;

        PitchMode(Callable<Float> callable) {
            this.callable = callable;
        }

    }
}
