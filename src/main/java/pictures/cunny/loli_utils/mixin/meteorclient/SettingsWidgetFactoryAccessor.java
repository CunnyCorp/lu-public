package pictures.cunny.loli_utils.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = SettingsWidgetFactory.class, remap = false)
public interface SettingsWidgetFactoryAccessor {
    @Accessor("factories")
    Map<Class<?>, SettingsWidgetFactory.Factory> getFactories();
}
