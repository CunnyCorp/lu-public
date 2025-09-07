package pictures.cunny.loli_utils.utility.render;

import meteordevelopment.meteorclient.utils.render.color.Color;

public class TextPart {
    public String text;
    public Color color = Color.WHITE;

    public static TextPart of(String text) {
        TextPart inst = new TextPart();
        inst.text = text;

        return inst;
    }

    public static TextPart of(String text, Color color) {
        TextPart inst = new TextPart();
        inst.text = text;
        inst.color = color;

        return inst;
    }
}
