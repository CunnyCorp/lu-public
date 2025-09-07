package pictures.cunny.loli_utils.utility.modules;

import net.minecraft.world.level.dimension.DimensionType;

import java.util.concurrent.Callable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@SuppressWarnings("unused")
public enum DimensionCheck {
    OW(() -> {
        if (mc.player == null) {
            return false;
        }

        DimensionType dimension = mc.player.clientLevel.dimensionType();
        return dimension.bedWorks();
    }),
    OW_OR_NETHER(() -> {
        if (mc.player == null) {
            return false;
        }

        DimensionType dimension = mc.player.clientLevel.dimensionType();
        return dimension.respawnAnchorWorks() || dimension.bedWorks();
    }),
    OW_OR_END(() -> {
        if (mc.player == null) {
            return false;
        }

        DimensionType dimension = mc.player.clientLevel.dimensionType();
        return !dimension.respawnAnchorWorks();
    }),
    NETHER(() -> {
        if (mc.player == null) {
            return false;
        }

        DimensionType dimension = mc.player.clientLevel.dimensionType();
        return dimension.respawnAnchorWorks();
    }),
    NETHER_OR_END(() -> {
        if (mc.player == null) {
            return false;
        }

        DimensionType dimension = mc.player.clientLevel.dimensionType();
        return !dimension.bedWorks();
    }),
    END(() -> {
        if (mc.player == null) {
            return false;
        }

        DimensionType dimension = mc.player.clientLevel.dimensionType();
        return !dimension.bedWorks() && !dimension.respawnAnchorWorks();
    });

    public final Callable<Boolean> check;

    DimensionCheck(Callable<Boolean> check) {
        this.check = check;
    }
}
