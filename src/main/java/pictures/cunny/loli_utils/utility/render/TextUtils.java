package pictures.cunny.loli_utils.utility.render;

import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import pictures.cunny.loli_utils.utility.StringUtils;

import java.awt.*;

public class TextUtils {
    public static Style MODULE_NAME_STYLE = Style.EMPTY.withColor(new Color(109, 241, 218).getRGB()).withBold(true);
    public static Style MODULE_INFO_STYLE = Style.EMPTY.withColor(new Color(205, 123, 230).getRGB());
    public static Style MODULE_INFO_SUB_STYLE = Style.EMPTY.withColor(new Color(123, 160, 230).getRGB()).withItalic(true);

    public static MutableComponent BRACKET_OPEN_COMPONENT = Component.literal("[").setStyle(Style.EMPTY.withColor(new Color(125, 126, 126).getRGB()).withBold(true));
    public static MutableComponent BRACKET_CLOSE_COMPONENT = Component.literal("]").setStyle(Style.EMPTY.withColor(new Color(125, 126, 126).getRGB()).withBold(true));

    public static MutableComponent getModuleNameFormat(Module module) {
        MutableComponent component = Component.empty();

        component.append(BRACKET_OPEN_COMPONENT);

        component.append(Component.literal(StringUtils.readable(module.name)).withStyle(MODULE_NAME_STYLE));

        component.append(BRACKET_CLOSE_COMPONENT);

        return component;
    }
}
