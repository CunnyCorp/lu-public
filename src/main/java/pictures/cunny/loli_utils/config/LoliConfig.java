package pictures.cunny.loli_utils.config;

import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.nbt.CompoundTag;
import pictures.cunny.loli_utils.utility.render.PlayerEffects;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoliConfig extends System<LoliConfig> {
    public Map<String, PlayerEffects> playerEffects = new HashMap<>();

    public LoliConfig() {
        super("loli");
    }

    public static LoliConfig get() {
        return Systems.get(LoliConfig.class);
    }

    @Override
    public void save(File folder) {
        super.save(folder);
    }

    @Override
    public LoliConfig fromTag(CompoundTag tag) {
        if (tag.contains("playerEffects", CompoundTag.TAG_COMPOUND)) {
            CompoundTag playerEffectsTag = tag.getCompound("playerEffects");
            for (String name : playerEffectsTag.getAllKeys()) {
                CompoundTag playerTag = playerEffectsTag.getCompound(name);
                playerEffects.put(name, new PlayerEffects(new Color().fromTag(playerTag.getCompound("color")), playerTag.getBoolean("bold"), playerTag.getBoolean("underline"), playerTag.getBoolean("italic")));

            }
        }

        return this;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        CompoundTag playerEffectsTag = new CompoundTag();

        for (Map.Entry<String, PlayerEffects> entry : playerEffects.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.put("color", entry.getValue().color.toTag());
            playerTag.putBoolean("bold", entry.getValue().bold);
            playerTag.putBoolean("underline", entry.getValue().underline);
            playerTag.putBoolean("italic", entry.getValue().italic);

            playerEffectsTag.put(entry.getKey(), playerTag);
        }

        tag.put("playerEffects", playerEffectsTag);

        return tag;
    }
}
