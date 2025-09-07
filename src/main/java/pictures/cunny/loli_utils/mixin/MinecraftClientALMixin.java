package pictures.cunny.loli_utils.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

import java.net.URL;

@Mixin(value = MinecraftClient.class)
public class MinecraftClientALMixin {
    @Inject(
            method = "post(Ljava/net/URL;Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;",
            at = @At("HEAD"),
            remap = false)
    public void post0(URL url, Object body, Class<?> responseClass, CallbackInfoReturnable<?> cir) {
        LoliUtilsMeteor.LOGGER.info("A post via MC AuthLib was made:\n    - URL: {}\n    - Content: {}",
                url.toString(),
                ObjectMapper.create().writeValueAsString(body));
    }
}
