package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.InventoryUtils;

import java.util.ArrayList;
import java.util.List;

public class AutoDrop extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> rate =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("rate")
                            .description("The rate to drop items per tick.")
                            .defaultValue(2)
                            .range(1, 10000)
                            .sliderRange(1, 20)
                            .build());
    public final Setting<Boolean> dropWholeStack =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("drop-whole-stack")
                            .description("Drop the entire stack instead of just one.")
                            .defaultValue(false)
                            .build());
    public final Setting<List<Item>> items =
            sgDefault.add(
                    new ItemListSetting.Builder()
                            .name("items")
                            .description("A list of items to dump.")
                            .defaultValue(Items.SHULKER_BOX)
                            .build());
    public final Setting<List<MenuType<?>>> screens =
            sgDefault.add(
                    new ScreenHandlerListSetting.Builder()
                            .name("screens")
                            .description("The screens to dump items into.")
                            .defaultValue(List.of(MenuType.SHULKER_BOX))
                            .build());
    public final Setting<Integer> dropXTimes =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("drop-amount")
                            .description("How many items to drop at a time.")
                            .defaultValue(9)
                            .range(1, 54)
                            .sliderRange(1, 54)
                            .build());
    private final Setting<Keybind> dropPlayerKey =
            sgDefault.add(
                    new KeybindSetting.Builder()
                            .name("drop-player-key")
                            .description("Drop items from your inventory when this is pressed.")
                            .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_RIGHT))
                            .build());
    private final Setting<Keybind> dropWholeKey =
            sgDefault.add(
                    new KeybindSetting.Builder()
                            .name("drop-container-key")
                            .description("Drop items from all slots when this is pressed.")
                            .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_PAGE_UP))
                            .build());
    private final Setting<Keybind> dropConfKey =
            sgDefault.add(
                    new KeybindSetting.Builder()
                            .name("drop-conf-key")
                            .description("Drop items from X slots when this is pressed.")
                            .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_PAGE_DOWN))
                            .build());
    private final List<ItemStack> droppedSlots = new ArrayList<>();
    private boolean startDropping = false;
    private boolean dumpInventory = false;
    private int dropsLeft = 0;
    private int slot = 0;

    public AutoDrop() {
        super(LoliUtilsMeteor.CATEGORY, "auto-drop", "Automatically drops items.");
    }

    @EventHandler
    public void onScreenOpen(OpenScreenEvent event) {
        startDropping = false;
        slot = 0;
        dropsLeft = 0;
        droppedSlots.clear();
    }

    @EventHandler
    public void onTick(TickEvent.Pre tickEvent) {
        if (!canUseScreen()) {
            if (!dumpInventory && dropPlayerKey.get().isPressed()) {
                dumpInventory = true;
            }

            if (dumpInventory) {
                int slot = InventoryUtils.findMatchingSlot((stack, slot1) -> items.get().contains(stack.getItem()));

                if (slot == -1) {
                    dumpInventory = false;
                    return;
                }

                InventoryUtils.dropSlot(slot, dropWholeStack.get());
            }

            return;
        }

        dumpInventory = false;

        if (!startDropping && dropWholeKey.get().isPressed()) {
            dropsLeft = 54;
            slot = 0;
            startDropping = true;
            droppedSlots.clear();
        } else if (!startDropping && dropConfKey.get().isPressed()) {
            dropsLeft = dropXTimes.get();
            slot = 0;
            startDropping = true;
            droppedSlots.clear();
        }

        if (!startDropping) {
            return;
        }

        int tossed = 0;

        assert mc.player != null;
        for (ItemStack stack : mc.player.containerMenu.getItems()) {
            if (droppedSlots.contains(stack)) {
                continue;
            }

            if (dropsLeft <= 0 || tossed >= rate.get()) {
                break;
            }

            if (items.get().contains(stack.getItem())) {
                droppedSlots.add(stack);
                InventoryUtils.dropSlot(slot, dropWholeStack.get());
                tossed++;
                dropsLeft--;
            }

            slot++;
        }
    }

    public boolean canUseScreen() {
        try {
            return mc.player != null && screens.get().contains(mc.player.containerMenu.getType());
        } catch (Exception e) {
            return false;
        }
    }
}
