package pictures.cunny.loli_utils.mixin.baritone;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.ICustomGoalProcess;
import baritone.command.defaults.GotoCommand;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pictures.cunny.loli_utils.modules.movement.elytrafly.BaritoneTweaks;
import pictures.cunny.loli_utils.utility.EntityUtils;

@Mixin(value = GotoCommand.class, remap = false)
public class GotoCommandMixin {
    @Redirect(
            method = "execute",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lbaritone/api/process/ICustomGoalProcess;setGoalAndPath(Lbaritone/api/pathing/goals/Goal;)V",
                    ordinal = 0))
    public void setGoal(ICustomGoalProcess instance, Goal goal) {
        BaritoneTweaks baritoneTweaks = Modules.get().get(BaritoneTweaks.class);
        if (baritoneTweaks.hijackGoto.get()
                && EntityUtils.isInNether()
                && BaritoneTweaks.hasSuitableElytra(baritoneTweaks.durability.get())) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().pathTo(goal);
            if (goal instanceof GoalXZ goalXZ) {
                BlockPos.MutableBlockPos newPos = new BlockPos.MutableBlockPos();
                newPos.set(goalXZ.getX(), 64, goalXZ.getZ());
                baritoneTweaks.flyLocation.set(newPos.immutable());
                BaritoneAPI.getProvider()
                        .getPrimaryBaritone()
                        .getElytraProcess()
                        .pathTo(baritoneTweaks.flyLocation.get());
            } else if (goal instanceof GoalBlock goalBlock) {
                baritoneTweaks.flyLocation.set(goalBlock.getGoalPos());
                BaritoneAPI.getProvider()
                        .getPrimaryBaritone()
                        .getElytraProcess()
                        .pathTo(baritoneTweaks.flyLocation.get());
            }
        } else {
            instance.setGoalAndPath(goal);
        }
    }
}
