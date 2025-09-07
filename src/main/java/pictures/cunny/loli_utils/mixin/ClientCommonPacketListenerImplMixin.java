package pictures.cunny.loli_utils.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.events.KeepAliveEvent;

@Mixin(value = ClientCommonPacketListenerImpl.class, priority = 100000000)
public class ClientCommonPacketListenerImplMixin {
    @Redirect(
            method = "onPacketError",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/DisconnectionDetails;)V"))
    public void onPacketException0(Connection instance, DisconnectionDetails disconnectionDetails) {
        LoliUtilsMeteor.LOGGER.info("Prevented being kicked for a packet issue.");
    }

    @Inject(
            method = "handleKeepAlive",
            at = @At(value = "HEAD"),
            cancellable = true)
    public void blockKeepAlive0(ClientboundKeepAlivePacket packet, CallbackInfo ci) {
        if (MeteorClient.EVENT_BUS.post(KeepAliveEvent.get(packet)).isCancelled()) {
            ci.cancel();
        }
    }
}
