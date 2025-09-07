package pictures.cunny.loli_utils.modules.misc;


import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.BlockActivateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.List;

public class StashMover extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<List<Item>> items =
            sgDefault.add(
                    new ItemListSetting.Builder()
                            .name("items")
                            .description("A list of items to dump.")
                            .defaultValue(Items.SHULKER_BOX)
                            .build());
    public final Setting<BlockPos> destinationPos =
            sgDefault.add(
                    new BlockPosSetting.Builder()
                            .name("destination-pos")
                            .description("The 'home' position where you will be dumping items.")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    public final Setting<BlockPos> eChestPos =
            sgDefault.add(
                    new BlockPosSetting.Builder()
                            .name("e-chest-pos")
                            .description("The e-chest to dump with.")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    public final Setting<BlockPos> pearlChestPos =
            sgDefault.add(
                    new BlockPosSetting.Builder()
                            .name("pearl-chest-pos")
                            .description("The pearl-chest to take pearls from.")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    public final Setting<BlockPos> chamberPos =
            sgDefault.add(
                    new BlockPosSetting.Builder()
                            .name("chamber-pos")
                            .description("Where to rotate to for entering the water column.")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    public final Setting<BlockPos> chamberExitPos =
            sgDefault.add(
                    new BlockPosSetting.Builder()
                            .name("chamber-exit-pos")
                            .description("Where to rotate to for exiting the chamber.")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    public final Setting<Double> pitch =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("pitch")
                            .description("Pitch to rotate to when arriving.")
                            .defaultValue(0)
                            .decimalPlaces(1)
                            .sliderRange(-90, 90)
                            .build());
    public final Setting<Double> yaw =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("yaw")
                            .description("Yaw to rotate to when arriving.")
                            .defaultValue(0)
                            .decimalPlaces(1)
                            .sliderRange(-180, 180)
                            .build());
    public final Setting<Double> pearlPitch =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("pearl-pitch")
                            .description("Pitch to rotate to for throwing pearl.")
                            .defaultValue(0)
                            .decimalPlaces(1)
                            .sliderRange(-90, 90)
                            .build());
    public final Setting<Double> pearlYaw =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("pearl-yaw")
                            .description("Yaw to rotate to for throwing pearl.")
                            .defaultValue(0)
                            .decimalPlaces(1)
                            .sliderRange(-180, 180)
                            .build());
    private final SettingGroup sgTimeout = settings.createGroup("Timeout");
    public final Setting<Integer> initialTimeout =
            sgTimeout.add(
                    new IntSetting.Builder()
                            .name("initial")
                            .description(".")
                            .defaultValue(15)
                            .sliderRange(1, 256)
                            .build());
    public final Setting<Integer> initialMoveTimeout =
            sgTimeout.add(
                    new IntSetting.Builder()
                            .name("initial-move")
                            .description(".")
                            .defaultValue(3)
                            .sliderRange(1, 256)
                            .build());
    public final Setting<Integer> containerTimeout =
            sgTimeout.add(
                    new IntSetting.Builder()
                            .name("container")
                            .description(".")
                            .defaultValue(15)
                            .sliderRange(1, 256)
                            .build());


    private int splitSlot = -1;
    private int placedSlot = -1;
    private int step = 0;
    private int idleTimer = 0;
    private boolean lockSteps = false;
    private boolean fixingInWall = false;

    public StashMover() {
        super(LoliUtilsMeteor.CATEGORY, "stash-mover", "Helps with moving stashes.");
    }

    @Override
    public void onActivate() {
        this.step = 0;
        fixingInWall = false;
        this.lockSteps = false;
        this.splitSlot = -1;
        this.placedSlot = -1;
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (mc.player == null) {
            return;
        }

        if (event.packet instanceof ServerboundChatCommandPacket(String command)) {
            if (command.contains("kill")) {
                step = 0;
                fixingInWall = false;
                lockSteps = false;
                splitSlot = -1;
                placedSlot = -1;
            }
        }

        if (event.packet instanceof ServerboundChatCommandSignedPacket packet) {
            if (packet.command().contains("kill")) {
                step = 0;
                fixingInWall = false;
                lockSteps = false;
                splitSlot = -1;
                placedSlot = -1;
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || !mc.player.isAlive()) {
            step = 0;
            fixingInWall = false;
            lockSteps = false;
            splitSlot = -1;
            placedSlot = -1;
            return;
        }

        if (isNearHome() && !isDoingSteps()) {
            if (lockSteps) {
                return;
            }
            step = 1;
            return;
        }

        if (!isNearHome()) {
            step = 0;
            fixingInWall = false;
            lockSteps = false;
            splitSlot = -1;
            placedSlot = -1;
            return;
        }

        if (idleTimer > 0) {
            idleTimer--;
            return;
        }

        switch (step) {
            case 1 -> {
                boolean stillFixing = false;
                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

                blockPos.set(mc.player.blockPosition().getX(), mc.player.blockPosition().getY(), mc.player.blockPosition().getZ() + Direction.NORTH.getStepZ());

                if (BlockUtils.isPlayerInside(blockPos) && !BlockUtils.isReplaceable(blockPos)) {
                    mc.player.setYRot(Direction.SOUTH.toYRot());
                    stillFixing = true;
                    fixingInWall = true;
                }

                blockPos.set(mc.player.blockPosition().getX(), mc.player.blockPosition().getY(), mc.player.blockPosition().getZ() + Direction.SOUTH.getStepZ());

                if (!stillFixing && BlockUtils.isPlayerInside(blockPos) && !BlockUtils.isReplaceable(blockPos)) {
                    mc.player.setYRot(Direction.NORTH.toYRot());
                    stillFixing = true;
                    fixingInWall = true;
                }

                blockPos.set(mc.player.blockPosition().getX() + Direction.EAST.getStepX(), mc.player.blockPosition().getY(), mc.player.blockPosition().getZ());

                if (!stillFixing && BlockUtils.isPlayerInside(blockPos) && !BlockUtils.isReplaceable(blockPos)) {
                    mc.player.setYRot(Direction.WEST.toYRot());
                    stillFixing = true;
                    fixingInWall = true;
                }

                blockPos.set(mc.player.blockPosition().getX() + Direction.WEST.getStepX(), mc.player.blockPosition().getY(), mc.player.blockPosition().getZ());

                if (!stillFixing && BlockUtils.isPlayerInside(blockPos) && !BlockUtils.isReplaceable(blockPos)) {
                    mc.player.setYRot(Direction.EAST.toYRot());
                    stillFixing = true;
                    fixingInWall = true;
                }

                if (fixingInWall) {
                    if (stillFixing) {
                        mc.options.keyUp.setDown(true);
                        lockSteps = true;
                    } else {
                        mc.options.keyUp.setDown(false);
                        fixingInWall = false;
                        lockSteps = false;
                    }
                } else {
                    idleTimer += initialTimeout.get();
                }


            }

            case 2 -> {
                if (!mc.player.onGround()) {
                    mc.player.setXRot((float) Rotations.getPitch(chamberExitPos.get()));
                    mc.player.setYRot((float) Rotations.getYaw(chamberExitPos.get()));
                    mc.options.keyUp.setDown(true);

                    if (mc.player.isInLiquid()) {
                        mc.options.keyJump.setDown(true);
                    }

                    lockSteps = true;
                } else {
                    lockSteps = false;
                    mc.options.keyUp.setDown(false);
                    mc.options.keyJump.setDown(false);
                }

                idleTimer += initialMoveTimeout.get();
            }

            case 4, 5, 6, 7 -> {
                mc.player.setXRot(pitch.get().floatValue());
                mc.player.setYRot(yaw.get().floatValue());
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(yaw.get().floatValue(), pitch.get().floatValue(), mc.player.onGround(), mc.player.horizontalCollision));
            }

            case 10, 40 -> {
                int slot = InventoryUtils.findMatchingSlot((stack, slot1) -> items.get().contains(stack.getItem()));

                if (slot == -1) {
                    lockSteps = false;
                    idleTimer += 3;
                } else {
                    lockSteps = true;

                    InventoryUtils.dropSlot(slot, true);
                }
            }

            case 15 -> {
                PacketUtils.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(
                        Vec3.atCenterOf(eChestPos.get()),
                        Direction.UP,
                        eChestPos.get(),
                        false
                ), 0));
                MeteorClient.EVENT_BUS.post(BlockActivateEvent.get(Blocks.ENDER_CHEST.defaultBlockState()));

                idleTimer += containerTimeout.get();
            }

            case 45 -> {
                mc.player.setXRot((float) Rotations.getPitch(chamberPos.get().getCenter()));
                mc.player.setYRot((float) Rotations.getYaw(chamberPos.get().getCenter()));
                if (!mc.player.isInLiquid()) {
                    mc.options.keyUp.setDown(true);

                    lockSteps = true;
                } else {
                    lockSteps = false;
                    mc.options.keyUp.setDown(false);
                }

            }

            case 51 -> {
                PacketUtils.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(
                        Vec3.atCenterOf(pearlChestPos.get()),
                        Direction.UP,
                        pearlChestPos.get(),
                        false
                ), 0));
                idleTimer += containerTimeout.get();
            }

            case 70 -> {
                try {
                    if (mc.player.containerMenu.getType() != MenuType.GENERIC_9x6 && mc.player.containerMenu.getType() != MenuType.GENERIC_9x3) {
                        step = 50;
                        return;
                    }
                } catch (UnsupportedOperationException e) {
                    step = 50;
                    return;
                }

                int findSingle = InventoryUtils.findMatchingSlot((stack, slot) -> stack.getItem() == Items.ENDER_PEARL && stack.getCount() == 1);

                if (findSingle != -1) {
                    splitSlot = -1;
                    InventoryUtils.swapToHotbar(findSingle, mc.player.getInventory().getSelectedSlot());
                } else {
                    splitSlot = InventoryUtils.findMatchingSlot((stack, slot) -> stack.getItem() == Items.ENDER_PEARL);

                    if (splitSlot == -1) {
                        step = 0;
                        fixingInWall = false;
                        lockSteps = true;
                        mc.player.closeContainer();
                        return;
                    }

                    InventoryUtils.pickup(splitSlot);
                }

                idleTimer += 3;
            }

            case 73 -> {
                if (splitSlot != -1) {

                    if (mc.player.containerMenu.getCarried().isEmpty()) {
                        step = 69;
                        return;
                    }

                    int slot4 = InventoryUtils.findMatchingSlot((stack, s) -> stack.isEmpty() && s != splitSlot);

                    if (slot4 == -1) {
                        step = 0;
                        lockSteps = true;
                        mc.player.closeContainer();
                        return;
                    }

                    placedSlot = slot4;

                    InventoryUtils.placeItem(slot4);
                }

                idleTimer += 3;
            }

            case 76 -> {
                if (splitSlot != -1) {
                    InventoryUtils.pickup(splitSlot);
                }

                idleTimer += 3;
            }

            case 80 -> {
                if (splitSlot != -1) {
                    InventoryUtils.swapToHotbar(placedSlot, mc.player.getInventory().getSelectedSlot());
                }

                idleTimer += 3;
            }

            case 84 -> {
                mc.player.connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, pearlYaw.get().floatValue(), pearlPitch.get().floatValue()));

                idleTimer += 3;
            }

            case 90 -> {
                PacketUtils.command("kill");
                lockSteps = true;
                step = 0;
            }
        }

        if (!lockSteps) {
            step++;
        }
    }

    private boolean isDoingSteps() {
        return step > 0;
    }

    public boolean isNearHome() {
        assert mc.player != null;
        return mc.player.distanceToSqr(destinationPos.get().getCenter()) <= 6;
    }
}
