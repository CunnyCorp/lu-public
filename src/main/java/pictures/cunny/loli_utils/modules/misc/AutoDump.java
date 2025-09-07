package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.render.TextUtils;

import java.util.List;

public class AutoDump extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> rate =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("rate")
                            .description("The rate to move items per tick.")
                            .defaultValue(6)
                            .sliderRange(1, 20)
                            .build());
    public final Setting<Integer> delay =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("delay")
                            .description("How long before doing any actions after opening a screen.")
                            .defaultValue(10)
                            .sliderRange(1, 20)
                            .build());
    public final Setting<Boolean> autoClose =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("auto-close")
                            .description("Automatically closes screen after putting everything in.")
                            .defaultValue(true)
                            .build());
    public final Setting<Boolean> containerInfo =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("container-info")
                            .description("Shows debug information in chat after auto-closing.")
                            .defaultValue(true)
                            .visible(autoClose::get)
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
                            .defaultValue(List.of(MenuType.GENERIC_9x3, MenuType.GENERIC_9x6))
                            .build());

    private int screenOpenTicks = 0;
    private boolean hasDumpedItems = false;
    private int transferredItems = 0;

    public AutoDump() {
        super(LoliUtilsMeteor.CATEGORY, "auto-dump", "Automatically dump items into chests, skid of meteors but won't time you out.");
    }

    @EventHandler
    public void onTick(TickEvent.Post tickEvent) {
        if (!canUseScreen()) {
            screenOpenTicks = 0;
            hasDumpedItems = false;
            transferredItems = 0;
            return;
        }

        screenOpenTicks++;

        if (screenOpenTicks <= delay.get()) {
            return;
        }

        int r = 0;

        boolean playerHasDumpItems = false;

        for (int i = SlotUtils.indexToId(SlotUtils.MAIN_START); i < SlotUtils.indexToId(SlotUtils.MAIN_START) + 4 * 9; i++) {
            if (mc.player.containerMenu.getSlot(i).hasItem() && items.get().contains(mc.player.containerMenu.getSlot(i).getItem().getItem())) {
                playerHasDumpItems = true;
                break;
            }
        }

        if (!playerHasDumpItems) {
            if (hasDumpedItems && autoClose.get()) {
                if (containerInfo.get()) {
                    sendInfo();
                }

                mc.player.closeContainer();
            }
            return;
        }

        boolean containerHasEmptySlots = false;

        for (int i = 0; i < SlotUtils.indexToId(SlotUtils.MAIN_START); i++) {
            if (!mc.player.containerMenu.getSlot(i).hasItem()) {
                containerHasEmptySlots = true;
                break;
            }
        }

        if (!containerHasEmptySlots) {
            if (hasDumpedItems && autoClose.get()) {
                if (containerInfo.get()) {
                    sendInfo();
                }

                mc.player.closeContainer();
            }
            return;
        }

        for (int i = SlotUtils.indexToId(SlotUtils.MAIN_START); i < SlotUtils.indexToId(SlotUtils.MAIN_START) + 4 * 9; i++) {
            if (r >= rate.get()) break;
            if (!mc.player.containerMenu.getSlot(i).hasItem()) continue;
            if (!items.get().contains(mc.player.containerMenu.getSlot(i).getItem().getItem())) continue;

            hasDumpedItems = true;
            r++;
            transferredItems++;
            InvUtils.shiftClick().slotId(i);
        }
    }

    public void sendInfo() {
        int itemsInContainer = 0;
        for (int i = 0; i < SlotUtils.indexToId(SlotUtils.MAIN_START); i++) {
            if (mc.player.containerMenu.getSlot(i).hasItem()) {
                itemsInContainer++;
            }
        }

        MutableComponent component = Component.empty();
        component.append(TextUtils.getModuleNameFormat(this));
        component.append(Component.literal(" (ATTEMPT) Transferred: ").withStyle(TextUtils.MODULE_INFO_STYLE));
        component.append(Component.literal(String.valueOf(transferredItems)).withStyle(TextUtils.MODULE_INFO_SUB_STYLE));
        component.append(Component.literal(" - Items In Container: ").withStyle(TextUtils.MODULE_INFO_STYLE));
        component.append(Component.literal(itemsInContainer + "/" + SlotUtils.indexToId(SlotUtils.MAIN_START)).withStyle(TextUtils.MODULE_INFO_SUB_STYLE));


        ((IChatHud) mc.gui.getChat()).meteor$add(component, 0);
    }

    public boolean canUseScreen() {
        try {
            return mc.player != null && screens.get().contains(mc.player.containerMenu.getType());
        } catch (Exception e) {
            return false;
        }
    }
}
