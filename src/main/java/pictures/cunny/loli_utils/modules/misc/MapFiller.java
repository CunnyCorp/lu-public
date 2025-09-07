package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.phys.AABB;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.Comparator;
import java.util.List;

public class MapFiller extends Module {
    private final Comparator<ItemFrame> distanceComparator = Comparator.comparingDouble(value -> mc.player == null ? 0 : mc.player.getEyePosition().distanceTo(value.position()));

    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Boolean> autoBreak =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("auto-break")
                            .description("Automatically breaks maps that aren't whitelisted.")
                            .defaultValue(false)
                            .build());
    public final Setting<List<String>> whitelist =
            sgDefault.add(
                    new StringListSetting.Builder()
                            .name("map-ids")
                            .description("Map IDs to not break.")
                            .build());
    public final Setting<Double> range =
            sgDefault.add(
                    new DoubleSetting.Builder()
                            .name("range")
                            .description("Range to interact with maps.")
                            .defaultValue(3.5)
                            .sliderRange(1, 5)
                            .build());
    public final Setting<Integer> delay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("delay")
                            .description("How long between attacks.")
                            .defaultValue(6)
                            .sliderRange(1, 20)
                            .build());
    public final Setting<Integer> timeout =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("timeout")
                            .description("Timeout for inventory interactions.")
                            .defaultValue(6)
                            .sliderRange(1, 20)
                            .build());
    private int timer = 0;
    private ItemFrame targetFrame = null;

    public MapFiller() {
        super(LoliUtilsMeteor.CATEGORY, "map-filler", "Fills maps into item frames.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        List<ItemFrame> frames = mc.level.getEntitiesOfClass(ItemFrame.class, new AABB(mc.player.position().add(6, 6, 6), mc.player.position().subtract(6, 6, 6)), (entity) -> {
            if (entity.position().distanceTo(mc.player.getEyePosition()) <= range.get()) {
                if (entity.getItem().isEmpty()) {
                    return true;
                }

                if (!entity.getItem().has(DataComponents.MAP_ID)) {
                    return false;
                }

                MapId mapId = entity.getFramedMapId(entity.getItem());

                if (mapId == null) {
                    return false;
                }

                return autoBreak.get() && !whitelist.get().contains(Integer.toString(mapId.id()));
            }

            return false;
        });

        frames.sort(distanceComparator);

        if (!frames.isEmpty()) {
            timer = delay.get();
            targetFrame = frames.getFirst();

            if (!targetFrame.getItem().isEmpty()) {
                PacketUtils.send(ServerboundInteractPacket.createAttackPacket(frames.getFirst(), mc.player.isShiftKeyDown()));
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        if (targetFrame != null) {
            FindItemResult result = InvUtils.find(stack -> {
                if (stack.isEmpty()) {
                    return false;
                }

                if (stack.has(DataComponents.MAP_ID)) {
                    MapId mapId = stack.get(DataComponents.MAP_ID);

                    return mapId != null && whitelist.get().contains(Integer.toString(mapId.id()));
                }

                return false;
            });

            if (!result.found()) {
                targetFrame = null;
                return;
            }

            if (!result.isHotbar()) {
                InventoryUtils.swapToHotbar(result.slot(), 7);
                timer = timeout.get();
                return;
            } else {
                InventoryUtils.swapSlot(result.slot());
            }

            PacketUtils.send(ServerboundInteractPacket.createInteractionPacket(targetFrame, mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND, targetFrame.position()));
            PacketUtils.send(ServerboundInteractPacket.createInteractionPacket(targetFrame, mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
            mc.player.swing(InteractionHand.MAIN_HAND);
            timer = timeout.get();
            targetFrame = null;
        }
    }
}
