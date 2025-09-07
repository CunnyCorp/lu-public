package pictures.cunny.loli_utils.events;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;

public class KeepAliveEvent extends Cancellable {
    public static final KeepAliveEvent INSTANCE = new KeepAliveEvent();
    public ClientboundKeepAlivePacket packet;

    public KeepAliveEvent() {
        this.setCancelled(false);
        this.packet = null;
    }

    public static KeepAliveEvent get(ClientboundKeepAlivePacket packet) {
        INSTANCE.setCancelled(false);
        INSTANCE.packet = packet;
        return INSTANCE;
    }
}
