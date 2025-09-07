package pictures.cunny.loli_utils.utility.packets;

import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public record BlockPlacement(InteractionHand hand, BlockHitResult hitResult) {
    public void place() {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        PacketUtils.send(new ServerboundUseItemOnPacket(hand, hitResult, 0));
        //mc.gameMode.useItemOn(mc.player, hand, hitResult);
    }
}
