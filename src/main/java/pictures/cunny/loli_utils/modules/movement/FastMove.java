package pictures.cunny.loli_utils.modules.movement;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.events.KeyboardInputEvent;
import pictures.cunny.loli_utils.modules.LoliModule;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

public class FastMove extends LoliModule {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    private final Setting<Integer> movePackets =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("packets")
                            .description("How many times to move per tick pre burst.")
                            .defaultValue(3)
                            .sliderRange(1, 6)
                            .build());
    private final Setting<Integer> inAirMovePackets =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("in-air-packets")
                            .description("How many times to move per tick pre burst while in the air.")
                            .defaultValue(1)
                            .sliderRange(1, 6)
                            .build());

    private final Setting<Integer> burstDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("burst-delay")
                            .description("How long between burst movements.")
                            .defaultValue(3)
                            .sliderRange(1, 10)
                            .build());
    private final Setting<Integer> burstMovements =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("burst-amount")
                            .description("How many times to move per tick pre burst.")
                            .defaultValue(1)
                            .sliderRange(1, 5)
                            .build());
    private final Setting<Boolean> headSnap =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("head-snap")
                            .description(".")
                            .defaultValue(false)
                            .build());
    private final Setting<Double> snapMultiplier =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("snap-mutli")
                            .description("How hard to head snap.")
                            .defaultValue(1.3)
                            .sliderRange(0.3, 5.0)
                            .build());
    private final Setting<Boolean> autoJump =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("auto-jump")
                            .description("Auto Jump while being speedy.")
                            .defaultValue(true)
                            .build());
    private final Setting<Boolean> fastFall =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("fast-fall")
                            .description("Forces you down.")
                            .defaultValue(true)
                            .build());
    public final Setting<Double> minFallHeight =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("fall-height")
                            .description("How far do you have to fall before enabling.")
                            .defaultValue(0.5)
                            .sliderRange(0.4, 2.5)
                            .build());
    public final Setting<Integer> jumpWait =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("jump-wait")
                            .description("How long to wait after jumping.")
                            .defaultValue(5)
                            .sliderRange(1, 20)
                            .build());
    private final Setting<Boolean> forceSprint =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("force-sprint")
                            .description("Forcefully sprint while being speedy.")
                            .defaultValue(true)
                            .build());

    private int lastJump = 0;
    private int burstTicks = 0;
    private int cycle = -1;

    public FastMove() {
        super(LoliUtilsMeteor.CATEGORY, "fast-move", "Attempts to move very quickly.");
        this.timedTicks = 5;
    }

    @Override
    public void update() {
        if (mc.player == null) {
            return;
        }

        if (mc.options.keyJump.isDown()) {
            lastJump = jumpWait.get();
        }

        lastJump--;

        if (notPressingKeys()) {
            return;
        }

        if (burstTicks >= burstDelay.get()) {
            burstTicks = 0;
            for (int i = 0; i < burstMovements.get(); i++) {
                sendMovements();
            }
        } else {
            burstTicks++;
        }

        float ogYaw = mc.player.getXRot();

        for (int i = 0; i < (mc.player.onGround() ? movePackets.get() : inAirMovePackets.get()); i++) {
            sendMovements();
        }

        if (headSnap.get()) {
            mc.player.setXRot(ogYaw);
        }
    }

    @EventHandler
    public void onInputs(KeyboardInputEvent event) {
        if (mc.player == null) {
            return;
        }

        if (notPressingKeys()) {
            return;
        }

        event.input = new Input(
                !shouldFastFall() && mc.options.keyUp.isDown(),
                !shouldFastFall() && mc.options.keyDown.isDown(),
                !shouldFastFall() && mc.options.keyLeft.isDown(),
                !shouldFastFall() && mc.options.keyRight.isDown(),
                !shouldFastFall() && ((autoJump.get() && mc.player.onGround() && mc.options.keyUp.isDown()) || mc.options.keyJump.isDown()),
                shouldFastFall() || mc.options.keyShift.isDown(),
                !shouldFastFall() && ((forceSprint.get() && mc.options.keyUp.isDown()) || mc.options.keySprint.isDown())
        );

        if (autoJump.get() && mc.player.onGround() && mc.options.keyUp.isDown()) {
            mc.player.setJumping(true);
        }

        event.cancel();
    }

    private void sendMovements() {
        assert mc.player != null;
        mc.player.walkDistO = mc.player.walkDist;
        mc.player.deltaMovementOnPreviousTick = mc.player.getDeltaMovement();

        if (autoJump.get() && mc.player.onGround() && mc.options.keyUp.isDown()) {
            mc.player.noJumpDelay = 0;
            mc.player.setJumping(true);
        }

        Input newInput = new Input(
                !shouldFastFall() && mc.options.keyUp.isDown(),
                !shouldFastFall() && mc.options.keyDown.isDown(),
                !shouldFastFall() && mc.options.keyLeft.isDown(),
                !shouldFastFall() && mc.options.keyRight.isDown(),
                !shouldFastFall() && ((autoJump.get() && mc.player.onGround() && mc.options.keyUp.isDown()) || mc.options.keyJump.isDown()),
                shouldFastFall() || mc.options.keyShift.isDown(),
                !shouldFastFall() && ((forceSprint.get() && mc.options.keyUp.isDown()) || mc.options.keySprint.isDown())
        );

        if (!mc.player.lastSentInput.equals(newInput)) {
            PacketUtils.send(new ServerboundPlayerInputPacket(newInput));
            mc.player.lastSentInput = newInput;
        }

        if (headSnap.get()) {
            mc.player.setYRot(Mth.wrapDegrees(mc.player.getYRot() + (float) (cycle * snapMultiplier.get())));

            cycle++;

            if (cycle > 1) {
                cycle = -1;
            }
        }

        mc.player.aiStep();

        if (forceSprint.get() && (mc.options.keyUp.isDown() || mc.player.isSwimming())) {
            mc.player.setSprinting(true);
            PacketUtils.send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
        }

        mc.player.sendPosition();

        PacketUtils.send(new ServerboundClientTickEndPacket());
    }

    public boolean shouldFastFall() {
        return fastFall.get() && mc.player.fallDistance > minFallHeight.get() && !mc.player.onGround() && lastJump <= 0;
    }

    public boolean notPressingKeys() {
        return !shouldFastFall() && !mc.options.keyUp.isDown() && !mc.options.keyDown.isDown() && !mc.options.keyRight.isDown() && !mc.options.keyLeft.isDown();
    }
}
