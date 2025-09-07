package pictures.cunny.loli_utils.mixin.meteorclient;

import meteordevelopment.meteorclient.utils.network.Capes;
import meteordevelopment.orbit.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

@Mixin(value = Capes.class, remap = false)
public class CapesMixin {
    @Redirect(
            method = "init",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lmeteordevelopment/meteorclient/utils/network/MeteorExecutor;execute(Ljava/lang/Runnable;)V"))
    private static void init0(Runnable task) {
        LoliUtilsMeteor.LOGGER.info("Blocking meteor capes from functioning.");
    }

    @Redirect(
            method = "init",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lmeteordevelopment/orbit/IEventBus;subscribe(Ljava/lang/Class;)V"))
    private static void init1(IEventBus instance, Class<?> aClass) {
    }
}
