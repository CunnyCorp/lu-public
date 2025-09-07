package pictures.cunny.loli_utils.modules.movement.elytrafly;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

public class OnlyControl extends Module {
    private final SettingGroup sgDefault = this.settings.getDefaultGroup();
    private final Setting<Boolean> liquidCheck =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("liquid-check")
                            .description("Check if the player is in liquid.")
                            .defaultValue(false)
                            .build());
    private final Setting<Boolean> restoreDelta =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("restore-delta")
                            .description("Re-apply delta movement.")
                            .defaultValue(true)
                            .build());
    private final Setting<Boolean> grimRotBypass =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("grim-rotation")
                            .description("Grim is the most bypassable AC, actually.")
                            .defaultValue(false)
                            .build());
    private final Setting<Double> idleRate =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("idle-rate")
                            .description("Elevation while idling.")
                            .defaultValue(0)
                            .range(-1, 1)
                            .sliderRange(-1, 1)
                            .build());
    private final Setting<Double> speedMulti =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("speed-multi")
                            .description("Multiply speed by a tiny amount.")
                            .defaultValue(1.1)
                            .range(1, 999)
                            .sliderRange(1, 2)
                            .build());
    private final Setting<Keybind> controlKey =
            sgDefault.add(
                    new KeybindSetting.Builder()
                            .name("control-key")
                            .description("The key to accelerate or stop depending on further configuration.")
                            .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_W))
                            .build());
    private final Setting<Boolean> invertControlKey =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("invert-key")
                            .description("If enabled makes the key stop you, off accelerates.")
                            .defaultValue(true)
                            .build());

    public OnlyControl() {
        super(LoliUtilsMeteor.CATEGORY, "only-control", "Allows you to pause flight with rockets.");
    }

    // Micro Optimization
    private final double[] previousDelta = new double[3];
    private boolean hasSetDelta = false;
    private boolean controlEnabled = false;

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player.isFallFlying()) {
            PacketUtils.send(new ServerboundPlayerInputPacket(new Input(
                    mc.options.keyUp.isDown(),
                    mc.options.keyDown.isDown(),
                    mc.options.keyLeft.isDown(),
                    mc.options.keyRight.isDown(),
                    true,
                    mc.options.keyShift.isDown(),
                    mc.options.keySprint.isDown()
            )));
        }
    }

    @EventHandler(priority = 9999)
    private void onPlayerMove(PlayerMoveEvent event) {
        assert mc.player != null;

        if (
                !(mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA)
                        || !mc.player.isFallFlying()
                        || (liquidCheck.get() && mc.player.isInLiquid())) {
            return;
        }

        if (shouldControl()) {
            if (!hasSetDelta) {
                previousDelta[0] = event.movement.x;
                previousDelta[1] = event.movement.y;
                previousDelta[2] = event.movement.z;
                hasSetDelta = true;
            }

            if (grimRotBypass.get()) {
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(-179.9f, -89.9f, false, mc.player.horizontalCollision));
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(179.9f, 89.9f, false, mc.player.horizontalCollision));
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(-179.9f, 89.9f, false, mc.player.horizontalCollision));
                PacketUtils.send(new ServerboundMovePlayerPacket.Rot(179.9f, -89.9f, false, mc.player.horizontalCollision));
            }

            ((IVec3d) event.movement).meteor$set(0, idleRate.get(), 0);
            mc.player.setDeltaMovement(0, idleRate.get(), 0);
            controlEnabled = true;
        } else if (controlEnabled) {

            if (hasSetDelta && restoreDelta.get()) {
                hasSetDelta = false;
                ((IVec3d) event.movement).meteor$set(previousDelta[0], previousDelta[1], previousDelta[2]);
                mc.player.setDeltaMovement(previousDelta[0], previousDelta[1], previousDelta[2]);
            }

            controlEnabled = false;
        }
    }

    public boolean shouldControl() {
        return invertControlKey.get() == controlKey.get().isPressed();
    }
}
