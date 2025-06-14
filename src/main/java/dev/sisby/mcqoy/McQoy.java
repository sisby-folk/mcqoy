package dev.sisby.mcqoy;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.LongFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.LongSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueListImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class McQoy implements ModInitializer {
	public static final String ID = "mcqoy";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final McQoyConfig CONFIG = McQoyConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", ID, McQoyConfig.class);

	@Override
	public void onInitialize() {
		LOGGER.info("[McQoy] I’m beginning to think I can cure a rainy day!");

	}

	public static Screen createScreen(Screen parent, Config config) {
		final YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder().title(Text.of(FabricLoader.getInstance().getModContainer(CONFIG.id()).map(m -> m.getMetadata().getName()).orElse(null)));
		LinkedHashMap<String, ConfigCategory.Builder> categories = new LinkedHashMap<>();
		for (TrackedValue<?> field : config.values()) {
			ConfigCategory.Builder category = categories.computeIfAbsent(
				field.key().length() == 1 ? (config.family().isEmpty() ? config.id() : config.family()) : field.key().getKeyComponent(0),
				k -> ConfigCategory.createBuilder().name(Text.of(Objects.requireNonNullElse(config.metadata(DisplayNameConvention.TYPE), NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE).coerce(k)))
			);
			Text displayName = getDisplayName(field);
			OptionDescription description = OptionDescription.of(getComments(field).stream().map(Text::of).toArray(Text[]::new));
			mapAndAddField(field, category, displayName, description);
		}
		for (ConfigCategory.Builder s : categories.values()) {
			builder.category(s.build());
		}

		builder.save(CONFIG::save);
		return builder.build().generateScreen(parent);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void mapAndAddField(TrackedValue<?> field, ConfigCategory.Builder category, Text displayName, OptionDescription description) {
		Constraint.Range<?> tempRangeConstraint = null;
		for (Constraint<?> constraint : field.constraints()) {
			if (constraint instanceof Constraint.Range<?> range) {
				tempRangeConstraint = range;
				break;
			}
		}
		final Constraint.Range<?> rangeConstraint = tempRangeConstraint;
		switch (field.getDefaultValue()) {
			case Boolean ignored -> singleOption((TrackedValue<Boolean>) field, category, displayName, description, TickBoxControllerBuilder::create);
			case String ignored -> singleOption((TrackedValue<String>) field, category, displayName, description, StringControllerBuilder::create);
			case Integer ignored -> singleOption((TrackedValue<Integer>) field, category, displayName, description, intOrSliderController(rangeConstraint));
			case Long ignored -> singleOption((TrackedValue<Long>) field, category, displayName, description, longOrSliderController(rangeConstraint));
			case Float ignored -> singleOption((TrackedValue<Float>) field, category, displayName, description, floatOrSliderController(rangeConstraint));
			case Double ignored -> singleOption((TrackedValue<Double>) field, category, displayName, description, doubleOrSliderController(rangeConstraint));
			case Enum def -> enumOption(field, category, displayName, description, def);
			case ValueListImpl<?> list -> {
				switch (list.getDefaultValue()) {
					case Boolean ignored -> listOption((TrackedValue<ValueList<Boolean>>) field, category, displayName, description, TickBoxControllerBuilder::create);
					case String ignored -> listOption((TrackedValue<ValueList<String>>) field, category, displayName, description, StringControllerBuilder::create);
					case Integer ignored -> listOption((TrackedValue<ValueList<Integer>>) field, category, displayName, description, intOrSliderController(rangeConstraint));
					case Long ignored -> listOption((TrackedValue<ValueList<Long>>) field, category, displayName, description, longOrSliderController(rangeConstraint));
					case Float ignored -> listOption((TrackedValue<ValueList<Float>>) field, category, displayName, description, floatOrSliderController(rangeConstraint));
					case Double ignored -> listOption((TrackedValue<ValueList<Double>>) field, category, displayName, description, doubleOrSliderController(rangeConstraint));
					case Enum def -> enumListOption(field, category, displayName, description, list, def);
					default -> LOGGER.warn("[McQoy] Unfamiliar with list field {} of class {} - skipping it!", field.key().getLastComponent(), list.getDefaultValue().getClass());
				}
			}
			default -> LOGGER.warn("[McQoy] Unfamiliar with field {} of class {} - skipping it!", field.key().getLastComponent(), field.getDefaultValue().getClass());
		}
	}

	private static <T> void singleOption(TrackedValue<T> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, Function<Option<T>, ControllerBuilder<T>> controller) {
		category.option(Option.<T>createBuilder().name(displayName).description(description).binding(field.getDefaultValue(), field::value, field::setValue).controller(controller).build());
	}

	private static <T> void listOption(TrackedValue<ValueList<T>> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, Function<Option<T>, ControllerBuilder<T>> controller) {
		category.group(ListOption.<T>createBuilder().name(displayName).description(description).binding((field.getDefaultValue()), field::value,
			l -> {
				field.value().clear();
				field.value().addAll(l);
			}
		).controller(controller).initial(field.getDefaultValue().getDefaultValue()).build());
	}

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> void enumOption(TrackedValue<?> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, T defaultValue) {
		category.option(Option.<T>createBuilder().name(displayName).description(description).binding(defaultValue, () -> (T) field.value(), v -> ((TrackedValue<T>) field).setValue(v)).controller(
			o -> EnumControllerBuilder.create(o).enumClass(defaultValue.getDeclaringClass())
		).build());
	}

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> void enumListOption(TrackedValue<?> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, ValueList<?> defaultList, T defaultValue) {
		category.group(ListOption.<T>createBuilder().name(displayName).description(description).binding((List<T>) defaultList, () -> (ValueList<T>) field.value(),
			l -> {
				((TrackedValue<ValueList<T>>) field).value().clear();
				((TrackedValue<ValueList<T>>) field).value().addAll(l);
			}
		).controller(o -> EnumControllerBuilder.create(o).enumClass(defaultValue.getDeclaringClass())).initial(defaultValue).build());
	}

	private static Function<Option<Integer>, ControllerBuilder<Integer>> intOrSliderController(Constraint.Range<?> rangeConstraint) {
		if (rangeConstraint == null) return IntegerFieldControllerBuilder::create;
		return opt -> IntegerSliderControllerBuilder.create(opt)
			.range((Integer) rangeConstraint.min(), (Integer) rangeConstraint.max())
			.step(1);
	}

	private static Function<Option<Long>, ControllerBuilder<Long>> longOrSliderController(Constraint.Range<?> rangeConstraint) {
		if (rangeConstraint == null) return LongFieldControllerBuilder::create;
		return opt -> LongSliderControllerBuilder.create(opt)
			.range((Long) rangeConstraint.min(), (Long) rangeConstraint.max())
			.step(1L);
	}

	private static Function<Option<Float>, ControllerBuilder<Float>> floatOrSliderController(Constraint.Range<?> rangeConstraint) {
		if (rangeConstraint == null) return FloatFieldControllerBuilder::create;
		return opt -> FloatSliderControllerBuilder.create(opt)
			.range((Float) rangeConstraint.min(), (Float) rangeConstraint.max())
			.step(0.01F)
			.formatValue(f -> Text.of("%.2f".formatted(f)));
	}

	private static Function<Option<Double>, ControllerBuilder<Double>> doubleOrSliderController(Constraint.Range<?> rangeConstraint) {
		if (rangeConstraint == null) return DoubleFieldControllerBuilder::create;
		return opt -> DoubleSliderControllerBuilder.create(opt)
			.range((Double) rangeConstraint.min(), (Double) rangeConstraint.max())
			.step(0.01)
			.formatValue(f -> Text.of("%.2f".formatted(f)));
	}

	public static Text getDisplayName(ValueTreeNode value) {
		if (value.hasMetadata(DisplayName.TYPE)) {
			if (value.metadata(DisplayName.TYPE).isTranslatable()) {
				return Text.translatable(value.metadata(DisplayName.TYPE).getName());
			}
			return Text.literal(value.metadata(DisplayName.TYPE).getName());
		} else {
			return Text.literal(Objects.requireNonNullElse(value.metadata(DisplayNameConvention.TYPE), NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE).coerce(value.key().getLastComponent()));
		}
	}

	public static List<String> getComments(ValueTreeNode node) {
		List<String> outList = new ArrayList<>();
		if (node.hasMetadata(Comment.TYPE)) {
			for (String string : node.metadata(Comment.TYPE)) {
				outList.add(string);
			}
		}
		return outList;
	}
}
