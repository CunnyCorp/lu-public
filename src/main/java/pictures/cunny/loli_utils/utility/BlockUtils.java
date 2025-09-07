package pictures.cunny.loli_utils.utility;

import baritone.api.BaritoneAPI;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.modules.printer.PrinterUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.*;
import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockUtils {
    public static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST};
    public static final Comparator<int[]> CLOSEST_XZ_COMPARATOR =
            Comparator.comparingDouble(
                    value -> {
                        if (value.length == 2) {
                            return MeteorClient.mc.player != null
                                    ? Utils.squaredDistance(
                                    MeteorClient.mc.player.getX(),
                                    700,
                                    MeteorClient.mc.player.getZ(),
                                    value[0],
                                    700,
                                    value[1])
                                    : 0;
                        } else if (value.length == 3) {
                            return MeteorClient.mc.player != null
                                    ? Utils.squaredDistance(
                                    MeteorClient.mc.player.getX(),
                                    700,
                                    MeteorClient.mc.player.getZ(),
                                    value[0],
                                    700,
                                    value[2])
                                    : 0;
                        }

                        return 0;
                    });
    private static final double MAGIC_PLACE_OFFSET = 0.0154;

    public static Direction[] getDirections() {
        return Direction.values();
    }

    public static List<EndCrystal> imposedCrystals(BlockPos pos) {
        assert mc.player != null;
        return mc.player.level().getEntities(EntityType.END_CRYSTAL, new AABB(pos.offset(3, 3, 3).getCenter(), pos.offset(-3, -3, -3).getCenter()), entity -> {
            return entity.isColliding(pos, Blocks.BEDROCK.defaultBlockState()) || isOnSurround(entity);
        });
    }

    public static boolean canExplode(BlockPos pos) {
        if (isReplaceable(pos)) return true;
        assert mc.player != null;
        return mc.player.level().getBlockState(pos).getBlock().getExplosionResistance() < 100;
    }

    public static boolean isOnSurround(EndCrystal crystal) {
        for (Direction direction : HORIZONTALS) {
            assert mc.player != null;
            BlockPos offset = mc.player.blockPosition().relative(direction);
            if (crystal.blockPosition().equals(offset.relative(Direction.UP))
                    || crystal.blockPosition().equals(offset.relative(Direction.DOWN))) return true;
        }
        return false;
    }

    public static boolean isReplaceable(BlockPos pos) {
        return mc.player != null && (mc.player.level().getBlockState(pos).isAir()
                || mc.player.level().getBlockState(pos).canBeReplaced()
                || isLiquid(pos));
    }

    public static boolean isLiquid(BlockPos pos) {
        return mc.player != null
                && mc.player.level().getBlockState(pos).getBlock() instanceof LiquidBlock;
    }

    public static boolean isNotAir(BlockPos pos) {
        return mc.player == null || !mc.player.level().getBlockState(pos).isAir();
    }

    public static boolean canPlace(BlockPos pos) {
        return canPlace(pos, 3.75, false);
    }

    public static boolean canPlace(BlockPos pos, double dist) {
        return canPlace(pos, dist, false);
    }

    public static boolean canPlace(BlockPos pos, double dist, boolean liquidPlace) {
        assert mc.player != null;

        List<Entity> entities =
                mc.player
                        .level()
                        .getEntities(null, new AABB(pos.getCenter().add(3, 3, 3), pos.getCenter().add(-3, -3, -3))).stream().filter(
                                entity -> {
                                    if (EntityUtils.canPlaceIn(entity)) {
                                        return false;
                                    }

                                    return entity.isColliding(pos, Blocks.BEDROCK.defaultBlockState());
                                }).toList();

        return isReplaceable(pos) && entities.isEmpty() && mc.player.getEyePosition().closerThan(getSafeHitResult(pos).getLocation(), dist) && (liquidPlace ? shouldLiquidPlace(pos) : !shouldAirPlace(pos));
    }

    public static boolean hasEntitiesInside(BlockPos pos) {
        assert mc.player != null;
        List<Entity> entities =
                mc.player
                        .level()
                        .getEntities(null, new AABB(pos.getCenter().add(3, 3, 3), pos.getCenter().add(-3, -3, -3))).stream().filter(
                                entity -> {
                                    if (EntityUtils.canPlaceIn(entity)) {
                                        return false;
                                    }

                                    return entity.isColliding(pos, Blocks.BEDROCK.defaultBlockState());
                                }).toList();
        return !entities.isEmpty();
    }

    public static boolean isPlayerInside(BlockPos pos) {
        assert mc.player != null;
        List<Entity> entities =
                mc.player
                        .level()
                        .getEntities(null, new AABB(pos.getCenter().add(3, 3, 3), pos.getCenter().add(-3, -3, -3))).stream().filter(
                                entity -> {
                                    if (entity == mc.player) {
                                        return entity.isColliding(pos, Blocks.BEDROCK.defaultBlockState());
                                    }

                                    return false;
                                }).toList();
        return !entities.isEmpty();
    }

    public static Direction getPlaceDirection(BlockPos pos) {
        for (Direction direction : getDirections()) {
            if (isReplaceable(pos.relative(direction))) return direction;
        }
        return Direction.UP;
    }

    public static boolean shouldAirPlace(BlockPos pos) {
        for (Direction direction : getDirections()) {
            if (!BlockUtils.isReplaceable(pos.relative(direction))) return false;
        }
        return true;
    }

    public static boolean isExposedToAir(BlockPos pos) {
        for (Direction direction : HORIZONTALS) {
            if (!BlockUtils.isNotAir(pos.relative(direction))) return true;
        }
        return false;
    }


    public static boolean shouldLiquidPlace(BlockPos pos) {
        return BlockUtils.isLiquid(pos.relative(Direction.DOWN));
    }

    public static Map.Entry<Float, Float> getRotation(boolean raytrace, BlockPos pos) {
        assert mc.player != null;

        if (raytrace) {
            if (canRaycast(pos, (float) Rotations.getYaw(pos), (float) Rotations.getPitch(pos))) {
                return Map.entry((float) Rotations.getYaw(pos), (float) Rotations.getPitch(pos));
            }

            if (PrinterUtils.PRINTER.experimentalRotations.get()) {
                double yaw = Rotations.getYaw(pos);
                double pitch = Rotations.getPitch(pos);

                for (float scanYaw = -3; scanYaw <= 3; scanYaw += 1f) {
                    for (float scanPitch = -3; scanPitch <= 3; scanPitch += 1f) {
                        float nextYaw = (float) Mth.wrapDegrees(scanYaw + yaw);
                        float nextPitch = (float) Mth.wrapDegrees(scanPitch + pitch);
                        if (canRaycast(pos, nextPitch, nextYaw)) {
                            return Map.entry(nextYaw, nextPitch);
                        }
                    }
                }
            }

            Optional<Rotation> rotation =
                    RotationUtils.reachable(
                            BaritoneAPI.getProvider().getBaritoneForPlayer(mc.player).getPlayerContext(),
                            pos,
                            4.5);

            if (rotation.isPresent()) {
                if (canRaycast(pos, rotation.get().getPitch(), rotation.get().getYaw())) {
                    return Map.entry(rotation.get().getYaw(), rotation.get().getPitch());
                }
            }

            for (Direction direction : Direction.values()) {
                Vec3 vec3d =
                        new Vec3(
                                (double) pos.getX() + ((double) direction.getOpposite().getStepX() * 0.5),
                                (double) pos.getY() + ((double) direction.getOpposite().getStepY() * 0.5),
                                (double) pos.getZ() + ((double) direction.getOpposite().getStepZ() * 0.5));
                double yaw = Rotations.getYaw(vec3d), pitch = Rotations.getPitch(vec3d);

                rotation =
                        RotationUtils.reachable(
                                BaritoneAPI.getProvider().getBaritoneForPlayer(mc.player).getPlayerContext(),
                                pos.relative(direction),
                                4.5);

                if (rotation.isPresent()) {
                    if (canRaycast(pos, rotation.get().getPitch(), rotation.get().getYaw())) {
                        return Map.entry(rotation.get().getYaw(), rotation.get().getPitch());
                    }
                }

                if (canRaycast(pos, (float) pitch, (float) yaw)) {
                    return Map.entry((float) yaw, (float) pitch);
                } else {
                    if (PrinterUtils.PRINTER.experimentalRotations.get()) {
                        for (float scanYaw = -3; scanYaw <= 3; scanYaw += 1f) {
                            for (float scanPitch = -3; scanPitch <= 3; scanPitch += 1f) {
                                float nextYaw = (float) Mth.wrapDegrees(scanYaw + yaw);
                                float nextPitch = (float) Mth.wrapDegrees(scanPitch + pitch);
                                if (canRaycast(pos, nextPitch, nextYaw)) {
                                    return Map.entry(nextYaw, nextPitch);
                                }
                            }
                        }
                    }

                    vec3d =
                            new Vec3(
                                    (double) pos.getX() + direction.getOpposite().getStepX(),
                                    (double) pos.getY() + direction.getOpposite().getStepY(),
                                    (double) pos.getZ() + direction.getOpposite().getStepZ());
                    yaw = Rotations.getYaw(vec3d);
                    pitch = Rotations.getPitch(vec3d);

                    rotation =
                            RotationUtils.reachable(
                                    BaritoneAPI.getProvider().getBaritoneForPlayer(mc.player).getPlayerContext(),
                                    pos.relative(direction),
                                    4.5);

                    if (rotation.isPresent()) {
                        if (canRaycast(pos, rotation.get().getPitch(), rotation.get().getYaw())) {
                            return Map.entry(rotation.get().getYaw(), rotation.get().getPitch());
                        }
                    }

                    if (canRaycast(pos, (float) pitch, (float) yaw)) {
                        return Map.entry((float) yaw, (float) pitch);
                    }

                    if (PrinterUtils.PRINTER.experimentalRotations.get()) {
                        for (float scanYaw = -3; scanYaw <= 3; scanYaw += 1f) {
                            for (float scanPitch = -3; scanPitch <= 3; scanPitch += 1f) {
                                float nextYaw = (float) Mth.wrapDegrees(scanYaw + yaw);
                                float nextPitch = (float) Mth.wrapDegrees(scanPitch + pitch);
                                if (canRaycast(pos, nextPitch, nextYaw)) {
                                    return Map.entry(nextYaw, nextPitch);
                                }
                            }
                        }
                    }
                }
            }

            return Map.entry(
                    (float) Rotations.getYaw(clickOffset(pos)), (float) Rotations.getPitch(clickOffset(pos)));
        }
        return Map.entry(
                (float) Rotations.getYaw(clickOffset(pos)), (float) Rotations.getPitch(clickOffset(pos)));
    }

    public static boolean canRaycast(BlockPos pos, float pitch, float yaw) {
        assert mc.player != null;

        if (PrinterUtils.fakePlayer == null) {
            PrinterUtils.initFakePlayer();
        }

        PrinterUtils.updateFakePlayer(pitch, yaw);

        HitResult pHitResult = PrinterUtils.fakePlayer.pick(4, 1.0f, false);

        if (pHitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockHitResult hitResult = (BlockHitResult) pHitResult;

        return hitResult.getBlockPos().relative(hitResult.getDirection()).equals(pos);
    }

    public static Vec3 clickOffset(BlockPos pos) {
        return clickOffset(pos, getPlaceDirection(pos));
    }

    public static Vec3 clickOffset(BlockPos pos, Direction direction) {
        return Vec3.atCenterOf(pos).add(direction.getStepX() * 0.5, direction.getStepY() * 0.5, direction.getStepZ() * 0.5);
    }

    public static BlockHitResult getSafeHitResult(BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        Direction direction = Direction.UP;

        Vec3 offset;
        double yHeight = 0;

        for (Direction dir : getDirections()) {
            // Performance!
            mutable.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
            if (!isReplaceable(mutable)) {
                yHeight = getHeight(mutable);

                direction = dir;

                if (dir == Direction.DOWN) {
                    break;
                }
            }
        }

        offset = clickOffset(pos, direction);

        if (yHeight <= 0.2) {
            offset = new Vec3(offset.x, Math.floor(offset.y) + MAGIC_PLACE_OFFSET, offset.z);
        }

        return new BlockHitResult(offset, direction.getOpposite(), mutable.set(pos.getX() + direction.getStepX(), pos.getY() + direction.getStepY(), pos.getZ() + direction.getStepZ()), false);
    }

    public static double getHeight(BlockPos pos) {
        return mc.player.clientLevel.getBlockState(pos).getShape(mc.player.clientLevel, pos).max(Direction.Axis.Y);
    }

    public static BlockHitResult getBlockHitResult(
            boolean raytrace, BlockPos pos, Direction direction) {
        if (raytrace) {
            assert mc.player != null;

            Map.Entry<Float, Float> rot = getRotation(true, pos);

            if (PrinterUtils.fakePlayer == null) {
                return null;
            }

            PrinterUtils.updateFakePlayer(rot.getValue(), rot.getKey());

            return (BlockHitResult) PrinterUtils.fakePlayer.pick(4.5, 1.0f, false);
        }

        return new BlockHitResult(
                clickOffset(pos),
                direction == null ? getPlaceDirection(pos).getOpposite() : direction,
                pos.relative(direction == null ? getPlaceDirection(pos) : direction.getOpposite()),
                false);
    }


    public static boolean placeBlock(FindItemResult itemResult, BlockPos pos, double distance) {
        if (canPlace(pos, distance)) {
            if (!itemResult.isHotbar()) {
                return false;
            }

            PacketUtils.send(new ServerboundSetCarriedItemPacket(itemResult.slot()));
            //mc.player.getInventory().setSelectedSlot(itemResult.slot());

            PacketUtils.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            PacketUtils.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, getSafeHitResult(pos), 0));

            return true;
        }

        return false;
    }

    public static List<int[]> findNearBlocksByRadius(BlockPos.MutableBlockPos pos, int radius, Predicate<int[]> predicate) {
        if (mc.level == null) {
            return new ArrayList<>();
        }

        ArrayList<int[]> blockPosList = new ArrayList<>((radius * radius) * 2);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

        for (int bX = -radius; bX <= radius; bX++) {
            blockPos.setX(pos.getX() + bX);
            for (int bZ = -radius; bZ <= radius; bZ++) {
                blockPos.setZ(pos.getZ() + bZ);

                var posVec = new int[]{blockPos.getX(), blockPos.getY(), blockPos.getZ()};
                if (predicate.test(posVec)) {
                    blockPosList.add(posVec);
                }
            }
        }

        return blockPosList;
    }
}
