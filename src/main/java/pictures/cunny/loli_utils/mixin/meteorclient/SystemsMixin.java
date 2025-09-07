package pictures.cunny.loli_utils.mixin.meteorclient;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.config.LoliConfig;

@Mixin(value = Systems.class, remap = false)
public abstract class SystemsMixin {
    @Shadow
    private static System<?> add(System<?> system) {
        return null;
    }

    @Inject(
            method = "init",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lmeteordevelopment/meteorclient/systems/Systems;add(Lmeteordevelopment/meteorclient/systems/System;)Lmeteordevelopment/meteorclient/systems/System;",
                    ordinal = 1,
                    shift = At.Shift.AFTER))
    private static void init0(CallbackInfo ci) {
        add(new LoliConfig());
    }
}
