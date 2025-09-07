package pictures.cunny.loli_utils.mixin.meteorclient;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.utility.Dependencies;

@Mixin(value = Nuker.class, remap = false)
public class NukerMixin {
    @Inject(method = "onTickPre", at = @At("HEAD"), cancellable = true)
    public void onTickPre(TickEvent.Pre event, CallbackInfo ci) {
        if (Modules.get().get(AutoEat.class).eating
                || Modules.get().get(AutoGap.class).isEating()
                || Modules.get().get(KillAura.class).getTarget() != null) {
            ci.cancel();
        }
    }
}
