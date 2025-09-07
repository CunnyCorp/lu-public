package pictures.cunny.loli_utils.mixin;

import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.Utf8String;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Utf8String.class)
public abstract class Utf8StringMixin {
    @Redirect(
            method = "read",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lio/netty/buffer/ByteBufUtil;utf8MaxBytes(I)I",
                    remap = false))
    private static int decode0(int seqLength) {
        return ByteBufUtil.utf8MaxBytes(100000000);
    }

    @Redirect(
            method = "read",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;length()I", ordinal = 0))
    private static int decode1(String instance) {
        return 0;
    }

    @Redirect(
            method = "write",
            at = @At(value = "INVOKE", target = "Ljava/lang/CharSequence;length()I", ordinal = 0))
    private static int encode0(CharSequence instance) {
        return 0;
    }

    @Redirect(
            method = "write",
            at =
            @At(
                    value = "INVOKE",
                    target = "Lio/netty/buffer/ByteBufUtil;utf8MaxBytes(I)I",
                    ordinal = 0,
                    remap = false))
    private static int encode1(int seqLength) {
        return ByteBufUtil.utf8MaxBytes(100000000);
    }
}
