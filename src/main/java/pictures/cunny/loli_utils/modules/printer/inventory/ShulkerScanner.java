package pictures.cunny.loli_utils.modules.printer.inventory;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShulkerScanner {
    public static List<SimpleQuantity> getQuantities(ItemStack stack) {
        if (!stack.has(DataComponents.CONTAINER)) {
            return new ArrayList<>();
        }

        Iterable<ItemStack> stacks = stack.get(DataComponents.CONTAINER).nonEmptyItems();

        Map<Item, Integer> quantities = new HashMap<>();

        stacks.forEach(stack1 -> {
            if (!stack.isEmpty()) {
                quantities.put(stack1.getItem(), quantities.getOrDefault(stack1.getItem(), 0) + 1);
            }
        });

        List<SimpleQuantity> simpleQuantities = new ArrayList<>();

        quantities.forEach((item, count) -> {
            simpleQuantities.add(new SimpleQuantity(item, count));
        });

        return simpleQuantities;
    }

    public static List<SimpleQuantity> getCurrentQuantities() {
        if (MeteorClient.mc.player == null) {
            return new ArrayList<>();
        }

        List<SimpleQuantity> quantities = new ArrayList<>();

        for (ItemStack stack : MeteorClient.mc.player.containerMenu.getItems()) {
            if (stack.has(DataComponents.CONTAINER)) {
                quantities.addAll(getQuantities(stack));
            } else {
                quantities.add(new SimpleQuantity(stack.getItem(), stack.getCount()));
            }
        }

        return quantities;
    }
}
