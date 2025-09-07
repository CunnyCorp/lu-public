package pictures.cunny.loli_utils.modules.printer.themes;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.BlockPos;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pictures.cunny.loli_utils.modules.printer.PrinterUtils.PRINTER;

public class AsciiTheme extends PlacingTheme {
    public List<List<Integer>> asciiPositions = new ArrayList<>();
    public int partXLength;
    public int partYLength;
    public boolean isFricked = false;


    public AsciiTheme() {
        loadAscii();
    }

    public void loadAscii() {
        asciiPositions.clear();


        if (PRINTER == null) {
            isFricked = true;
            return;
        }

        String[] ascii = PRINTER.asciiPreset.get().ascii.split("\n");

        isFricked = false;

        int lastLength = -1;

        for (String line : ascii) {
            if (line.isBlank()) {
                continue;
            }

            if (line.length() != lastLength && lastLength != -1) {
                isFricked = true;
                LoliUtilsMeteor.LOGGER.info("It is all fricked up! The ascii is fricked i tell ya!");
                break;
            }

            lastLength = line.length();

            List<Integer> part = new ArrayList<>();

            for (String piece : line.split("")) {
                piece = piece.trim();
                if (piece.equals("*")) {
                    part.add(1);
                }
                if (piece.equals("#")) {
                    part.add(2);
                } else if (piece.equals("+")) {
                    part.add(3);
                } else if (piece.equals("-")) {
                    part.add(0);
                }
            }

            LoliUtilsMeteor.LOGGER.info(Arrays.toString(part.toArray()));

            asciiPositions.add(part);
        }

        if (asciiPositions.isEmpty()) {
            isFricked = true;
            return;
        }

        partXLength = asciiPositions.getFirst().size();
        partYLength = asciiPositions.size();
    }

    @Override
    public SettingColor getNextColor(BlockPos pos) {
        if (PRINTER.placingColors.get().size() < 2 || isFricked) {
            return PRINTER.placingColors.get().getFirst();
        }

        int y = Math.abs(pos.getZ() % partYLength);
        int x = Math.abs(pos.getX() % (partXLength + 1));

        int color = Math.min(PRINTER.placingColors.get().size() - 1, asciiPositions.get(y).get(Math.min(partXLength - 1, x)));

        return PRINTER.placingColors.get().get(color);
    }

    public enum Preset {
        Heart("""
                ---------
                ---------
                ---*-*---
                --*****--
                ---***---
                ----*----
                ---------
                ---------"""),
        UwU("""
                ---------------
                -*-*-------*-*-
                -*-*-#-#-#-*-*-
                -***--#-#--***-
                ---------------"""),
        OwO("""
                ---------------
                -***-------***-
                -*-*-#-#-#-*-*-
                -***--#-#--***-
                ---------------"""),
        Simple("""
                -----
                --*--
                -+#+-
                --*--
                -----""");

        final String ascii;

        Preset(String ascii) {
            this.ascii = ascii;
        }
    }
}
