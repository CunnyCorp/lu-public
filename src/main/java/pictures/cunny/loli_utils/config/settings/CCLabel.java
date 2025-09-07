package pictures.cunny.loli_utils.config.settings;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import pictures.cunny.loli_utils.utility.render.SpecialEffects;

public class CCLabel extends WLabel {
    public CCLabel(String text, boolean title) {
        super(text, title);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!text.isEmpty()) {
            renderer.text(text, x, y, color != null ? color : getTheme().textColor(), false);
        }
    }

    @Override
    public void set(String text) {
        if (Double.isFinite(width)) {
            double nw = Math.abs(Math.round(theme.textWidth(text, text.length(), title)) - width);
            if (nw > SpecialEffects.UPDATE_THRESHOLD) {
                invalidate();
            }
        } else {
            invalidate();
        }

        this.text = text;
    }
}
