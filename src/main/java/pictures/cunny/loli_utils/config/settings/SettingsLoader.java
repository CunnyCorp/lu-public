package pictures.cunny.loli_utils.config.settings;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import pictures.cunny.loli_utils.utility.render.TextState;

import java.util.Map;

public class SettingsLoader {
    public static void init(Map<Class<?>, SettingsWidgetFactory.Factory> factories) {
        factories.put(LabelHCKSetting.class, (table, setting) -> labelW(table, (LabelHCKSetting) setting));
        factories.put(LabelListHCKSetting.class, (table, setting) -> labelListW(table, (LabelListHCKSetting) setting));
        factories.put(ButtonHCKSetting.class, (table, setting) -> buttonW(table, (ButtonHCKSetting) setting));

    }

    private static void buttonW(WTable table, ButtonHCKSetting setting) {
        table.getRow(table.rowI()).removeLast();
        WButton button = GuiThemes.get().button(setting.name);
        button.action = setting.runnable;
        table.add(button);
    }

    private static void labelW(WTable table, LabelHCKSetting setting) {
        table.getRow(table.rowI()).removeLast();

        Cell<?> cell = table.add(GuiThemes.get().horizontalList()).center();
        WHorizontalList textSection = (WHorizontalList) cell.widget();

        for (WLabel label : setting.get().getParts(GuiThemes.get())) {
            textSection.add(label);
        }
    }

    private static void labelListW(WTable table, LabelListHCKSetting setting) {
        table.getRow(table.rowI()).removeLast();

        WVerticalList textList = table.add(GuiThemes.get().verticalList()).expandX().widget();

        for (TextState text : setting.get()) {
            Cell<?> cell = textList.add(GuiThemes.get().horizontalList()).center();
            WHorizontalList textSection = (WHorizontalList) cell.widget();

            for (WLabel label : text.getParts(GuiThemes.get())) {
                textSection.add(label);
            }
        }
    }
}
