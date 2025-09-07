package pictures.cunny.loli_utils.mixin;

import io.netty.channel.ChannelFuture;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

import java.net.InetSocketAddress;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(
            method =
                    "connect",
            at = @At("HEAD"))
    private static void connect0(
            InetSocketAddress inetSocketAddress, boolean bl, Connection connection, CallbackInfoReturnable<ChannelFuture> cir) {
        LoliUtilsMeteor.LOGGER.info("Server Ip - {}:{}", inetSocketAddress.getAddress(), inetSocketAddress.getPort());
    }
}
