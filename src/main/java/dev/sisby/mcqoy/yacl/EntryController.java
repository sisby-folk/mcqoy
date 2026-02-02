package dev.sisby.mcqoy.yacl;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.network.chat.Component;

public class EntryController<T> implements Controller<Map.Entry<String, T>> {
	private final Option<Map.Entry<String, T>> option;
	private final Controller<String> keyController;
	private final Controller<T> valueController;

	public EntryController(Option<Map.Entry<String, T>> option, @NotNull Function<Option<String>, ControllerBuilder<String>> keyController, @NotNull Function<Option<T>, ControllerBuilder<T>> valueController) {
		this.option = option;
		this.keyController = new MapKeyOption<>(option, keyController).controller();
		this.valueController = new MapValueOption<>(option, valueController).controller();
	}

	@Override
	public Option<Map.Entry<String, T>> option() {
		return option;
	}

	@Override
	public Component formatValue() {
		return Component.literal(option.pendingValue().toString());
	}

	@Override
	public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
		return new EntryControllerElement<>(this, screen, widgetDimension, keyController.provideWidget(screen, widgetDimension), valueController.provideWidget(screen, widgetDimension));
	}
}
