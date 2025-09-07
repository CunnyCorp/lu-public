package pictures.cunny.loli_utils.modules.printer.movesets;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import pictures.cunny.loli_utils.modules.printer.PrinterUtils;

import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ConstElytraMove extends DefaultMove {
    private double speed;
    private int ticks;
    private boolean loaded = true;

    @Override
    public MoveSets type() {
        return MoveSets.CONST_EFLY;
    }

    @Override
    public void tick(BlockPos pos) {
        ticks++;

        if (loaded) {
            MeteorClient.EVENT_BUS.subscribe(MoveSets.CONST_EFLY.movement);
            loaded = false;
            return;
        }

        if (mc.player.isFallFlying()) {
            if (Utils.distance(pos.getX(), pos.getY(), pos.getZ(),
                    mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ()) > PrinterUtils.PRINTER.descendRange.get()) {
                if (mc.player.getY() > pos.getY() + (PrinterUtils.PRINTER.maxDistanceAbove.get() + PrinterUtils.PRINTER.minDistanceAbove.get())) {
                    mc.player.setXRot((float) Math.min(12, mc.player.getXRot() + PrinterUtils.PRINTER.subtlePitchStep.get()));
                } else if (mc.player.getY() < pos.getY() + PrinterUtils.PRINTER.minDistanceAbove.get()) {
                    float nextPitch = mc.player.getXRot();
                    if (nextPitch < PrinterUtils.PRINTER.constantPitch.get()) {
                        nextPitch += PrinterUtils.PRINTER.dramaticPitchStep.get();
                    } else if (nextPitch > PrinterUtils.PRINTER.constantPitch.get()) {
                        nextPitch -= PrinterUtils.PRINTER.dramaticPitchStep.get();
                    }
                    mc.player.setXRot(nextPitch);
                } else {
                    float nextPitch = mc.player.getXRot();
                    if (nextPitch < PrinterUtils.PRINTER.constantPitch.get()) {
                        nextPitch += PrinterUtils.PRINTER.dramaticPitchStep.get();
                    } else if (nextPitch > PrinterUtils.PRINTER.constantPitch.get()) {
                        nextPitch -= PrinterUtils.PRINTER.subtlePitchStep.get();
                    }
                    mc.player.setXRot(nextPitch);
                }
            } else {
                speed -= PrinterUtils.PRINTER.elytraSpeedStepDec.get();
                if (speed < 3) {
                    speed = 3;
                }
                mc.player.setXRot((float) Math.min(32, mc.player.getXRot() + PrinterUtils.PRINTER.dramaticPitchStep.get()));
            }

            mc.player.setXRot((float) Rotations.getYaw(pos));
        } else {
            mc.player.setXRot((float) Rotations.getYaw(pos));

            if (Utils.distance(pos.getX(), pos.getY(), pos.getZ(),
                    mc.player.getBlockX(), pos.getY(), mc.player.getBlockZ()) <= PrinterUtils.PRINTER.descendRange.get()) {
                mc.options.keyUp.setDown(true);
                return;
            }

            if (PrinterUtils.PRINTER.jumpTimer >= 20) {
                PrinterUtils.PRINTER.jumpTimer = 0;
            }

            mc.player.setXRot(PrinterUtils.PRINTER.takeOffPitch.get().floatValue());

            switch (PrinterUtils.PRINTER.jumpTimer) {
                case 0:
                    mc.player.jumpFromGround();
                    break;
                case 3:
                    mc.player.setJumping(false);
                    mc.player.setSprinting(true);
                    Objects.requireNonNull(mc.getConnection())
                            .send(
                                    new ServerboundPlayerCommandPacket(
                                            mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                    mc.player.startFallFlying();
                    //mc.player.jump();
                    break;
            }

            PrinterUtils.PRINTER.jumpTimer++;
        }
    }

    @EventHandler(priority = 99999)
    private void onPlayerMove(PlayerMoveEvent event) {
        assert mc.player != null;

        if (!(mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA)) return;
        if (!mc.player.isFallFlying() || mc.player.isInLiquid()) {
            speed = 15;
            return;
        }

        double yaw = mc.player.getYRot();
        double x = (speed / 20d) * Math.cos(Math.toRadians(yaw + 90d));
        double z = (speed / 20d) * Math.sin(Math.toRadians(yaw + 90d));

        ((IVec3d) event.movement).meteor$setXZ(x, z);

        if (ticks >= 1) {
            if (speed >= PrinterUtils.PRINTER.elytraSpeed.get()) speed = PrinterUtils.PRINTER.elytraSpeed.get();
            else
                speed = Math.min(PrinterUtils.PRINTER.elytraSpeed.get(), speed + PrinterUtils.PRINTER.elytraSpeedStep.get());

            ticks = 0;
        }

        mc.player.setDeltaMovement(event.movement.x(), event.movement.y(), event.movement.z());
    }

    @Override
    public void cancel(BlockPos pos) {
        ticks = 0;
        speed = 7;
        if (mc.player != null) {
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
        }

        loaded = true;
        MeteorClient.EVENT_BUS.unsubscribe(MoveSets.CONST_EFLY.movement);
    }
}
