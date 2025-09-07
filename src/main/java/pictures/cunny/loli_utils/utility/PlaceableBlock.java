package pictures.cunny.loli_utils.utility;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

public record PlaceableBlock(InteractionHand hand, FindItemResult itemResult, BlockPos blockPos) {
}
