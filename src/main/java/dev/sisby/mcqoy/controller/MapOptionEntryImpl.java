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

    private final Binding<Map.Entry<String, T>> binding;
    private final Controller<Map.Entry<String, T>> controller;

    MapOptionEntryImpl(MapOptionImpl<T> group, Map.Entry<String, T> initialValue, @NotNull Function<MapOptionEntry<T>, Controller<String>> keyController, @NotNull Function<MapOptionEntry<T>, Controller<T>> valueController) {
        this.group = group;
        this.key = initialValue.getKey();
        this.value = initialValue.getValue();
        this.binding = new EntryBinding(initialValue);
        this.controller = new EntryController<>(keyController.apply(this), valueController.apply(this), this);
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
    public @NotNull Controller<Map.Entry<String, T>> controller() {
        return controller;
    }

    @Override
    public @NotNull StateManager<Map.Entry<String, T>> stateManager() {
        throw new UnsupportedOperationException("MapOptionEntryImpl does not support state managers");
    }

    @Override
    public @NotNull Binding<Map.Entry<String, T>> binding() {
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
    public @NotNull Map.Entry<String, T> pendingValue() {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    @Override
    public void requestSet(@NotNull Map.Entry<String, T> value) {
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
    public void addEventListener(OptionEventListener<Map.Entry<String, T>> listener) {

    }

    @Override
    public void addListener(BiConsumer<Option<Map.Entry<String, T>>, Map.Entry<String, T>> changedListener) {

    }

    /**
     * Open in case mods need to find the real controller type.
     */
    public record EntryController<T>(Controller<String> keyController, Controller<T> valueController, MapOptionEntryImpl<T> entry) implements Controller<Map.Entry<String, T>> {
        @Override
        public Option<Map.Entry<String, T>> option() {
            return entry;
        }

        @Override
        public Text formatValue() {
            return valueController.formatValue().copy().append(keyController.formatValue());
        }

        @Override
        public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
            return new MapEntryWidget(screen, entry, valueController.provideWidget(screen, widgetDimension), keyController.provideWidget(screen, widgetDimension));
        }
    }

    private class EntryBinding implements Binding<Map.Entry<String, T>> {
        private final Map.Entry<String, T> initialValue;

        public EntryBinding(Map.Entry<String, T> initialValue) {
            this.initialValue = initialValue;
        }

        @Override
        public void setValue(Map.Entry<String, T> newEntry) {
            key = newEntry.getKey();
            value = newEntry.getValue();
            group.triggerListener(OptionEventListener.Event.OTHER, true);
        }

        @Override
        public Map.Entry<String, T> getValue() {
            return new AbstractMap.SimpleEntry<>(key, value);
        }

        @Override
        public Map.Entry<String, T> defaultValue() {
            return initialValue;
        }
    }
}
