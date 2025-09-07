package pictures.cunny.loli_utils.utility;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.util.Locale.ROOT;

public class StringUtils {
    public static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final List<String> alphabet =
            Arrays.stream("qwertyuiopasdfghjklzxcvbnm1234567890/._-".split("")).toList();

    public static String readable(String original) {
        original = original.toLowerCase();
        StringBuilder newString = new StringBuilder();
        if (original.contains("-") || original.contains("_")) {
            for (String segment : original.split("[-_]")) {
                String first = segment.split("")[0];
                newString.append(segment.replaceFirst(first, first.toUpperCase(ROOT)));
            }
        } else {
            newString = new StringBuilder(original.toLowerCase(ROOT));
            String first = newString.toString().split("")[0];
            newString =
                    new StringBuilder(newString.toString().replaceFirst(first, first.toUpperCase(ROOT)));
        }

        return newString.toString();
    }

    public static boolean toBool(String string) {
        return switch (string) {
            case "true", "yes", "1", "y" -> true;
            default -> false;
        };
    }

    public static String randomText(int amount) {
        return randomText(amount, false);
    }

    public static String randomText(int amount, boolean uni) {
        StringBuilder str = new StringBuilder();

        if (uni) {
            int leftLimit = 123;
            int rightLimit = 20000;

            for (int i = 0; i < amount; i++) {
                str.append((char) (leftLimit + (int) (RANDOM.nextFloat() * (rightLimit - leftLimit + 1))));
            }
        } else {
            for (int i = 0; i < amount; i++) {
                str.append(CollectionUtils.random(alphabet));
            }
        }

        return str.toString();
    }

    public static String randomInt(int amount) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            str.append(RANDOM.nextInt(9));
        }
        return str.toString();
    }
}
