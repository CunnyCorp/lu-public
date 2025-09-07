package pictures.cunny.loli_utils.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.Packet;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HoldPackets extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Set<Class<? extends Packet<?>>>> packets =
            sgDefault.add(
                    new PacketListSetting.Builder()
                            .name("packets")
                            .description("Only hold the packets in this list.")
                            .defaultValue(new HashSet<>())
                            .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
                            .build());

    private final List<Packet<?>> packetHolding = new ArrayList<>();

    public HoldPackets() {
        super(LoliUtilsMeteor.CATEGORY, "hold-packets", "Stores packets till disabled.");
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            for (Packet<?> packet : packetHolding) {
                pictures.cunny.loli_utils.utility.packets.PacketUtils.send(packet);
            }
        }
        packetHolding.clear();
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (packets.get().contains(event.packet.getClass())) {
            packetHolding.add(event.packet);
            event.cancel();
        }
    }
}
