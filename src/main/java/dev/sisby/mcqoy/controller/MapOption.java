package dev.sisby.mcqoy.controller;

import com.google.common.collect.ImmutableList;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.StateManager;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface MapOption<T> extends OptionGroup, Option<Map<String, T>> {
    @Override
    @NotNull ImmutableList<MapOptionEntry<T>> options();

    int numberOfEntries();

    int maximumNumberOfEntries();

    int minimumNumberOfEntries();

    MapOptionEntry<T> insertNewEntry();

    void insertEntry(int index, MapOptionEntry<?> entry);

    int indexOf(MapOptionEntry<?> entry);

    void removeEntry(MapOptionEntry<?> entry);

    void addRefreshListener(Runnable changedListener);

    static <T> MapOption.Builder<T> createBuilder() {
        return new MapOptionImpl.BuilderImpl<>();
    }

    @Deprecated
    static <T> MapOption.Builder<T> createBuilder(Class<T> typeClass) {
        return createBuilder();
    }

    interface Builder<T> {
        /**
         * Sets name of the list, for UX purposes, a name should always be given,
         * but isn't enforced.
         *
         * @see MapOption#name()
         */
        MapOption.Builder<T> name(@NotNull Text name);

        MapOption.Builder<T> description(@NotNull OptionDescription description);

        /**
         * Sets the value that is used when creating new entries
         */
        MapOption.Builder<T> initial(@NotNull Supplier<T> initialValue);

        /**
         * Sets the value that is used when creating new entries
         */
        MapOption.Builder<T> initial(@NotNull T initialValue);

        MapOption.Builder<T> controllers(@NotNull Function<Option<String>, ControllerBuilder<String>> keyController, @NotNull Function<Option<T>, ControllerBuilder<T>> valueController);

        MapOption.Builder<T> state(@NotNull StateManager<Map<String, T>> stateManager);

        /**
         * Sets the binding for the option.
         * Used for default, getter and setter.
         *
         * @see Binding
         */
        MapOption.Builder<T> binding(@NotNull Binding<Map<String, T>> binding);

        /**
         * Sets the binding for the option.
         * Shorthand of {@link Binding#generic(Object, Supplier, Consumer)}
         *
         * @param def default value of the option, used to reset
         * @param getter should return the current value of the option
         * @param setter should set the option to the supplied value
         * @see Binding
         */
        MapOption.Builder<T> binding(@NotNull Map<String, T> def, @NotNull Supplier<@NotNull Map<String, T>> getter, @NotNull Consumer<@NotNull Map<String, T>> setter);

        /**
         * Sets if the option can be configured
         *
         * @see Option#available()
         */
        MapOption.Builder<T> available(boolean available);

        /**
         * Sets a minimum size for the list. Once this size is reached,
         * no further entries may be removed.
         */
        MapOption.Builder<T> minimumNumberOfEntries(int number);

        /**
         * Sets a maximum size for the list. Once this size is reached,
         * no further entries may be added.
         */
        MapOption.Builder<T> maximumNumberOfEntries(int number);

        /**
         * Dictates if new entries should be added to the end of the list
         * rather than the top.
         */
        MapOption.Builder<T> insertEntriesAtEnd(boolean insertAtEnd);

        /**
         * Adds a flag to the option.
         * Upon applying changes, all flags are executed.
         * {@link Option#flags()}
         */
        MapOption.Builder<T> flag(@NotNull OptionFlag... flag);

        /**
         * Adds a flag to the option.
         * Upon applying changes, all flags are executed.
         * {@link Option#flags()}
         */
        MapOption.Builder<T> flags(@NotNull Collection<OptionFlag> flags);

        /**
         * Dictates if the group should be collapsed by default.
         * If not set, it will not be collapsed by default.
         *
         * @see OptionGroup#collapsed()
         */
        MapOption.Builder<T> collapsed(boolean collapsible);

        MapOption.Builder<T> addListener(@NotNull OptionEventListener<Map<String, T>> listener);

        MapOption.Builder<T> addListeners(@NotNull Collection<OptionEventListener<Map<String, T>>> listeners);

        /**
         * Adds a listener to the option. Invoked upon changing any of the list's entries.
         *
         * @see Option#addListener(BiConsumer)
         */
        MapOption.Builder<T> listener(@NotNull BiConsumer<Option<Map<String, T>>, Map<String, T>> listener);

        /**
         * Adds multiple listeners to the option. Invoked upon changing of any of the list's entries.
         *
         * @see Option#addListener(BiConsumer)
         */
        MapOption.Builder<T> listeners(@NotNull Collection<BiConsumer<Option<Map<String, T>>, Map<String, T>>> listeners);

        MapOption<T> build();
    }
}
