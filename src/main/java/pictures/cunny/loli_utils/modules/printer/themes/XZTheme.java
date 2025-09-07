package pictures.cunny.loli_utils.modules.printer.themes;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.BlockPos;

import static pictures.cunny.loli_utils.modules.printer.PrinterUtils.PRINTER;

public class XZTheme extends PlacingTheme {
    @Override
    public SettingColor getNextColor(BlockPos pos) {
        if (PRINTER.placingColors.get().size() < 2) {
            return PRINTER.placingColors.get().getFirst();
        }

        int x = Math.abs(pos.getX() % 7);
        int z = Math.abs(pos.getZ() % 7);

        // Side 1
        if ((x == 3 && z == 2) || (x == 2 && z == 3) || (x == 3 && z == 3)) {
            return PRINTER.placingColors.get().get(1);
        }

        // Side 2
        if ((x == 4 && z == 3) || (x == 3 && z == 4)) {
            return PRINTER.placingColors.get().get(1);
        }

        return PRINTER.placingColors.get().getFirst();
    }
}
