package pictures.cunny.loli_utils.utility.timed;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import pictures.cunny.loli_utils.events.TimedEvent;
import pictures.cunny.loli_utils.modules.LoliModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimedModules {
    private static final Map<LoliModule, Integer> MODULE_TIMERS = new HashMap<>();

    @PreInit
    public static void onInit() {
        MeteorClient.EVENT_BUS.subscribe(new TimedModules());
    }

    @EventHandler(priority = 1000)
    public void onTick(TimedEvent event) {
        List<LoliModule> removeModules = new ArrayList<>();

        for (LoliModule module : MODULE_TIMERS.keySet()) {
            if (!module.isActive()) {
                removeModules.add(module);
            } else {
                if (MODULE_TIMERS.get(module) > 0) {
                    MODULE_TIMERS.put(module, MODULE_TIMERS.get(module) - 1);
                }
            }
        }

        removeModules.forEach(MODULE_TIMERS::remove);
    }

    public static boolean isTicking(LoliModule module) {
        if (!module.isActive()) {
            return true;
        }

        if (!MODULE_TIMERS.containsKey(module)) {
            return true;
        }

        return MODULE_TIMERS.get(module) > 0;
    }

    public static void tickModule(LoliModule module) {
        MODULE_TIMERS.put(module, module.timedTicksSetting.get() == -1 ? 0 : module.timedTicksSetting.get());
    }
}
