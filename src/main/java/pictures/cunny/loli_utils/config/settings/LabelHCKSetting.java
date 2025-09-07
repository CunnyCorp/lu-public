package pictures.cunny.loli_utils.config.settings;

import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.CompoundTag;
import pictures.cunny.loli_utils.utility.render.SpecialEffects;
import pictures.cunny.loli_utils.utility.render.TextState;

public class LabelHCKSetting extends Setting<TextState> {
    public final Callback dynamicTextState;
    public final TextState textState = new TextState();

    public LabelHCKSetting(Callback callable) {
        super("", "", new TextState(), (s) -> {
        }, (s) -> {
        }, () -> true);
        this.dynamicTextState = callable;
        SpecialEffects.LABEL_HACK.add(this);
    }

    @Override
    public TextState get() {
        this.dynamicTextState.updateStates(this);

        return textState;
    }

    @Override
    protected TextState parseImpl(String str) {
        return TextState.EMPTY;
    }

    @Override
    protected boolean isValueValid(TextState value) {
        return true;
    }

    @Override
    protected CompoundTag save(CompoundTag tag) {
        return tag;
    }

    @Override
    protected TextState load(CompoundTag tag) {
        return TextState.EMPTY;
    }

    @FunctionalInterface
    public interface Callback {
        void updateStates(LabelHCKSetting setting);
    }
}
