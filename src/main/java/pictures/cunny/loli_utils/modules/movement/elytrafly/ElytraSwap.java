package pictures.cunny.loli_utils.modules.movement.elytrafly;

import baritone.api.BaritoneAPI;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.InventoryUtils;

public class ElytraSwap extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    private final Setting<Integer> durabilitySwap =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("durability")
                            .description("The durability to swap at.")
                            .defaultValue(30)
                            .sliderRange(1, Items.ELYTRA.components().get(DataComponents.MAX_DAMAGE))
                            .range(1, Items.ELYTRA.components().get(DataComponents.MAX_DAMAGE))
                            .build());

    private int stepCounter = -1;
    private FindItemResult result;

    public ElytraSwap() {
        super(LoliUtilsMeteor.CATEGORY, "elytra-swap", "Strict focused elytra swapping.");
    }

    @Override
    public void onActivate() {
        stepCounter = -1;
        result = null;
        if (BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().isActive()) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().cancel();
        }
    }

    @Override
    public void onDeactivate() {
        this.onActivate();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            return;
        }

        if (stepCounter > 0) {
            stepCounter++;
            processStep();
            return;
        }

        ItemStack chestStack = mc.player.getInventory().equipment.get(EquipmentSlot.CHEST);

        if (chestStack.getItem() == Items.ELYTRA
                && chestStack.getMaxDamage() - chestStack.getDamageValue() <= durabilitySwap.get()) {
            result =
                    InvUtils.find(
                            itemStack ->
                                    itemStack.getItem() == Items.ELYTRA
                                            && itemStack.getMaxDamage() - itemStack.getDamageValue() > durabilitySwap.get());

            if (!result.found() || result.isArmor()) return;

            stepCounter = 1;
            processStep();
        }
    }

    private void processStep() {
        if (stepCounter == -1 || result == null || mc.player == null || mc.gameMode == null) {
            stepCounter = -1;
            result = null;
            return;
        }

        switch (stepCounter) {
            case 1:
                InventoryUtils.swapSlot(
                        result.isHotbar() ? result.slot() : InventoryUtils.findEmptySlotInHotbar(7));
                break;
            case 4:
                if (result.isHotbar()) {
                    break;
                }

                InventoryUtils.swapToHotbar(result.slot(), mc.player.getInventory().getSelectedSlot());
                break;
            case 8:
                mc.gameMode.useItem(
                        mc.player, result.isOffhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                break;
            case 12:
                stepCounter = -1;
        }
    }
}
