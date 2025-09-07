package pictures.cunny.loli_utils.mixin;

import com.mojang.authlib.minecraft.client.ObjectMapper;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

@Mixin(YggdrasilUserApiService.class)
public class YggdrasilUserApiServiceMixin {
    @Inject(method = "reportAbuse", at = @At("HEAD"), remap = false)
    public void reportAbuse(AbuseReportRequest request, CallbackInfo ci) {
        LoliUtilsMeteor.LOGGER.info("Report content: {}", ObjectMapper.create().writeValueAsString(request));
    }
}
