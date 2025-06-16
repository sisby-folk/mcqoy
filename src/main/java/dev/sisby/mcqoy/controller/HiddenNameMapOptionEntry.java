package dev.sisby.mcqoy.controller;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.StateManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;

public class HiddenNameMapOptionEntry<T> implements MapOptionEntry<T> {
    private final MapOptionEntry<T> option;

    public HiddenNameMapOptionEntry(MapOptionEntry<T> option) {
        this.option = option;
    }

    @Override
    public @NotNull Text name() {
        return Text.empty();
    }

    @Override
    public @NotNull OptionDescription description() {
        return option.description();
    }

    @Override
    @Deprecated
    public @NotNull Text tooltip() {
        return option.tooltip();
    }

    @Override
    public @NotNull StateManager<T> stateManager() {
        return option.stateManager();
    }

    @Override
    public @NotNull Controller<T> controller() {
        return option.controller();
    }

    @Override
    public @NotNull Binding<T> binding() {
        return option.binding();
    }

    @Override
    public boolean available() {
        return option.available();
    }

	@Override
    public void setAvailable(boolean available) {
        option.setAvailable(available);
    }

    @Override
    public MapOption<T> parentGroup() {
        return option.parentGroup();
    }

    @Override
    public @NotNull ImmutableSet<OptionFlag> flags() {
        return option.flags();
    }

    @Override
    public boolean changed() {
        return option.changed();
    }

    @Override
    public @NotNull T pendingValue() {
        return option.pendingValue();
    }

	@Override
	public Map.@NotNull Entry<String, T> pendingEntry() {
		return option.pendingEntry();
	}

    @Override
    public void requestSet(@NotNull T value) {
        option.requestSet(value);
    }

    @Override
    public boolean applyValue() {
        return option.applyValue();
    }

    @Override
    public void forgetPendingValue() {
        option.forgetPendingValue();
    }

    @Override
    public void requestSetDefault() {
        option.requestSetDefault();
    }

    @Override
    public boolean isPendingValueDefault() {
        return option.isPendingValueDefault();
    }

    @Override
    public boolean canResetToDefault() {
        return option.canResetToDefault();
    }

    @Override
    @Deprecated
    public void addListener(BiConsumer<Option<T>, T> changedListener) {
        option.addListener(changedListener);
    }

    @Override
    public void addEventListener(OptionEventListener<T> listener) {
        option.addEventListener(listener);
    }
}
