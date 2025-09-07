package pictures.cunny.loli_utils.utility.render;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.core.BlockPos;
import pictures.cunny.loli_utils.utility.BlockUtils;

public class VIRenderWrap extends RenderWrap {
    public VIRenderWrap(BlockPos blockpos, int maxFadeTime, int fadeTime, int breath, Color color) {
        super(blockpos, maxFadeTime, fadeTime, breath, color);
    }

    @Override
    public boolean shouldSkip() {
        return MeteorClient.mc.player != null && !BlockUtils.isNotAir(this.blockPos()) && this.blockPos().closerThan(MeteorClient.mc.player.getOnPos(), 32);
    }
}
