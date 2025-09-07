package pictures.cunny.loli_utils.modules.printer.themes;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.BlockPos;

import static pictures.cunny.loli_utils.modules.printer.PrinterUtils.PRINTER;

public class SortedTheme extends PlacingTheme {
    @Override
    public SettingColor getNextColor(BlockPos pos) {
        int origIndex = PRINTER.nextColorIndex;

        PRINTER.nextColorIndex++;

        if (PRINTER.nextColorIndex >= PRINTER.placingColors.get().size()) {
            PRINTER.nextColorIndex = 0;
        }

        return PRINTER.placingColors.get().get(origIndex);
    }
}
