package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.apache.commons.lang3.time.FastDateFormat;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.config.settings.LabelHCKSetting;
import pictures.cunny.loli_utils.config.settings.LabelListHCKSetting;
import pictures.cunny.loli_utils.utility.render.TextState;

import java.util.List;

public class ExampleModule extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    private int llDelay = 0;
    private final Setting<List<TextState>> labels =
            sgDefault.add(
                    new LabelListHCKSetting((setting) -> {
                        llDelay++;

                        for (int i = 0; i < 4; i++) {
                            TextState state = setting.getOrCreate(i);
                            boolean isEven = i % 2 == 0;

                            Color exColor = llDelay <= (isEven ? 20 : 0) ? Color.BLUE : Color.RED;


                            state.updatePart(0, i == 0 ? " - Example of Labels - " : "Wow more?", exColor);
                            state.updatePart(1, FastDateFormat.getInstance("dd, MM, yyyy, H, m, s").format(System.currentTimeMillis() + (i * 15000)), i % 2 == 0 ? Color.MAGENTA : Color.CYAN);
                        }

                        if (llDelay > 40) {
                            llDelay = 0;
                        }
                    }));
    private int lDelay = 0;
    private final Setting<TextState> label =
            sgDefault.add(
                    new LabelHCKSetting((setting) -> {
                        lDelay++;

                        setting.textState.updatePart(0, "Dripping cheese all over my ", Color.YELLOW);
                        setting.textState.updatePart(1, "lunch.", lDelay <= 20 ? Color.YELLOW : Color.ORANGE);

                        if (lDelay > 40) {
                            lDelay = 0;
                        }
                    }));

    public ExampleModule() {
        super(LoliUtilsMeteor.CATEGORY, "example-module", "An example module, to show new settings.");
    }
}
