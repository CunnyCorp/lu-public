package pictures.cunny.loli_utils.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class CrystalUtils {
    public static final List<Item> FALLBACK_BLOCKS = List.of(Items.ENDER_CHEST, Items.NETHERITE_BLOCK, Items.CRYING_OBSIDIAN);

    public static boolean isSurroundMissing() {
        for (Direction dir : BlockUtils.HORIZONTALS) {
            assert mc.player != null;
            if (BlockUtils.isReplaceable(mc.player.blockPosition().relative(dir))) return true;
        }
        return false;
    }

    public static int isPhased(LivingEntity player) {
        int i = 0;
        assert mc.player != null;
        assert mc.level != null;
        BlockPos pos = player.blockPosition();
        if (!BlockUtils.isReplaceable(pos) && player.isColliding(pos, mc.player.level().getBlockState(pos)))
            i++;
        for (Direction direction : BlockUtils.HORIZONTALS) {
            BlockPos offset = pos.relative(direction);
            if (!BlockUtils.isReplaceable(offset) && player.isColliding(offset, mc.player.level().getBlockState(offset))) {
                i++;
            }

            if (!BlockUtils.isReplaceable(offset.relative(Direction.UP)) && player.isColliding(offset.relative(Direction.UP), mc.player.level().getBlockState(offset.relative(Direction.UP)))) {
                i++;
            }
        }

        return i;
    }
}
