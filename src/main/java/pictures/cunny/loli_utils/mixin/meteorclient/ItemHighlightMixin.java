package pictures.cunny.loli_utils.mixin.meteorclient;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.render.ItemHighlight;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(value = ItemHighlight.class, remap = false)
public class ItemHighlightMixin {
    @Unique
    public Setting<Boolean> hasIllegalEnchants;
    @Unique
    public Setting<List<Item>> filterItemFromEnchantChecks;
    @Unique
    public Setting<Boolean> isUnbreakable;
    @Unique
    public Setting<Boolean> hasLore;
    @Unique
    public Setting<Boolean> hasCustomData;
    @Unique
    public Setting<List<String>> filterCustomData;
    @Shadow
    @Final
    private SettingGroup sgGeneral;

    @Shadow
    @Final
    private Setting<List<Item>> items;

    @Shadow
    @Final
    private Setting<SettingColor> color;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(CallbackInfo ci) {
        //SettingGroup customGroup = ((ItemHighlight) (Object) this).settings.createGroup("Lolified");

        hasIllegalEnchants =
                sgGeneral.add(
                        new BoolSetting.Builder()
                                .name("illegal-enchants")
                                .description("Highlights items with enchants they shouldn't have.")
                                .defaultValue(false)
                                .build());
        filterItemFromEnchantChecks =
                sgGeneral.add(
                        new ItemListSetting.Builder()
                                .name("enchanted-filter")
                                .description("Hide certain enchanted items from showing up as illegal.")
                                .defaultValue(Items.DIAMOND_SWORD)
                                .build());
        isUnbreakable =
                sgGeneral.add(
                        new BoolSetting.Builder()
                                .name("unbreakable")
                                .description("Highlights unbreakable items.")
                                .defaultValue(false)
                                .build());
        hasLore =
                sgGeneral.add(
                        new BoolSetting.Builder()
                                .name("lore-items")
                                .description("Highlights items with lore on them.")
                                .defaultValue(false)
                                .build());
        hasCustomData =
                sgGeneral.add(
                        new BoolSetting.Builder()
                                .name("custom-data")
                                .description("Highlights items with custom data.")
                                .defaultValue(false)
                                .build());
        filterCustomData =
                sgGeneral.add(
                        new StringListSetting.Builder()
                                .name("custom-data-filter")
                                .description("Ignore certain custom data tags from being listed.")
                                .build());

    }

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    public void getColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        AtomicBoolean highlightStack = new AtomicBoolean(false);

        if (stack.has(DataComponents.CONTAINER)) {
            ItemContainerContents contents = stack.get(DataComponents.CONTAINER);

            if (contents == null) {
                return;
            }

            Iterable<ItemStack> stacks = contents.nonEmptyItems();

            stacks.forEach(stack1 -> {
                if (items.get().contains(stack1.getItem())) {
                    highlightStack.set(true);
                } else if (isIllegal(stack1)) {
                    highlightStack.set(true);
                }
            });
        } else if (isIllegal(stack)) {
            highlightStack.set(true);
        }

        if (highlightStack.get()) {
            cir.setReturnValue(color.get().getPacked());
        }
    }

    @Unique
    private boolean isIllegal(ItemStack stack) {
        // Has lore, very cool.
        if (hasLore.get() && stack.has(DataComponents.LORE)) {
            ItemLore lore = stack.get(DataComponents.LORE);
            if (lore != null && !lore.lines().isEmpty()) {
                return true;
            }
        }

        // Is unbreakable, try it?
        if (isUnbreakable.get() && stack.has(DataComponents.UNBREAKABLE)) {
            return true;
        }

        if (hasCustomData.get() && hasCustomTags(stack)) {
            return true;
        }

        if (!filterItemFromEnchantChecks.get().contains(stack.getItem()) && hasIllegalEnchants.get()) {
            if (stack.has(DataComponents.ENCHANTMENTS)) {
                ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);

                // ????
                if (enchantments != null) {
                    if (!stack.has(DataComponents.ENCHANTABLE) && !enchantments.isEmpty()) {
                        return true;
                    }

                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                        // Enchant is higher than max value.
                        if (enchantments.getLevel(entry.getKey()) > entry.getKey().value().getMaxLevel()) {
                            return true;
                        }

                        // Item has enchants it shouldn't be allowed to have.
                        if (!entry.getKey().value().canEnchant(stack.getItem().getDefaultInstance())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Unique
    private boolean hasCustomTags(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

            if (customData != null) {
                for (String key : customData.copyTag().keySet()) {
                    if (customData.contains(key) && !filterCustomData.get().contains(key)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
