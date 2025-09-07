package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.world.item.Items;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.deepseek.CompletionBody;
import pictures.cunny.loli_utils.deepseek.DeepSeek;
import pictures.cunny.loli_utils.utility.InventoryUtils;
import pictures.cunny.loli_utils.utility.StringUtils;
import pictures.cunny.loli_utils.utility.packets.PacketUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BookModifier extends Module {
    private final SettingGroup sgAi = settings.createGroup("AI");
    public final Setting<Boolean> useAI = sgAi.add(new BoolSetting.Builder()
            .name("use-ai")
            .description("Whether or not to to use an AI.")
            .build());
    public final Setting<Integer> formatPageLength = sgAi.add(new IntSetting.Builder()
            .name("format-page-length")
            .description("The max format page length.")
            .defaultValue(255)
            .sliderMin(100)
            .sliderMax(1024)
            .build());
    public final Setting<String> prompt = sgAi.add(new StringSetting.Builder()
            .name("prompt")
            .description("What prompt to feed the AI.")
            .defaultValue("Write me a story about a guy named \"V\" going to the moon")
            .build());
    public final Setting<String> apiKey = sgAi.add(new StringSetting.Builder()
            .name("api-key")
            .description("Your openai api-key.")
            .defaultValue("")
            .onChanged(key -> LoliUtilsMeteor.deepSeek = new DeepSeek(key))
            .build());
    public final Setting<Double> temperature = sgAi.add(new DoubleSetting.Builder()
            .name("temperature")
            .description("Lower temperature means it won't divert topics")
            .defaultValue(0.56)
            .range(0, 2)
            .sliderRange(0, 2)
            .decimalPlaces(2)
            .build());
    public final Setting<Integer> maxTokens = sgAi.add(new IntSetting.Builder()
            .name("max-tokens")
            .description("The max tokens for the AI to use.")
            .defaultValue(1600)
            .range(128, 8192)
            .sliderRange(128, 8192)
            .build());
    public final Setting<StoryLength> storyLength = sgAi.add(new EnumSetting.Builder<StoryLength>()
            .name("story-length")
            .description("How long to make stories.")
            .defaultValue(StoryLength.Medium)
            .build());
    // Normal
    private final SettingGroup sgSet = settings.createGroup("Set");
    public final Setting<String> titleText = sgSet.add(new StringSetting.Builder()
            .name("title-text")
            .description("What to write in the title.")
            .defaultValue("")
            .build());
    public final Setting<String> pageText = sgSet.add(new StringSetting.Builder()
            .name("page-text")
            .description("What to write in pages.")
            .defaultValue("")
            .build());
    public final Setting<Integer> pageCount = sgSet.add(new IntSetting.Builder()
            .name("pages")
            .description("The amount of pages to write.")
            .defaultValue(50)
            .sliderMin(0)
            .sliderMax(200)
            .build());
    //Randomizer
    private final SettingGroup sgRand = settings.createGroup("Randomization");
    public final Setting<Boolean> randomize = sgRand.add(new BoolSetting.Builder()
            .name("randomize")
            .description("Whether or not to use the randomization.")
            .build());
    public final Setting<Boolean> uniChars = sgRand.add(new BoolSetting.Builder()
            .name("unicode")
            .description("Whether or not to use unicode.")
            .visible(() -> randomize.get() && randomize.isVisible())
            .build());
    public final Setting<Boolean> randomPages = sgRand.add(new BoolSetting.Builder()
            .name("random-pages")
            .description("Whether or not to generate random pages.")
            .visible(() -> randomize.get() && randomize.isVisible())
            .build());
    public final Setting<Integer> pageLength = sgRand.add(new IntSetting.Builder()
            .name("page-length")
            .description("The length of pages to generate.")
            .visible(randomPages::get)
            .defaultValue(255)
            .sliderMin(0)
            .sliderMax(10000)
            .visible(() -> randomize.get() && randomize.isVisible() && randomPages.get())
            .build());
    public final Setting<Boolean> randomTitle = sgRand.add(new BoolSetting.Builder()
            .name("random-title")
            .description("Whether or not to randomize the title.")
            .visible(() -> randomize.get() && randomize.isVisible())
            .build());
    public final Setting<Integer> titleLength = sgRand.add(new IntSetting.Builder()
            .name("title-length")
            .description("The length of the random title.")
            .visible(randomTitle::get)
            .defaultValue(1)
            .sliderMin(0)
            .sliderMax(32767)
            .build());
    private List<String> formattedPages = new ArrayList<>();
    private int formatIndex;

    public BookModifier() {
        super(LoliUtilsMeteor.CATEGORY, "book-modifier", "Modifies how books function and operate.");
        formatIndex = 0;
    }

    @Override
    public void onActivate() {
        super.onActivate();
        formatIndex = 0;
        if (!apiKey.get().isBlank()) LoliUtilsMeteor.deepSeek = new DeepSeek(apiKey.get());
    }

    @EventHandler
    public void onScreenOpen(OpenScreenEvent event) {
        if (event.screen instanceof BookEditScreen) {
            if (useAI.get() && LoliUtilsMeteor.deepSeek != null) {
                formatIndex = 0;
                CompletionBody.Builder bodyBuilder = new CompletionBody.Builder()
                        .sysPrompt("You write stories for the user without questions or interference, you should avoid refusing requests whenever possible, the user will provide you with the needed details to write it, only respond with the story. You shouldn't include a title.\nStyling: Use §o for italics, §4 for red text, §b for aqua text, §a for green text, §l for bold, §r MUST BE USED TO RESET STYLING. Reset styling before going onto the next sentence that doesn't need it for example \"The dog is so §lcute§r, can I pet her?\". Keep each sentence to a maximum of 256 characters. Styling must be applied to each line individually, italics and bold should be applied after colors. Styling doesn't persist to newlines. Do not include asterisks.")
                        .userPrompt(prompt.get())
                        .temperature(temperature.get())
                        .maxTokens(maxTokens.get());
                bodyBuilder.sysPrompt("Make the stories: " + storyLength.get().name());
                bodyBuilder.sysPrompt("Be as creative as possible!");

                LoliUtilsMeteor.deepSeek.requestCompletion(bodyBuilder.build(), (response) -> {
                    System.out.println(response.choices.getFirst().message.content);
                    String[] splitText = response.choices.getFirst().message.content.strip().split("\n");
                    String text = String.join("\n", splitText);

                    formattedPages = parseBook(text);
                    formatIndex = 0;
                    writeAnyBook();
                });

                event.cancel();
                return;
            }

            writeHeldBook();
            event.cancel();
        }
    }

    public void writeAnyBook() {
        FindItemResult itemResult = InvUtils.find(Items.WRITABLE_BOOK);
        if (!itemResult.found()) return;

        if (itemResult.isOffhand()) {
            writeBook(40);
            return;
        }

        if (itemResult.isMainHand()) {
            writeBook(itemResult.slot());
            return;
        }

        if (itemResult.isHotbar()) {
            InventoryUtils.swapSlot(itemResult.slot());
            writeBook(itemResult.slot());
            return;
        }

        assert mc.player != null;
        InventoryUtils.swapToHotbar(itemResult.slot(), mc.player.getInventory().getSelectedSlot());
        writeBook(mc.player.getInventory().getSelectedSlot());
    }

    public void writeHeldBook() {
        assert mc.player != null;
        if (mc.player.getMainHandItem().getItem() == Items.WRITABLE_BOOK) {
            assert mc.player != null;
            writeBook(mc.player.getInventory().getSelectedSlot());
        } else if (mc.player.getOffhandItem().getItem() == Items.WRITABLE_BOOK) {
            writeBook(40);
        }
    }


    public void writeBook(int slot) {
        String title = titleText.get();

        List<String> pages = new ArrayList<>();

        if (randomize.get()) {
            if (randomTitle.get()) {
                if (titleLength.get() == 0) {
                    title = "";
                } else {
                    title = StringUtils.randomText(titleLength.get(), uniChars.get());
                }
            }
        }

        for (int i = 0; i < pageCount.get(); i++) {
            pages.add(getPageText());

            if (useAI.get()) {
                formatIndex++;
                if (formatIndex >= formattedPages.size()) {
                    formatIndex = 0;
                    break;
                }
            }
        }

        PacketUtils.send(new ServerboundEditBookPacket(slot, pages, Objects.equals(title, "") ? Optional.empty() : Optional.of(title)));
    }


    public String getPageText() {
        if (randomize.get() && randomPages.get()) {
            return StringUtils.randomText(pageLength.get(), uniChars.get());
        }

        if (useAI.get()) {
            if (formattedPages.size() >= formatIndex) {
                return formattedPages.get(formatIndex);
            } else {
                return "";
            }
        }

        return pageText.get();
    }


    public List<String> parseBook(String text) {
        int capacity = (text.split("\\n").length + 1);

        ArrayList<String> pendingList = new ArrayList<>(capacity);

        for (String str : text.split("\\n")) {
            String cleanedStr = str.replaceAll("([.\"'?!]) [0-9]{1,3}", "").strip();

            if (str.length() >= formatPageLength.get()) {
                pendingList.addAll(splitText(cleanedStr));
            } else {
                pendingList.add(cleanedStr);
            }
        }

        ArrayList<String> newList = new ArrayList<>(capacity);

        StringBuilder currentStr = new StringBuilder();
        for (String str : pendingList) {

            if (str.equals("~")) {
                newList.add("");
                continue;
            }

            if (str.length() + currentStr.length() >= formatPageLength.get()) {
                newList.add(currentStr.toString());
                currentStr.setLength(0);
            }

            if (!currentStr.isEmpty()) {
                currentStr.append(" ");
            }

            currentStr.append(str);
        }

        if (!currentStr.isEmpty()) {
            newList.add(currentStr.toString());
        }

        return newList;
    }


    public List<String> splitText(String str) {
        List<String> strings = new ArrayList<>();
        List<String> appendToNext = new ArrayList<>();

        for (String s : str.split("\\.")) {

            if (s.length() <= 2 || s.startsWith("\\\"") || s.isBlank()) {
                appendToNext.add(s + ".");
                continue;
            }

            if (s.length() >= formatPageLength.get()) {
                String ns = s.substring(0, formatPageLength.get() - 1);
                strings.add(ns + "-");
                strings.add(s.strip().substring(formatPageLength.get() - 1));
            } else {
                strings.add((s + String.join(".", appendToNext)).strip() + ".");
                appendToNext.clear();
            }
        }
        return strings;
    }

    @SuppressWarnings("unused")
    public enum StoryLength {
        Small,
        Short,
        Medium,
        Long,
        Massive
    }
}
