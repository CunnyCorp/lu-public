package pictures.cunny.loli_utils.utility.render;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.config.settings.CCLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TextState {
    public static final TextState EMPTY = TextState.of("");
    public List<TextPart> parts = new ArrayList<>();
    public List<WLabel> labels = new ArrayList<>();

    public static TextState of(String str) {
        TextState inst = new TextState();
        inst.addPart(str);

        return inst;
    }

    public static TextState of(String str, Color color) {
        TextState inst = new TextState();
        inst.addPart(str, color);

        return inst;
    }

    public void addPart(String text) {
        parts.add(TextPart.of(text));
    }

    public void addPart(String text, Color color) {
        parts.add(TextPart.of(text, color));
    }

    public void updatePart(int i, String text) {
        if (i > parts.size()) {
            throw new ArrayIndexOutOfBoundsException("updatePart supplied with (" + i + ") but max is (" + parts.size() + ")");
        }

        if (i == parts.size()) {
            addPart(text);
            LoliUtilsMeteor.LOGGER.info("Adding part at {}", i);
            return;
        }

        if (!Objects.equals(text, parts.get(i).text)) {
            LoliUtilsMeteor.LOGGER.info("Updating part at {}", i);
            parts.get(i).text = text;
            if (!labels.isEmpty()) {
                labels.get(i).set(text);
            }
        }
    }

    public void updatePart(int i, String text, Color color) {
        if (i > parts.size()) {
            throw new ArrayIndexOutOfBoundsException("updatePart supplied with (" + i + ") but max is (" + parts.size() + ")");
        }

        if (i == parts.size() || parts.isEmpty()) {
            addPart(text, color);
            return;
        }

        if (!Objects.equals(text, parts.get(i).text) || parts.get(i).color != color) {
            parts.get(i).text = text;
            parts.get(i).color = color;
            if (!labels.isEmpty()) {
                labels.get(i).set(text);
                labels.get(i).color(color);
            }
        }
    }


    public List<WLabel> getParts(GuiTheme theme) {
        labels.clear();
        for (TextPart part : parts) {
            CCLabel label = new CCLabel(part.text, false);
            label.color = part.color;
            labels.add(label);
        }
        return labels;
    }
}
