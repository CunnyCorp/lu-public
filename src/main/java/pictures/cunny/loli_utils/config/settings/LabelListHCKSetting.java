package pictures.cunny.loli_utils.config.settings;

import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.CompoundTag;
import pictures.cunny.loli_utils.utility.render.SpecialEffects;
import pictures.cunny.loli_utils.utility.render.TextState;

import java.util.ArrayList;
import java.util.List;

public class LabelListHCKSetting extends Setting<List<TextState>> {
    public final Callback dynamicTextState;
    private final List<TextState> textStates = new ArrayList<>();

    public LabelListHCKSetting(Callback callable) {
        super("", "", new ArrayList<>(), (s) -> {
        }, (s) -> {
        }, () -> true);
        this.dynamicTextState = callable;
        SpecialEffects.LABEL_LIST_HACK.add(this);
    }

    public TextState getOrCreate(int i) {
        if (i > textStates.size()) {
            throw new ArrayIndexOutOfBoundsException("getOrCreate supplied with (" + i + ") but max is (" + textStates.size() + ")");
        }

        if (i == textStates.size()) {
            textStates.add(new TextState());
        }

        return textStates.get(i);
    }

    @Override
    public List<TextState> get() {
        try {
            this.dynamicTextState.updateStates(this);

            return textStates;
        } catch (Exception ignored) {
        }

        return this.defaultValue;
    }

    @Override
    protected List<TextState> parseImpl(String str) {
        return new ArrayList<>();
    }

    @Override
    protected boolean isValueValid(List<TextState> value) {
        return true;
    }

    @Override
    protected CompoundTag save(CompoundTag tag) {
        return tag;
    }

    @Override
    protected List<TextState> load(CompoundTag tag) {
        return new ArrayList<>();
    }

    @FunctionalInterface
    public interface Callback {
        void updateStates(LabelListHCKSetting setting);
    }
}
