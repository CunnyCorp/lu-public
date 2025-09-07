package pictures.cunny.loli_utils.mixin;

import net.minecraft.world.item.component.WrittenBookContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WrittenBookContent.class)
public class WrittenBookContentMixin {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 3))
    public int init0(int constant) {
        return Integer.MAX_VALUE;
    }
}
