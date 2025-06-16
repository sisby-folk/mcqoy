package dev.sisby.mcqoy.controller;

import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.StateManager;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.impl.ProvidesBindingForDeprecation;
import net.minecraft.text.Text;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MapValueOption<T> implements Option<T> {
	private final Option<Map.Entry<String, T>> mapOption;

	private final Controller<T> controller;

	private final StateManager<T> stateManager;

	public MapValueOption(Option<Map.Entry<String, T>> mapOption, @NotNull Function<Option<T>, ControllerBuilder<T>> controlGetter) {
		this.mapOption = mapOption;
		this.controller = controlGetter.apply(this).build();
		this.stateManager = StateManager.createSimple(
			mapOption.binding().defaultValue().getValue(),
			() -> mapOption.pendingValue().getValue(),
			v -> mapOption.requestSet(new AbstractMap.SimpleEntry<>(mapOption.pendingValue().getKey(), v))
		);
	}

	@Override
	public @NotNull Text name() {
		return Text.empty();
	}

	@Override
	public @NotNull OptionDescription description() {
		return mapOption.description();
	}

	@Override
	public @NotNull Text tooltip() {
		return mapOption.tooltip();
	}

	@Override
	public @NotNull Controller<T> controller() {
		return controller;
	}

	@Override
	public @NotNull StateManager<T> stateManager() {
		return stateManager;
	}

	@Override
	@Deprecated
	public @NotNull Binding<T> binding() {
		if (stateManager instanceof ProvidesBindingForDeprecation) {
			return ((ProvidesBindingForDeprecation<T>) stateManager).getBinding();
		}
		throw new UnsupportedOperationException("Binding is not available for this option - using a new state manager which does not directly expose the binding as it may not have one.");
	}

	@Override
	public boolean available() {
		return mapOption.available();
	}

	@Override
	public void setAvailable(boolean available) {
		mapOption.setAvailable(available);
	}

	@Override
	public @NotNull ImmutableSet<OptionFlag> flags() {
		return mapOption.flags();
	}

	@Override
	public boolean changed() {
		return mapOption.changed();
	}

	@Override
	public @NotNull T pendingValue() {
		return mapOption.pendingValue().getValue();
	}

	@Override
	public void requestSet(@NotNull T value) {
		Validate.notNull(value, "`value` cannot be null");

		mapOption.requestSet(new AbstractMap.SimpleEntry<>(mapOption.pendingValue().getKey(), value));
	}

	@Override
	public boolean applyValue() {
		return mapOption.applyValue();
	}

	@Override
	public void forgetPendingValue() {
		mapOption.forgetPendingValue();
	}

	@Override
	public void requestSetDefault() {
		mapOption.requestSetDefault();
	}

	@Override
	public boolean isPendingValueDefault() {
		return mapOption.isPendingValueDefault();
	}

	@Override
	public void addEventListener(OptionEventListener<T> listener) {
		mapOption.addEventListener((o, e) -> listener.onEvent(this, e));
	}

	@Override
	@Deprecated
	public void addListener(BiConsumer<Option<T>, T> changedListener) {
		mapOption.addListener((o, e) -> changedListener.accept(this, e.getValue()));
	}
}

