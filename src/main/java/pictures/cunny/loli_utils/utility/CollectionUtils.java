package pictures.cunny.loli_utils.utility;

import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class CollectionUtils {
    private static final Random RANDOM = new Random();

    public static <T> T random(ObjectBigArrayBigList<T> list) {
        if (list.isEmpty()) return null;
        return list.get(RANDOM.nextLong(list.size64()));
    }

    public static <T> T random(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(RANDOM.nextInt(list.size()));
    }

    public static <T> T random(Collection<T> list) {
        if (list.isEmpty()) return null;
        return (T) list.toArray()[RANDOM.nextInt(list.size())];
    }
}
