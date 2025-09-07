package pictures.cunny.loli_utils.modules.printer.themes;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.BlockPos;

import static pictures.cunny.loli_utils.modules.printer.PrinterUtils.PRINTER;

public class ZTheme extends PlacingTheme {
    @Override
    public SettingColor getNextColor(BlockPos pos) {
        int i = Math.abs(pos.getZ() % PRINTER.placingColors.get().size());

        return PRINTER.placingColors.get().get(i);
    }
}
