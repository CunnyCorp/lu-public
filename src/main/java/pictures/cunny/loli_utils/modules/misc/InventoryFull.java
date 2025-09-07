package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.ItemStack;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

public class InventoryFull extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<Integer> fullTime =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("full-time")
                            .description("How long the inventory has to be full for in seconds.")
                            .defaultValue(3)
                            .sliderRange(1, 30)
                            .build());
    public final Setting<Integer> cooldown =
            sgDefault.add(
                    new IntSetting.Builder()
                            .name("cooldown")
                            .description("How long between running commands in seconds.")
                            .defaultValue(30)
                            .sliderRange(1, 120)
                            .build());
    public final Setting<Boolean> fullEChest =
            sgDefault.add(
                    new BoolSetting.Builder()
                            .name("full-echest")
                            .description("Requires a full EChest as well.")
                            .defaultValue(false)
                            .build());
    public final Setting<String> command =
            sgDefault.add(
                    new StringSetting.Builder()
                            .name("command")
                            .description("The command to run.")
                            .defaultValue("sellall")
                            .build());

    private int ticksFull = 0;
    private int ticks = 0;

    public InventoryFull() {
        super(LoliUtilsMeteor.CATEGORY, "inventory-full", "Automatically runs a command when inventory is full.");
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (ticks > 0) {
            ticks--;
            return;
        }

        if (InventoryUtils.isInventoryFull()) {
            if (fullEChest.get()) {
                int items = 0;

                for (ItemStack stack : EChestMemory.ITEMS) {
                    if (stack != ItemStack.EMPTY) {
                        items++;
                    }
                }

                if (items != 27) {
                    return;
                }
            }

            if (ticksFull == 0) {
                ticksFull = fullTime.get() * 20;
            }

            ticksFull--;

            if (ticksFull == 0) {
                PacketUtils.command(command.get());
                ticks = cooldown.get() * 20;
            }
        } else {
            ticksFull = 0;
        }
    }
}
