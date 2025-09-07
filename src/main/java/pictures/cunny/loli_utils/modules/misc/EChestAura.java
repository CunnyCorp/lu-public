package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.BlockActivateEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.BlockUtils;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.ArrayList;
import java.util.List;

public class EChestAura extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> delay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("delay")
                            .description("How long between opening e-chest's.")
                            .defaultValue(10)
                            .sliderRange(1, 20)
                            .build());

    private int ticks = 0;

    public EChestAura() {
        super(LoliUtilsMeteor.CATEGORY, "echest-aura", "Uses nearby ender chests if your inventory is full and you aren't in a gui.", "ec-aura", "ender-chest-aura");
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        assert mc.level != null;

        if (ticks > 0) {
            ticks--;
            return;
        }

        if (mc.screen == null && InventoryUtils.isInventoryFull()) {
            int items = 0;

            for (ItemStack stack : EChestMemory.ITEMS) {
                if (stack != ItemStack.EMPTY) {
                    items++;
                }
            }

            if (items != 27) {
                List<int[]> blockPositions = new ArrayList<>();

                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

                for (int i = -4; i <= 4; i++) {
                    blockPositions.addAll(BlockUtils.findNearBlocksByRadius(mc.player.blockPosition().offset(0, i, 0).mutable(), 4, (pos) -> {
                        blockPos.set(pos[0], pos[1], pos[2]);
                        return mc.level.getBlockEntity(blockPos, BlockEntityType.ENDER_CHEST).isPresent();
                    }));
                }

                blockPositions.sort(BlockUtils.CLOSEST_XZ_COMPARATOR);

                if (!blockPositions.isEmpty()) {
                    int[] eChestPos = blockPositions.getFirst();
                    blockPos.set(eChestPos[0], eChestPos[1], eChestPos[2]);
                    PacketUtils.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(
                            Vec3.atCenterOf(blockPos),
                            Direction.UP,
                            blockPos,
                            false
                    ), 0));
                    MeteorClient.EVENT_BUS.post(BlockActivateEvent.get(Blocks.ENDER_CHEST.defaultBlockState()));
                    ticks = delay.get();
                }
            }
        }
    }
}
