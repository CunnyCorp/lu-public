package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoAnimal extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    private final Setting<Animal> animal =
            sgDefault.add(
                    new EnumSetting.Builder<Animal>()
                            .name("animal")
                            .description("The animal you wish to be.")
                            .defaultValue(Animal.Cat)
                            .build());

    private final Map<Animal, List<String>> animalSounds = new HashMap<>();
    private final Pattern pattern = Pattern.compile("[a-zA-Z]+");

    public AutoAnimal() {
        super(LoliUtilsMeteor.CATEGORY, "auto-animal", "Convert messages into animal noises.");
        animalSounds.put(
                Animal.Rabbit, List.of("chirrup", "purr", "purrrrr", "prrr", "grunt", "thump"));
        animalSounds.put(
                Animal.Cat,
                List.of("meow", "mreow", "mrew", "purr", "purrrrr", "prrr", "mew", "rawr", "nya"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(SendMessageEvent event) {
        event.message = convertMessageToSounds(animal.get(), event.message);
    }

    public String convertMessageToSounds(Animal animal, String message) {
        List<String> sounds = animalSounds.get(animal);

        Map<String, String> textToSound = new HashMap<>();

        StringBuilder messageBuilder = new StringBuilder();

        for (String string : message.split(" ")) {
            Matcher matcher = pattern.matcher(string.strip());

            while (matcher.find()) {
                String text = matcher.group();

                textToSound.putIfAbsent(
                        text,
                        getAnimalSound(
                                sounds));

                messageBuilder.append(" ").append(string.replaceFirst(text, textToSound.get(text)));
                LoliUtilsMeteor.LOGGER.info("Converting {} to {}", text, textToSound.get(text));
            }
        }

        return messageBuilder.toString().strip();
    }

    /*private String getAnimalSound(List<String> sounds, double i) {
        i = Math.abs(Math.round(i));
        if (i < 0) {
            return getAnimalSound(sounds, Math.abs(i));
        }

        if (i < sounds.size() - 1) {
            return sounds.get(Math.max(0, (int) i));
        }

        return getAnimalSound(sounds, i / divisionFactor.get());
    }*/

    private String getAnimalSound(List<String> sounds) {
        var val = LoliUtilsMeteor.RANDOM.nextInt(-7, sounds.size() + 6);

        if (val < 0) {
            return getAnimalSound(sounds);
        }

        if (val < sounds.size() - 1) {
            return sounds.get(val);
        }

        return getAnimalSound(sounds);
    }

    public enum Animal {
        Rabbit,
        Cat
    }
}
