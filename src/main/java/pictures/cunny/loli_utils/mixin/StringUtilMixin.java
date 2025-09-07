package pictures.cunny.loli_utils.mixin;

import net.minecraft.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StringUtil.class)
public class StringUtilMixin {
    /**
     * @author loli-tummy
     * @reason Disable client side character validation.
     */
    /*@Overwrite
    public static boolean isValidChar(char c) {
        return true;
    }*/
}
