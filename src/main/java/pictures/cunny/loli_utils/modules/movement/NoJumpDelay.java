package pictures.cunny.loli_utils.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

public class NoJumpDelay extends Module {
    public NoJumpDelay() {
        super(LoliUtilsMeteor.CATEGORY, "no-jump-delay", "Removes the jump delay.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            return;
        }
        mc.player.noJumpDelay = 0;
    }
}
