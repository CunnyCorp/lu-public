package pictures.cunny.loli_utils.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import pictures.cunny.loli_utils.events.TimedEvent;
import pictures.cunny.loli_utils.utility.timed.TickType;
import pictures.cunny.loli_utils.utility.timed.TimedModules;

public class LoliModule extends Module {
    public int timedTicks = -1;

    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<TickType> tickType = sgDefault.add(new EnumSetting.Builder<TickType>()
            .name("tick-mode")
            .description("Which tick event to use.")
            .defaultValue(TickType.Pre)
            .build());
    public final Setting<Integer> timedTicksSetting = sgDefault.add(new IntSetting.Builder()
            .name("timed-ticks")
            .description("How many 10ms ticks to wait per cycle.")
            .defaultValue(timedTicks)
            .build());


    public LoliModule(Category category, String name, String description, String... aliases) {
        super(category, name, description, aliases);
    }

    @Override
    public void onActivate() {
        if (timedTicksSetting.get() != -1) {
            TimedModules.tickModule(this);
        }

        this.safeOnActivate();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (tickType.get() == TickType.Pre) update();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (tickType.get() == TickType.Post) update();
    }

    @EventHandler
    private void onTimed(TimedEvent event) {
        if (TimedModules.isTicking(this)) return;
        if (tickType.get() == TickType.Custom) {
            update();
            TimedModules.tickModule(this);
        }
    }

    public void safeOnActivate() {
    }

    public void update() {
    }
}
