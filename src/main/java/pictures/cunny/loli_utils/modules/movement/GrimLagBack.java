package pictures.cunny.loli_utils.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

public class GrimLagBack extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    private final Setting<Boolean> fullAuto =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("auto")
                            .description("Automatically lag back to gain speed.")
                            .defaultValue(true)
                            .build());
    private final Setting<Integer> delay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("delay")
                            .description("How long between lag-backs.")
                            .defaultValue(3)
                            .sliderRange(1, 50)
                            .build());
    private final Setting<Integer> holdTime =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("hold-time")
                            .description("How long to hold lag-backs.")
                            .defaultValue(3)
                            .sliderRange(1, 50)
                            .build());

    private int timer = 0;
    private int holdTicksLeft = 0;

    public GrimLagBack() {
        super(LoliUtilsMeteor.CATEGORY, "grim-lag-back", ".");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (!mc.player.onGround() && mc.player.isFallFlying()) {
            if (fullAuto.get()) {
                if (holdTicksLeft > 0) {
                    holdTicksLeft--;
                    mc.player.setDeltaMovement(0, 0, 0);
                    PacketUtils.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, true));
                    PacketUtils.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, true));
                } else if (timer >= delay.get()) {
                    timer = 0;
                    holdTicksLeft = holdTime.get();
                } else {
                    timer++;
                }
            } else if (mc.options.keyJump.isDown()) {
                mc.player.setDeltaMovement(0, 0, 0);
                PacketUtils.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, true));
                PacketUtils.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, true));
            }
        }
    }
}
