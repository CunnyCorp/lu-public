package pictures.cunny.loli_utils.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

public class NoInputs extends Module {
    public NoInputs() {
        super(LoliUtilsMeteor.CATEGORY, "no-inputs", "Stops sending inputs.");
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (mc.player == null) {
            return;
        }

        if (event.packet instanceof ServerboundPlayerInputPacket) {
            event.cancel();
        } else if (event.packet instanceof ServerboundPlayerCommandPacket packet) {
            if (packet.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING || packet.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
                event.cancel();
            }
        }
    }
}
