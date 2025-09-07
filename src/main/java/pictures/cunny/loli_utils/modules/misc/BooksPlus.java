package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.world.item.Items;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BooksPlus extends Module {
    private static final Random random = new Random(System.currentTimeMillis());
    // Randomizer
    private final SettingGroup sgRandomization = settings.createGroup("Randomization");
    public final Setting<Boolean> bot =
            sgRandomization.add(
                    new BoolSetting.Builder()
                            .name("bot")
                            .description("Whether or not to use the bot.")
                            .build());
    public final Setting<Boolean> randomize =
            sgRandomization.add(
                    new BoolSetting.Builder()
                            .name("randomize")
                            .description("Whether or not to use the randomization.")
                            .build());
    public final Setting<String> text =
            sgRandomization.add(
                    new StringSetting.Builder()
                            .name("text")
                            .description("What to write in pages.")
                            .defaultValue("")
                            .build());
    public final Setting<Boolean> uniChars =
            sgRandomization.add(
                    new BoolSetting.Builder()
                            .name("unicode")
                            .description("Whether or not to use unicode.")
                            .build());
    public final Setting<Integer> pageCount =
            sgRandomization.add(
                    new IntSetting.Builder()
                            .name("pages")
                            .description("The amount of pages to generate.")
                            .defaultValue(30)
                            .sliderMin(0)
                            .sliderMax(2000)
                            .build());
    public final Setting<Boolean> randomPages =
            sgRandomization.add(
                    new BoolSetting.Builder()
                            .name("random-pages")
                            .description("Whether or not to generate random pages.")
                            .build());
    public final Setting<Integer> pageLength =
            sgRandomization.add(
                    new IntSetting.Builder()
                            .name("page-length")
                            .description("The length of pages to generate.")
                            .visible(randomPages::get)
                            .defaultValue(255)
                            .sliderMin(0)
                            .sliderMax(10000)
                            .build());
    public final Setting<Boolean> randomTitle =
            sgRandomization.add(
                    new BoolSetting.Builder()
                            .name("random-title")
                            .description("Whether or not to randomize the title.")
                            .build());
    public final Setting<Integer> titleLength =
            sgRandomization.add(
                    new IntSetting.Builder()
                            .name("title-length")
                            .description("The length of the random title.")
                            .visible(randomTitle::get)
                            .defaultValue(1)
                            .sliderMin(0)
                            .sliderMax(32767)
                            .build());
    private int stepCounter = -1;
    private FindItemResult result;

    public BooksPlus() {
        super(LoliUtilsMeteor.CATEGORY, "book+", "A book modifier.");
    }

    public static String randomText(int amount, boolean uni) {
        StringBuilder str = new StringBuilder();
        int leftLimit = 48;
        int rightLimit = 122;

        if (uni) {
            leftLimit = 100000;
            rightLimit = 10000000;
        }

        for (int i = 0; i < amount; i++) {
            str.append((char) (leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1))));
        }
        return str.toString();
    }

    @Override
    public void onActivate() {
        stepCounter = -1;
        result = null;
    }

    @Override
    public void onDeactivate() {
        this.onActivate();
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundEditBookPacket packet && randomize.get()) {
            LoliUtilsMeteor.LOGGER.info("Writing book in slot {}", packet.slot());
            String titleText = packet.title().isPresent() ? packet.title().get() : "";
            List<String> pages = new ArrayList<>();

            if (randomTitle.get()) {
                if (titleLength.get() == 0) titleText = "";
                else titleText = randomText(titleLength.get(), true);
            }

            if (randomPages.get()) {
                for (int i = 0; i < pageCount.get(); i++) {
                    pages.add(randomText(pageLength.get(), uniChars.get()));
                }
            } else {
                for (int i = 0; i < pageCount.get(); i++) {
                    pages.add(text.get());
                }
            }
            ServerboundEditBookPacket packet1 =
                    new ServerboundEditBookPacket(packet.slot(), pages, Optional.of(titleText));
            PacketUtils.send(packet1);
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.gameMode == null || !bot.get()) return;

        if (stepCounter > 0) {
            stepCounter++;
            processStep();
            return;
        }

        result = InvUtils.find(itemStack -> itemStack.getItem() == Items.WRITABLE_BOOK);

        if (!result.found()) return;

        stepCounter = 1;
        processStep();
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
            case 25:
                assert false;

                String titleText = "";
                List<String> pages = new ArrayList<>();

                if (randomTitle.get()) {
                    if (titleLength.get() == 0) titleText = "";
                    else titleText = randomText(titleLength.get(), true);
                }

                if (randomPages.get()) {
                    for (int i = 0; i < pageCount.get(); i++) {
                        pages.add(randomText(pageLength.get(), uniChars.get()));
                    }
                } else {
                    for (int i = 0; i < pageCount.get(); i++) {
                        pages.add(text.get());
                    }
                }
                ServerboundEditBookPacket packet1 =
                        new ServerboundEditBookPacket(
                                mc.player.getInventory().getSelectedSlot(), pages, Optional.of(titleText));
                PacketUtils.send(packet1);
                break;
            case 45:
                assert false;
                mc.player.drop(true);
                break;
            case 50:
                stepCounter = -1;
        }
    }
}
