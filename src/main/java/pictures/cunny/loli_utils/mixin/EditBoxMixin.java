package pictures.cunny.loli_utils.mixin;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EditBox.class, priority = 1)
public class EditBoxMixin {
    @Inject(
            method = "getMaxLength",
            at = @At(value = "HEAD"),
            cancellable = true)
    public void insert0(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(999999999);
    }
}
