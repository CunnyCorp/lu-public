package pictures.cunny.loli_utils.modules.printer.themes;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.BlockPos;
import pictures.cunny.loli_utils.utility.CollectionUtils;

import static pictures.cunny.loli_utils.modules.printer.PrinterUtils.PRINTER;

public class RandomTheme extends PlacingTheme {
    @Override
    public SettingColor getNextColor(BlockPos pos) {
        return CollectionUtils.random(PRINTER.placingColors.get());
    }
}
