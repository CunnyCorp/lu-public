package pictures.cunny.loli_utils.modules.movement.elytrafly;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.*;

public class ControlFly extends Module {
    private final SettingGroup sgDefault = this.settings.getDefaultGroup();
    private final Setting<Integer> speedDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("speed-delay")
                            .description("Delay in ticks to speed you up")
                            .range(1, 200)
                            .sliderRange(1, 20)
                            .defaultValue(2)
                            .build());
    private final Setting<Double> startSpeed =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("start-speed")
                            .description("Start speed settings in bps")
                            .defaultValue(15)
                            .range(5, 500)
                            .sliderRange(5, 47)
                            .build());
    private final Setting<Double> speedSteps =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("speed-steps")
                            .description("How much to increase speed by.")
                            .defaultValue(2.2)
                            .range(0.3, 500)
                            .sliderRange(0.3, 47)
                            .build());
    private final Setting<Double> maxSpeed =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("max-speed")
                            .description("Max speed settings in bps")
                            .defaultValue(44)
                            .range(5, 500)
                            .sliderRange(15, 47)
                            .build());
    private final Setting<Boolean> disableControl =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("disable-control")
                            .description("Makes control options do nothing.")
                            .defaultValue(false)
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
    private final Setting<Integer> controlDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("control-delay")
                            .description("How long to wait before returning to e-fly.")
                            .defaultValue(50)
                            .range(1, 10000)
                            .sliderRange(1, 80)
                            .build());
    private final Setting<Double> idleRate =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("idle-rate")
                            .description("How fast to descend while idling.")
                            .defaultValue(-0.005)
                            .range(-1, 0)
                            .sliderRange(-1, 0)
                            .build());
    private final Setting<Boolean> pullTheFuckBack =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("reset-violations")
                            .description("Pauses everything to reset violations.")
                            .defaultValue(true)
                            .build());
    private final Setting<Boolean> pullTheFuckBackOnPing =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("on-ping")
                            .description("Pauses everything to reset violations.")
                            .defaultValue(false)
                            .build());
    private final Setting<Integer> pullTheFuckBackDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("violation-reset-delay")
                            .description("How long to pause for.")
                            .defaultValue(5)
                            .range(1, 10000)
                            .sliderRange(1, 40)
                            .build());
    private final Setting<Boolean> elytraBypass =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("bypass")
                            .description("Wowsa very cool.")
                            .defaultValue(false)
                            .build());
    private final Setting<Integer> forceDistance =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("force-distance")
                            .description("How long between forced sends.")
                            .defaultValue(100)
                            .range(1, 10000)
                            .sliderRange(1, 300)
                            .build());
    private final Setting<Integer> vrOnForce =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("force-violation-reset")
                            .description("Sets violation reset delay to this value, 0 to disable.")
                            .defaultValue(1)
                            .range(1, 10000)
                            .sliderRange(1, 40)
                            .build());
    private final Setting<Integer> bypassDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("bypass-delay")
                            .description("How long between movement.")
                            .defaultValue(25)
                            .range(1, 10000)
                            .sliderRange(1, 240)
                            .build());
    private final Setting<Integer> bypassWaitDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("bypass-start-delay")
                            .description("How long to wait before enabling the bypass.")
                            .defaultValue(120)
                            .range(1, 10000)
                            .sliderRange(1, 240)
                            .build());
    private final Setting<Boolean> resetSpeed =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("reset-speed")
                            .description("Resets speed on violations.")
                            .defaultValue(false)
                            .build());
    private final Setting<Boolean> flushPackets =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("flush-on-violation")
                            .description("Flushes movement on violations to start anew")
                            .defaultValue(true)
                            .build());
    public final Setting<Set<Class<? extends Packet<?>>>> packets =
            sgDefault.add(
                    new PacketListSetting.Builder()
                            .name("packets")
                            .description("Only hold the packets in this list.")
                            .defaultValue(new HashSet<>())
                            .filter(aClass -> meteordevelopment.meteorclient.utils.network.PacketUtils.getC2SPackets().contains(aClass))
                            .build());

    private Vec3 lastPos = Vec3.ZERO;
    private double speed;
    private int speedTicks;
    private boolean controlEnabled;
    private int bypassTicks = 0;
    private int bypassWaitingTicks = 0;
    private int controlTicks = 0;
    private int pullTheFuckBackTicks = 0;
    private final List<Packet<?>> packetHolding = new ArrayList<>();
    private ClientboundPlayerLookAtPacket lastForcedPacket = null;

    public ControlFly() {
        super(LoliUtilsMeteor.CATEGORY, "control-fly", "Adds some control to elytra fly.");
    }

    @Override
    public String getInfoString() {
        return String.format("%.2f", speed);
    }

    @Override
    public void onActivate() {
        assert mc.player != null;

        if (mc.player.isFallFlying()) {
            Vec3 velocity = mc.player.getDeltaMovement();
            speed = Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z) * 20;
        } else {
            speed = startSpeed.get();
        }

        speedTicks = 0;
        controlEnabled = false;
        bypassTicks = 0;
        bypassWaitingTicks = 0;
        controlTicks = 0;
        pullTheFuckBackTicks = 0;

        packetHolding.clear();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) {
            return;
        }

        if (event.packet instanceof ClientboundPlayerLookAtPacket packet && pullTheFuckBack.get()) {
            if (pullTheFuckBackOnPing.get()) {
                lastForcedPacket = packet;
            } else {
                PacketUtils.updatePosition(Objects.requireNonNull(packet.getPosition(mc.level)));
                mc.player.setPos(Objects.requireNonNull(packet.getPosition(mc.level)));
                pullTheFuckBackTicks = pullTheFuckBackDelay.get();

                if (flushPackets.get()) {
                    packetHolding.clear();
                } else {
                    sendPackets();
                }

                if (resetSpeed.get()) {
                    speed = startSpeed.get();
                }
                speedTicks = 0;
            }
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (elytraBypass.get() && bypassWaitingTicks <= 0 && pullTheFuckBackTicks <= 0 && packets.get().contains(event.packet.getClass())) {
            packetHolding.add(event.packet);
            event.cancel();
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (mc.player == null) {
            return;
        }

        if (!mc.player.isFallFlying()) {
            pullTheFuckBackTicks = 0;
            speedTicks = 0;
            bypassWaitingTicks = bypassWaitDelay.get();
            return;
        }

        if (Math.abs(lastPos.distanceTo(mc.player.getEyePosition())) >= forceDistance.get()) {
            sendPackets();
            // bypassTicks = bypassDelay.get();
            // ?
            pullTheFuckBackTicks = vrOnForce.get();
        }

        bypassWaitingTicks--;

        if (elytraBypass.get()) {
            if (bypassTicks <= 0) {
                sendPackets();
                bypassTicks = bypassDelay.get();
            }

            bypassTicks--;
        }

        pullTheFuckBackTicks--;

        speedTicks++;
    }

    @EventHandler(priority = 9999)
    private void onPlayerMove(PlayerMoveEvent event) {
        assert mc.player != null;

        if (
                !(mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA)
                        || !mc.player.isFallFlying()
                        || mc.player.isInLiquid()) {
            speed = startSpeed.get();
            speedTicks = 0;
            return;
        }

        if (pullTheFuckBackTicks > 0) {
            ((IVec3d) event.movement).meteor$set(0, 0, 0);
            mc.player.setDeltaMovement(0, 0, 0);
            if (resetSpeed.get()) {
                speed = startSpeed.get();
            }
            speedTicks = 0;
            return;
        }

        if (shouldControl()) {
            ((IVec3d) event.movement).meteor$set(0, idleRate.get(), 0);
            mc.player.setDeltaMovement(0, idleRate.get(), 0);
            controlEnabled = true;
            controlTicks = controlDelay.get();

            speed = startSpeed.get();
            speedTicks = 0;
            return;
        } else if (controlEnabled) {
            if (controlTicks > 0) {
                controlTicks--;
                return;
            } else {
                controlEnabled = false;
            }

            speed = startSpeed.get();
            speedTicks = 0;
        }


        double yaw = mc.player.getYRot();
        double x = (speed / 20d) * Math.cos(Math.toRadians(yaw + 90d));
        double z = (speed / 20d) * Math.sin(Math.toRadians(yaw + 90d));

        ((IVec3d) event.movement).meteor$setXZ(x, z);

        if (speedTicks >= speedDelay.get()) {
            speed = speed > maxSpeed.get()
                    ? maxSpeed.get()
                    : Math.min(speed + speedSteps.get(), maxSpeed.get());

            speedTicks = 0;
        }

        mc.player.setDeltaMovement(event.movement.x(), event.movement.y(), event.movement.z());
    }

    public boolean shouldControl() {
        return !disableControl.get() && invertControlKey.get() != controlKey.get().isPressed();
    }

    public void sendPackets() {
        if (mc.player != null) {
            for (Packet<?> packet : packetHolding) {
                PacketUtils.send(packet);
            }

            lastPos = mc.player.getEyePosition();
        }
        packetHolding.clear();
    }
}
