package dev.sisby.mcqoy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
import dev.sisby.mcqoy.controller.EntryController;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.MetadataContainer;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingScheme;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import folk.sisby.kaleido.lib.quiltconfig.impl.util.ConfigsImpl;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueListImpl;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueMapImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Mod(McQoy.ID)
@EventBusSubscriber(modid = McQoy.ID)
public class McQoy {
	public static final String ID = "mcqoy";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	public static final McQoyConfig CONFIG = McQoyConfig.createToml(FMLPaths.CONFIGDIR.get(), "", ID, McQoyConfig.class);

	public McQoy() {
		LOGGER.info("[McQoy] I’m beginning to think I can cure a rainy day!");
	}

	@SubscribeEvent
	public static void complete(FMLLoadCompleteEvent event) {
		getScreenFactories().forEach((id, factory) -> ModList.get().getModContainerById(id).ifPresent(c -> c
			.registerExtensionPoint(IConfigScreenFactory.class, (cl, p) -> factory.apply(p))));
	}

	public static Map<String, Function<Screen, Screen>> getScreenFactories() {
		Multimap<String, Config> modConfigs = HashMultimap.create();
		for (Config config : ConfigsImpl.getAll()) {
			String modId = config.family().isEmpty() ? config.id() : config.family();
			List.of(
				modId,
				modId.replace("-", ""),
				modId.replace("_", ""),
				modId.replace("_", "-"),
				modId.replace("-", "_")
			).forEach(s -> modConfigs.put(s, config));
		}
		Map<String, Function<Screen, Screen>> screenFactories = new HashMap<>();
		modConfigs.asMap().forEach((id, configs) -> screenFactories.put(id, parent -> createScreen(parent, id, configs)));
		return screenFactories;
	}

	public static Screen createScreen(Screen parent, String modId, Collection<Config> configs) {
		final YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder().title(Text.of("Config: " + ModList.get().getModContainerById(modId).map(c -> c.getModInfo().getDisplayName()).orElse(modId)));
		LinkedHashMap<String, ConfigCategory.Builder> categories = new LinkedHashMap<>();
		for (Config config : configs) {
			Text configDisplayName = getDisplayName(config, config.family().isEmpty() ? config.id() : config.family(), NamingSchemes.TITLE_CASE);
			ConfigCategory.Builder category;
			for (TrackedValue<?> field : config.values()) {
				if (field.key().length() == 1) { // No Section
					category = categories.computeIfAbsent(configDisplayName.getString(), k -> ConfigCategory.createBuilder().name(configDisplayName));
				} else { // With section, take topmost
					ValueTreeNode topSection = config.getNode(List.of(field.key().getKeyComponent(0)));
					Text sectionDisplayName = getDisplayName(topSection, topSection.key().getLastComponent(), NamingSchemes.TITLE_CASE);
					category = categories.computeIfAbsent(sectionDisplayName.getString(), k -> ConfigCategory.createBuilder().name(sectionDisplayName));
				}
				mapAndAddField(field, category, getDisplayName(field, field.key().getLastComponent(), NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE), OptionDescription.of(getComments(field).stream().map(Text::of).toArray(Text[]::new)));
			}
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
			case ValueMapImpl<?> map -> {
				switch (map.getDefaultValue()) {
					case Boolean ignored -> mapOption((TrackedValue<ValueMap<Boolean>>) field, category, displayName, description, TickBoxControllerBuilder::create);
					case String ignored -> mapOption((TrackedValue<ValueMap<String>>) field, category, displayName, description, StringControllerBuilder::create);
					case Integer ignored -> mapOption((TrackedValue<ValueMap<Integer>>) field, category, displayName, description, intOrSliderController(rangeConstraint));
					case Long ignored -> mapOption((TrackedValue<ValueMap<Long>>) field, category, displayName, description, longOrSliderController(rangeConstraint));
					case Float ignored -> mapOption((TrackedValue<ValueMap<Float>>) field, category, displayName, description, floatOrSliderController(rangeConstraint));
					case Double ignored -> mapOption((TrackedValue<ValueMap<Double>>) field, category, displayName, description, doubleOrSliderController(rangeConstraint));
					case Enum def -> enumMapOption(field, category, displayName, description, map, def);
					default -> LOGGER.warn("[McQoy] Unfamiliar with map field {} of class {} - skipping it!", field.key().getLastComponent(), map.getDefaultValue().getClass());
				}
			}
			default -> LOGGER.warn("[McQoy] Unfamiliar with field {} of class {} - skipping it!", field.key().getLastComponent(), field.getDefaultValue().getClass());
		}
	}

	private static <T> void singleOption(TrackedValue<T> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, Function<Option<T>, ControllerBuilder<T>> controller) {
		category.option(Option.<T>createBuilder().name(displayName).description(description).binding(field.getDefaultValue(), field::value, field::setValue).controller(controller).build());
	}

	private static <T> void listOption(TrackedValue<ValueList<T>> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, Function<Option<T>, ControllerBuilder<T>> controller) {
		category.group(ListOption.<T>createBuilder().name(displayName).description(description).binding(field.getDefaultValue(), field::value,
			l -> {
				field.value().clear();
				field.value().addAll(l);
			}
		).controller(controller).initial(field.getDefaultValue().getDefaultValue()).build());
	}

	private static <T> void mapOption(TrackedValue<ValueMap<T>> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, Function<Option<T>, ControllerBuilder<T>> valueController) {
		category.group(ListOption.<Map.Entry<String, T>>createBuilder().name(displayName).description(description).binding(
			field.getDefaultValue().entrySet().stream().toList(),
			() -> field.value().entrySet().stream().toList(),
			l -> {
				field.value().clear();
				l.forEach(e -> field.value().put(e.getKey(), e.getValue()));
			}
		).customController(o -> new EntryController<>(o, StringControllerBuilder::create, valueController)).initial(new AbstractMap.SimpleEntry<>("", field.getDefaultValue().getDefaultValue())).build());
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

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> void enumMapOption(TrackedValue<?> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, ValueMap<?> defaultMap, T defaultValue) {
		category.group(ListOption.<Map.Entry<String, T>>createBuilder().name(displayName).description(description).binding(
			((TrackedValue<ValueMap<T>>) field).getDefaultValue().entrySet().stream().toList(),
			() -> ((TrackedValue<ValueMap<T>>) field).value().entrySet().stream().toList(),
			l -> {
				((TrackedValue<ValueMap<T>>) field).value().clear();
				l.forEach(e -> ((TrackedValue<ValueMap<T>>) field).value().put(e.getKey(), e.getValue()));
			}
		).customController(o -> new EntryController<>(o, StringControllerBuilder::create, o2 -> EnumControllerBuilder.create(o2).enumClass(defaultValue.getDeclaringClass()))).initial(new AbstractMap.SimpleEntry<>("", ((TrackedValue<ValueMap<T>>) field).getDefaultValue().getDefaultValue())).build());
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

	public static Text getDisplayName(MetadataContainer value, String fallback, NamingScheme fallbackScheme) {
		if (value.hasMetadata(DisplayName.TYPE)) {
			if (value.metadata(DisplayName.TYPE).isTranslatable()) {
				return Text.translatable(value.metadata(DisplayName.TYPE).getName());
			}
			return Text.literal(value.metadata(DisplayName.TYPE).getName());
		} else {
			return Text.literal(Objects.requireNonNullElse(value.metadata(DisplayNameConvention.TYPE), fallbackScheme).coerce(fallback));
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
