package pictures.cunny.loli_utils.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.inventory.MenuType;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

// DO NOT LEAK
public class FunnyX extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> iterations =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("iterations")
                            .description("How many iterations to run.")
                            .defaultValue(1)
                            .sliderMin(1)
                            .sliderMax(10)
                            .build());
    public final Setting<Integer> tickDelay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("delay")
                            .description("The delay per opening in ticks")
                            .defaultValue(1)
                            .sliderRange(0, 40)
                            .build());

    private int delay;

    public FunnyX() {
        super(LoliUtilsMeteor.CATEGORY, "funny-x", "Spams open a mounted entity if it has storage.");
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof ClientboundOpenScreenPacket packet) {
            if (packet.getType() == MenuType.HOPPER
                    || packet.getType() == MenuType.SHULKER_BOX
                    || packet.getType() == MenuType.GENERIC_9x3
                    || packet.getType() == MenuType.GENERIC_9x6) {
                assert mc.player != null;
                event.cancel();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (delay >= tickDelay.get() && mc.player != null) {
            int it = 0;

            boolean isMountingStorage =
                    mc.player.getVehicle() != null;

            do {
                if (isMountingStorage)
                    mc.player.connection.send(
                            new ServerboundPlayerCommandPacket(
                                    mc.player, ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
                delay = 0;
            } while (it++ < iterations.get());
        }
        delay++;
    }
}
