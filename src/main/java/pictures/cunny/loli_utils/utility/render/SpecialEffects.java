package pictures.cunny.loli_utils.utility.render;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.screens.ModuleScreen;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import pictures.cunny.loli_utils.LoliUtilsMeteor;
import pictures.cunny.loli_utils.config.LoliConfig;
import pictures.cunny.loli_utils.config.settings.LabelHCKSetting;
import pictures.cunny.loli_utils.config.settings.LabelListHCKSetting;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SpecialEffects {
    // NON-INCLUSIVE
    public static final int UPDATE_THRESHOLD = 3;
    public static final List<LabelListHCKSetting> LABEL_LIST_HACK = new ArrayList<>();
    public static final List<LabelHCKSetting> LABEL_HACK = new ArrayList<>();
    public static final Map<String, String> NAME_TO_UUID = new HashMap<>();
    public static final Map<String, Long> NAME_CACHED_TIME = new HashMap<>();

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(new SpecialEffects());
        LoliConfig.get();
    }

    @EventHandler
    public void onTickPre(TickEvent.Pre event) {
        if (mc.screen instanceof ModuleScreen) {
            for (LabelListHCKSetting setting : new ArrayList<>(LABEL_LIST_HACK)) {
                if (setting.isVisible()) {
                    setting.dynamicTextState.updateStates(setting);
                }
            }

            for (LabelHCKSetting setting : new ArrayList<>(LABEL_HACK)) {
                if (setting.isVisible()) {
                    setting.dynamicTextState.updateStates(setting);
                }
            }
        }

        if (mc.player == null) {
            return;
        }

        updateCache();

        for (Map.Entry<String, Long> entry : Map.copyOf(NAME_CACHED_TIME).entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() > 15000) {
                NAME_TO_UUID.remove(entry.getKey());
                NAME_CACHED_TIME.remove(entry.getKey());
            }
        }
    }

    public static void updateCache() {
        if (mc.player == null) {
            return;
        }

        for (PlayerInfo playerListEntry : mc.player.connection.getOnlinePlayers()) {
            NAME_TO_UUID.put(playerListEntry.getProfile().getName().toLowerCase(), playerListEntry.getProfile().getId().toString());
            NAME_CACHED_TIME.put(playerListEntry.getProfile().getName().toLowerCase(), System.currentTimeMillis());
        }
    }

    /*@EventHandler
    public void onReceiveMessage(ReceiveMessageEvent event) {
        updateCache();

        int i = 0;
        for (Component component : event.getMessage().getSiblings()) {
            if (NAME_TO_UUID.containsKey(component.getString())) {
                if (hasColor(NAME_TO_UUID.get(component.getString()))) {
                    event.getMessage().getSiblings().set(i, styleName(component.getString(), NAME_TO_UUID.get(component.getString())));
                }
            } else {
                LoliUtilsMeteor.LOGGER.info("Component string: {}", component.getString());
            }
            i++;
        }
    }*/

    public static boolean hasColor(Player entity) {
        return LoliConfig.get().playerEffects.containsKey(entity.getStringUUID());
    }

    public static boolean hasColor(String uuid) {
        return LoliConfig.get().playerEffects.containsKey(uuid);
    }

    public static Color getColor(Player player) {
        return LoliConfig.get().playerEffects.get(player.getStringUUID()).color;
    }

    public static Component styleName(String name, String uuid) {
        PlayerEffects playerEffects = LoliConfig.get().playerEffects.get(uuid);

        MutableComponent nameText = Component.empty();

        nameText.setStyle(nameText.getStyle().withBold(playerEffects.bold).withUnderlined(playerEffects.underline).withItalic(playerEffects.italic).withColor(playerEffects.color.getPacked()));

        nameText.append(name);

        return nameText;
    }

    public static void ensureProfileExists(String uuid) {
        LoliConfig.get().playerEffects.putIfAbsent(uuid, new PlayerEffects(Color.LIGHT_GRAY, false, false, false));
    }
}
