package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.events.world.ServerConnectBeginEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.User;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

public class CaseSpoof extends Module {
    private final SettingGroup sgDefault = settings.getDefaultGroup();
    public final Setting<String> text =
            sgDefault.add(
                    new StringSetting.Builder()
                            .name("username")
                            .description(".")
                            .defaultValue("")
                            .build());


    public CaseSpoof() {
        super(LoliUtilsMeteor.CATEGORY, "case-spoof", "Adds back a less intelligent case-spoofing.");
        this.runInMainMenu = true;
    }

    @EventHandler
    public void onServerJoin(ServerConnectBeginEvent event) {
        if (!text.get().equalsIgnoreCase(mc.getUser().getName())) {
            text.set(mc.getUser().getName().toUpperCase());
        }

        Account.setSession(new User(text.get(), mc.getUser().getProfileId(), mc.getUser().getAccessToken(), mc.getUser().getXuid(), mc.getUser().getClientId(), User.Type.MSA));
    }
}
