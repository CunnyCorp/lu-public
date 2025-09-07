package pictures.cunny.loli_utils.modules.printer.movesets;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.core.BlockPos;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BaritoneMove extends DefaultMove {
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();

    @Override
    public MoveSets type() {
        return MoveSets.BARITONE;
    }

    @Override
    public void tick(BlockPos pos) {
        if (!baritone.getPathingBehavior().hasPath()) {
            baritone.getCustomGoalProcess().setGoalAndPath(new GoalNear(pos, this.radius()));
        }
    }

    @Override
    public void cancel(BlockPos pos) {
        if (mc.player == null) {
            return;
        }

        if (baritone.getPathingBehavior().hasPath()) {
            baritone.getPathingBehavior().cancelEverything();
        }
    }
}
