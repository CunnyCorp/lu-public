package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.StringUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.List;

public class AutoRename extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<List<Item>> items =
            sgDefault.add(
                    new ItemListSetting.Builder()
                            .name("items")
                            .description("A list of items to automatically rename.")
                            .defaultValue(Items.FIREWORK_ROCKET)
                            .build());
    public final Setting<Boolean> onlyUnnamed =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("only-unnamed")
                            .description(".")
                            .defaultValue(true)
                            .build());
    public final Setting<String> itemName =
            sgDefault.add(
                    new StringSetting.Builder()
                            .name("item-name")
                            .description(".")
                            .defaultValue("Nico'd Nya")
                            .build());
    public final Setting<Boolean> itemNameRand =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("item-name-randomizer")
                            .description(".")
                            .defaultValue(false)
                            .build());
    public final Setting<Boolean> unicode =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("unicode")
                            .description(".")
                            .defaultValue(false)
                            .build());
    public final Setting<Integer> itemNameRandLength =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("item-name-randomize-length")
                            .description(".")
                            .defaultValue(50)
                            .build());

    private int step = 0;

    public AutoRename() {
        super(LoliUtilsMeteor.CATEGORY, "auto-rename", "Automatically rename items.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        step++;
        processStep();
    }

    private void processStep() {
        if (mc.player == null || !(mc.player.containerMenu instanceof AnvilMenu menu)) {
            step = 0;
            return;
        }

        switch (step) {
            case 1:
                if (menu.getSlot(0).hasItem()) {
                    step = 0;
                    break;
                }
                break;
            case 4:
                FindItemResult itemResult = InvUtils.find(itemStack -> {
                    if (onlyUnnamed.get() && itemStack.has(DataComponents.CUSTOM_NAME)) {
                        return false;
                    }

                    return items.get().contains(itemStack.getItem());
                });

                if (!itemResult.found()) {
                    step = 0;
                    break;
                }

                InventoryUtils.quickMove(itemResult.slot());
                break;
            case 8:
                PacketUtils.send(new ServerboundRenameItemPacket(itemNameRand.get() ? StringUtils.randomText(itemNameRandLength.get(), unicode.get()) : itemName.get()));
                break;
            case 12:
                InventoryUtils.quickMove(menu.getResultSlot());
                break;
            case 16:
                step = 0;
                break;
        }
    }
}
