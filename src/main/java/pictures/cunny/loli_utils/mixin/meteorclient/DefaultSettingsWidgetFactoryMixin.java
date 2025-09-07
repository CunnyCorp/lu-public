package pictures.cunny.loli_utils.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pictures.cunny.loli_utils.config.settings.SettingsLoader;

@Mixin(value = DefaultSettingsWidgetFactory.class, remap = false)
public class DefaultSettingsWidgetFactoryMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(GuiTheme theme, CallbackInfo ci) {
        SettingsLoader.init(((SettingsWidgetFactoryAccessor) this).getFactories());
    }
}
