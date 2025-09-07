package pictures.cunny.loli_utils.modules.movement.elytrafly;

import baritone.api.BaritoneAPI;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.EntityUtils;

import java.util.Objects;

public class BaritoneTweaks extends Module {
    public static FireworkRocketEntity fireworkRocket;
    public final SettingGroup sgAutoActivate = settings.createGroup("Auto Activate");
    public final Setting<Boolean> shouldAutoFly =
            sgAutoActivate.add(
                    new BoolSetting.Builder()
                            .name("persistent-fly")
                            .description(".")
                            .defaultValue(false)
                            .build());
    public final Setting<Integer> durability =
            sgAutoActivate.add(
                    new IntSetting.Builder()
                            .name("durability")
                            .description("Assure you have a suitable elytra.")
                            .defaultValue(30)
                            .sliderRange(1, Items.ELYTRA.components().get(DataComponents.MAX_DAMAGE))
                            .range(1, Items.ELYTRA.components().get(DataComponents.MAX_DAMAGE))
                            .build());
    public final Setting<BlockPos> flyLocation =
            sgAutoActivate.add(
                    new BlockPosSetting.Builder()
                            .name("persistent-loc")
                            .description(".")
                            .defaultValue(BlockPos.ZERO)
                            .build());
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Boolean> hijackGoto =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("hijack-goto")
                            .description(".")
                            .defaultValue(false)
                            .build());

    public BaritoneTweaks() {
        super(
                LoliUtilsMeteor.CATEGORY,
                "baritone-tweaks",
                "Allows for using baritone-elytra without fireworks - Always enabled.");
    }

    public static boolean hasSuitableElytra(int minDurability) {
        FindItemResult result =
                InvUtils.find(
                        itemStack ->
                                itemStack.getItem() == Items.ELYTRA
                                        && itemStack.getMaxDamage() - itemStack.getDamageValue() > minDurability);
        ItemStack chestStack =
                Objects.requireNonNull(MeteorClient.mc.player).getInventory().equipment.get(EquipmentSlot.CHEST);
        return result.found() || chestStack.getMaxDamage() - chestStack.getDamageValue() <= minDurability;
    }


    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (!EntityUtils.isInNether()) return;

        if (isActive()) return;

        if (mc.player != null) {
            if (hasSuitableElytra(durability.get())) {
                if (shouldAutoFly.get()) {
                    if (!BaritoneAPI.getProvider().getPrimaryBaritone().getElytraProcess().isActive()) {
                        BaritoneAPI.getProvider()
                                .getPrimaryBaritone()
                                .getElytraProcess()
                                .pathTo(flyLocation.get());
                    }
                }
            }
        }
    }
}
