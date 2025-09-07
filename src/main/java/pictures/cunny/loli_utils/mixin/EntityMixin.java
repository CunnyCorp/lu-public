package pictures.cunny.loli_utils.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.modules.movement.FastMove;
import pictures.cunny.loli_utils.utility.timed.TickType;

@Mixin(value = Entity.class)
public class EntityMixin {
    @Inject(method = "applyEffectsFromBlocks()V", at = @At("HEAD"), cancellable = true)
    public void applyEffectsFromBlocks(CallbackInfo ci) {
        if (Modules.get().isActive(FastMove.class) && Modules.get().get(FastMove.class).tickType.get() == TickType.Custom) {
            ci.cancel();
        }
    }
}
