package pictures.cunny.loli_utils.utility;

import meteordevelopment.meteorclient.utils.player.SlotUtils;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * The type Inv utils.
 */
public class InventoryUtils {
    public static final Predicate<ItemStack> IS_BLOCK =
            (itemStack) -> Item.BY_BLOCK.containsValue(itemStack.getItem());

    /**
     * Swap slot.
     *
     * @param i the
     */
    public static void swapSlot(int i) {
        assert mc.player != null;
        mc.player.getInventory().setSelectedSlot(i);
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(i));
    }

    public static void dropSlot(int i, boolean stack) {
        if (mc.player == null) {
            return;
        }

        dropSlot(mc.player.containerMenu, i, stack);
    }

    public static void dropSlot(AbstractContainerMenu container, int i, boolean stack) {
        if (mc.player == null || mc.gameMode == null || i > mc.player.getInventory().getContainerSize() + 16) {
            return;
        }

        mc.gameMode.handleInventoryMouseClick(container.containerId, i, stack ? 1 : 0, ClickType.THROW, mc.player);
    }

    public static int findEmptySlotInHotbar(int i) {
        if (mc.player != null) {
            for (var ref =
                 new Object() {
                     int i = 0;
                 };
                 ref.i < 9;
                 ref.i++) {
                if (mc.player.getInventory().getItem(getHotbarOffset() + ref.i).isEmpty()) {
                    return ref.i;
                }
            }
        }
        return i;
    }

    public static int getInventoryOffset() {
        assert mc.player != null;
        return mc.player.containerMenu.slots.size() == 46
                ? mc.player.containerMenu instanceof CraftingMenu ? 10 : 9
                : mc.player.containerMenu.slots.size() - 36;
    }

    public static int getHotbarOffset() {
        return getInventoryOffset() + 27;
    }

    public static void swapToHotbar(int slot, int hot) {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot, hot, ClickType.SWAP, mc.player);
    }

    public static void placeItem(int slot) {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        // This is vanilla functionality.
        // mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, -999, 0, ClickType.THROW, mc.player);
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot, 1, ClickType.PICKUP, mc.player);
    }

    public static void pickup(int slot) {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        // This is vanilla functionality.
        // mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, -999, 0, ClickType.THROW, mc.player);
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot, 0, ClickType.PICKUP, mc.player);
    }

    public static void quickMove(int slot) {
        if (mc.player == null || mc.gameMode == null) {
            return;
        }

        // This is vanilla functionality.
        // mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, -999, 0, ClickType.THROW, mc.player);
        mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
    }

    public static int findMatchingSlot(StackCheck check) {
        int slot = 0;
        assert mc.player != null;
        for (ItemStack stack : mc.player.containerMenu.getItems()) {
            if (check.run(stack, slot)) {
                return slot;
            }

            slot++;
        }

        return -1;
    }

    public static boolean isInventoryFull() {
        for (int i = SlotUtils.indexToId(SlotUtils.MAIN_START); i < SlotUtils.indexToId(SlotUtils.MAIN_START) + 4 * 9; i++) {
            if (!mc.player.containerMenu.getSlot(i).hasItem()) {
                return false;
            }
        }

        return true;
    }

    @FunctionalInterface
    public interface StackCheck {
        boolean run(ItemStack stack, int slot);
    }
}
