package pictures.cunny.loli_utils.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.events.CloseScreenEvent;
import pictures.cunny.loli_utils.events.SendMovementPacketsEventCanc;
import pictures.cunny.loli_utils.events.TickPlayerMovementEvent;

@Mixin(value = LocalPlayer.class, priority = 10000)
public abstract class LocalPlayerMixin {
    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void onSendMovementPacketsHead(CallbackInfo ci) {
        if (MeteorClient.EVENT_BUS.post(SendMovementPacketsEventCanc.get()).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void onTickMovementHead(CallbackInfo ci) {
        if (MeteorClient.EVENT_BUS.post(TickPlayerMovementEvent.get()).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "closeContainer", at = @At("HEAD"))
    private void closeContainerHead(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(CloseScreenEvent.INSTANCE);
    }

}
