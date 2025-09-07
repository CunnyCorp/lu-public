package pictures.cunny.loli_utils.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {
    @Inject(method = "assertValidNamespace", at = @At("HEAD"), cancellable = true)
    private static void validateNamespace0(
            String namespace, String path, CallbackInfoReturnable<String> cir) {
        if (namespace.contains("<"))
            cir.setReturnValue(namespace);
    }

    @Inject(method = "assertValidPath", at = @At("HEAD"), cancellable = true)
    private static void validatePath0(
            String namespace, String path, CallbackInfoReturnable<String> cir) {
        if (path.contains(">"))
            cir.setReturnValue(path);
    }
}
