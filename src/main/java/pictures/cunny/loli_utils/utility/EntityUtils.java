package pictures.cunny.loli_utils.utility;


import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EntityUtils {
    private static final List<EntityType<?>> collidable =
            List.of(EntityType.ITEM, EntityType.TRIDENT, EntityType.ARROW, EntityType.AREA_EFFECT_CLOUD, EntityType.WIND_CHARGE, EntityType.SPECTRAL_ARROW);

    public static boolean canPlaceIn(Entity entity) {
        return collidable.contains(entity.getType()) || entity.isRemoved() || entity.isSpectator();
    }

    public static boolean isInNether() {
        return mc.player != null
                && mc.player
                .level().dimensionType().respawnAnchorWorks();
    }

    // For use in multi-ticking behavior, lmao?
    public static boolean isTouchingGround() {
        // Account for slabs.
        assert mc.player != null;
        if (BlockUtils.isNotAir(mc.player.blockPosition())) {
            double yS = mc.player.getBlockStateOn().getInteractionShape(mc.level, mc.player.getOnPos()).max(Direction.Axis.Y);

            // Phased ?
            if (yS == 1) {
                return true;
            }

            return mc.player.getY() - (Math.round(mc.player.getY()) + yS) < 0.02;
        }

        return mc.player.getY() - Math.round(mc.player.getY()) < 0.02;
    }
}
