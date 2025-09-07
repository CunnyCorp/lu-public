package pictures.cunny.loli_utils.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.events.ChunkLoadEvent;
import pictures.cunny.loli_utils.events.ChunkUnloadEvent;

@Mixin(value = ClientPacketListener.class, priority = 1)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "updateLevelChunk", at = @At("TAIL"))
    public void onLoadChunk(int x, int z, ClientboundLevelChunkPacketData packetData, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(ChunkLoadEvent.INSTANCE.get(x, z));
    }

    @Inject(method = "handleForgetLevelChunk", at = @At("HEAD"))
    public void onUnloadChunk(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(ChunkUnloadEvent.INSTANCE.get(packet.pos().x, packet.pos().z));
    }
}
