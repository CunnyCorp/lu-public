package pictures.cunny.loli_utils.utility;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunkSection;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ChunkUtils {
    public static boolean containsBlocks(int x, int z, List<Block> blockList) {
        assert mc.level != null;

        for (LevelChunkSection section : mc.level.getChunk(x, z).getSections()) {
            if (section.hasOnlyAir()) {
                continue;
            }

            if (section.maybeHas(blockState -> blockList.contains(blockState.getBlock()))) {
                return true;
            }
        }

        return false;
    }

    public static void testPalette(int x, int z) {
        for (LevelChunkSection section : mc.level.getChunk(x, z).getSections()) {
            if (section.hasOnlyAir()) {
                continue;
            }

            LoliUtilsMeteor.LOGGER.info("Chunk: {}, {} - Packet Size: {} - {}", x, z, section.getSerializedSize(), section.nonEmptyBlockCount);
        }
    }
}
