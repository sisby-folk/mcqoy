package dev.sisby.mcqoy.controller;

import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.StateManager;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class MapOptionEntryImpl<T> implements MapOptionEntry<T> {
    private final MapOptionImpl<T> group;

    private String key;
    private T value;

    private final Binding<T> binding;
    private final Controller<T> controller;

    MapOptionEntryImpl(MapOptionImpl<T> group, Map.Entry<String, T> initialValue, @NotNull Function<MapOptionEntry<T>, Controller<T>> controlGetter) {
        this.group = group;
        this.key = initialValue.getKey();
        this.value = initialValue.getValue();
        this.binding = new EntryBinding();
        this.controller = new EntryController<>(controlGetter.apply(new HiddenNameMapOptionEntry<>(this)), this);
    }

    @Override
    public @NotNull Text name() {
        return group.name();
    }

    @Override
    public @NotNull OptionDescription description() {
        return group.description();
    }

    @Override
    public @NotNull Text tooltip() {
        return group.tooltip();
    }

    @Override
    public @NotNull Controller<T> controller() {
        return controller;
    }

    @Override
    public @NotNull StateManager<T> stateManager() {
        throw new UnsupportedOperationException("MapOptionEntryImpl does not support state managers");
    }

    @Override
    public @NotNull Binding<T> binding() {
        return binding;
    }

    @Override
    public boolean available() {
        return parentGroup().available();
    }

    @Override
    public void setAvailable(boolean available) {

    }

    @Override
    public MapOption<T> parentGroup() {
        return group;
    }

    @Override
    public boolean changed() {
        return false;
    }

    @Override
    public @NotNull T pendingValue() {
        return value;
    }

    @Override
    public @NotNull Map.Entry<String, T> pendingEntry() {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    @Override
    public void requestSet(@NotNull T value) {
        binding.setValue(value);
    }

    @Override
    public boolean applyValue() {
        return false;
    }

    @Override
    public void forgetPendingValue() {

    }

    @Override
    public void requestSetDefault() {

    }

    @Override
    public boolean isPendingValueDefault() {
        return false;
    }

    @Override
    public boolean canResetToDefault() {
        return false;
    }

    @Override
    public void addEventListener(OptionEventListener<T> listener) {

    }

    @Override
    public void addListener(BiConsumer<Option<T>, T> changedListener) {

    }

    /**
     * Open in case mods need to find the real controller type.
     */
    public record EntryController<T>(Controller<T> controller, MapOptionEntryImpl<T> entry) implements Controller<T> {
        @Override
        public Option<T> option() {
            return controller.option();
        }

        @Override
        public Text formatValue() {
            return controller.formatValue();
        }

        @Override
        public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
            return new MapEntryWidget(screen, entry, controller.provideWidget(screen, widgetDimension));
        }
    }

    private class EntryBinding implements Binding<T> {
        @Override
        public void setValue(T newValue) {
            value = newValue;
            group.triggerListener(OptionEventListener.Event.OTHER, true);
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public T defaultValue() {
            throw new UnsupportedOperationException();
        }
    }
}
