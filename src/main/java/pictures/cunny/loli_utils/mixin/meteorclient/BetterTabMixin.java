package pictures.cunny.loli_utils.mixin.meteorclient;

import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pictures.cunny.loli_utils.utility.render.SpecialEffects;

@Mixin(value = BetterTab.class, remap = false)
public class BetterTabMixin {
    @Inject(method = "getPlayerName", at = @At(value = "RETURN"), cancellable = true)
    private void modifyColor(
            PlayerInfo playerListEntry, CallbackInfoReturnable<Component> cir) {
        if (SpecialEffects.hasColor(playerListEntry.getProfile().getId().toString())) {
            cir.setReturnValue(SpecialEffects.styleName(playerListEntry.getProfile().getName(), playerListEntry.getProfile().getId().toString()));
        }
    }
}
