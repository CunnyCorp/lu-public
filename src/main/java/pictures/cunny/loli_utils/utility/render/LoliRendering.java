package pictures.cunny.loli_utils.utility.render;

import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LoliRendering {
    public static void renderBlock(Renderer3D render, BlockPos pos, Color... colors) {
        render.blockLines(pos.getX(), pos.getY(), pos.getZ(), colors[0], 0);
        if (colors.length > 1) render.blockSides(
                pos.getX(), pos.getY(), pos.getZ(), colors[1], 0);
    }

    public static void renderRetractingCube(Renderer3D renderer, RenderWrap wrap) {
        if (wrap.blockPos() == null) {
            return;
        }

        int x = wrap.blockPos().getX();
        int y = wrap.blockPos().getY();
        int z = wrap.blockPos().getZ();

        double constrictSize = 0.5 * ((double) wrap.fadeTime() / wrap.maxFadeTime());

        renderer.boxSides(x + constrictSize, y + constrictSize, z + constrictSize,
                x + 1 - constrictSize, y + 1 - constrictSize, z + 1 - constrictSize,
                wrap.color(), 0);
    }

    public static void tickFadeTime(List<RenderWrap> wraps) {
        for (RenderWrap wrap : new ArrayList<>(wraps)) {
            if (wrap.fadeTime() < wrap.maxFadeTime()) {
                wrap.fadeTime(wrap.fadeTime() + 1);
            } else {
                wraps.remove(wrap);
            }
        }
    }

    public static void quad(Renderer3D renderer, Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3, Color color) {
        renderer.quad(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z, v3.x, v3.y, v3.z, color);
    }

    public static void line(Renderer3D renderer, Vec3 v0, Vec3 v1, Color color) {

    }

    public static void removeAnyDupes(List<RenderWrap> wraps, BlockPos blockPos) {
        wraps.removeIf(wrap -> wrap.blockPos().equals(blockPos));
    }
}
