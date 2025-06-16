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

public class MapKeyOption<T> implements Option<String> {
	private final Option<Map.Entry<String, T>> mapOption;

	private final Controller<String> controller;

	private final StateManager<String> stateManager;

	public MapKeyOption(Option<Map.Entry<String, T>> mapOption, @NotNull Function<Option<String>, ControllerBuilder<String>> controlGetter) {
		this.mapOption = mapOption;
		this.controller = controlGetter.apply(this).build();
		this.stateManager = StateManager.createSimple(
			mapOption.binding().defaultValue().getKey(),
			() -> mapOption.pendingValue().getKey(),
			k -> mapOption.requestSet(new AbstractMap.SimpleEntry<>(k, mapOption.pendingValue().getValue()))
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
	public @NotNull Controller<String> controller() {
		return controller;
	}

	@Override
	public @NotNull StateManager<String> stateManager() {
		return stateManager;
	}

	@Override
	@Deprecated
	public @NotNull Binding<String> binding() {
		if (stateManager instanceof ProvidesBindingForDeprecation) {
			return ((ProvidesBindingForDeprecation<String>) stateManager).getBinding();
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
	public @NotNull String pendingValue() {
		return mapOption.pendingValue().getKey();
	}

	@Override
	public void requestSet(@NotNull String value) {
		Validate.notNull(value, "`value` cannot be null");

		mapOption.requestSet(new AbstractMap.SimpleEntry<>(value, mapOption.pendingValue().getValue()));
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
	public void addEventListener(OptionEventListener<String> listener) {
		mapOption.addEventListener((o, e) -> listener.onEvent(this, e));
	}

	@Override
	@Deprecated
	public void addListener(BiConsumer<Option<String>, String> changedListener) {
		mapOption.addListener((o, e) -> changedListener.accept(this, e.getKey()));
	}
}
