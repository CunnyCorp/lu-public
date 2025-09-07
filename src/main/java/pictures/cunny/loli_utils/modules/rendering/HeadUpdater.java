package pictures.cunny.loli_utils.modules.rendering;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.entity.player.PlayerModelPart;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

public class HeadUpdater extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> rate =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("rate")
                            .description("How fast to update the client information.")
                            .defaultValue(5)
                            .sliderRange(1, 10)
                            .build());

    private boolean bl = false;

    public HeadUpdater() {
        super(LoliUtilsMeteor.CATEGORY, "head-updater", "Skin flicker basically.");
        this.runInMainMenu = true;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        for (int i = 0; i < rate.get(); i++) {
            updateHead();
        }
    }

    public void updateHead() {
        if (bl) {
            int i = 0;

            for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
                i |= playerModelPart.getMask();
            }

            PacketUtils.send(new ServerboundClientInformationPacket(new ClientInformation(
                    mc.options.languageCode,
                    mc.options.renderDistance().get(),
                    mc.options.chatVisibility().get(),
                    mc.options.chatColors().get(),
                    i,
                    mc.options.mainHand().get(),
                    false,
                    true,
                    mc.options.particles().get()
            )));
        } else {
            int i = 0;

            for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
                if (playerModelPart.getId().equals("hat")) {
                    continue;
                }
                i |= playerModelPart.getMask();
            }

            PacketUtils.send(new ServerboundClientInformationPacket(new ClientInformation(
                    mc.options.languageCode,
                    mc.options.renderDistance().get(),
                    mc.options.chatVisibility().get(),
                    mc.options.chatColors().get(),
                    i,
                    mc.options.mainHand().get(),
                    false,
                    false,
                    mc.options.particles().get()
            )));
        }

        bl = !bl;
    }
}
