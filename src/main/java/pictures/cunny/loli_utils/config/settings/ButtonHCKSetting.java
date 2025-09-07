package pictures.cunny.loli_utils.config.settings;

import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.CompoundTag;
import pictures.cunny.loli_utils.utility.render.TextState;

public class ButtonHCKSetting extends Setting<Boolean> {
    public final Runnable runnable;

    public ButtonHCKSetting(String name, Runnable callable) {
        super(name, "", true, (s) -> {
        }, (s) -> {
        }, () -> true);
        this.runnable = callable;
    }

    @Override
    public Boolean get() {
        return true;
    }

    @Override
    protected Boolean parseImpl(String str) {
        return true;
    }

    @Override
    protected boolean isValueValid(Boolean value) {
        return false;
    }

    @Override
    protected CompoundTag save(CompoundTag tag) {
        return tag;
    }

    @Override
    protected Boolean load(CompoundTag tag) {
        return true;
    }
}
