package pictures.cunny.loli_utils.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.events.KeyboardInputEvent;

@Mixin(value = KeyboardInput.class, priority = 1)
public class KeyboardInputMixin {
    @Inject(
            method = "tick",
            at = @At(value = "TAIL")
    )
    public void insert0(CallbackInfo ci) {
        KeyboardInputEvent event = MeteorClient.EVENT_BUS.post(KeyboardInputEvent.get((ClientInput) (Object) this));
        if (event.isCancelled() && event.input != null) {
            ((ClientInput) (Object) this).keyPresses = event.input;
        }
    }
}
