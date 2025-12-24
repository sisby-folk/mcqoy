package dev.sisby.mcqoy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Function4;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.MetadataContainer;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingScheme;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import folk.sisby.kaleido.lib.quiltconfig.impl.util.ConfigsImpl;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueListImpl;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class McQoy implements ModInitializer {
	public static final String ID = "mcqoy";
	public static final Logger LOGGER = LogManager.getLogger(McQoy.class);
	public static final McQoyConfig CONFIG = McQoyConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", ID, McQoyConfig.class);

	@Override
	public void onInitialize() {
		LOGGER.info("[McQoy] I’m beginning to think I can cure a rainy day!");
	}

	public static Map<String, Function<Screen, Screen>> getScreenFactories() {
		Multimap<String, Config> modConfigs = HashMultimap.create();
		for (Config config : ConfigsImpl.getAll()) {
			String modId = config.family().isEmpty() ? config.id() : config.family();
			Arrays.asList(
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
		final ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.of("Config: " + FabricLoader.getInstance().getModContainer(modId).get().getMetadata().getName()));
		LinkedHashMap<String, ConfigCategory> categories = new LinkedHashMap<>();
		for (Config config : configs) {
			Text configDisplayName = getDisplayName(config, config.family().isEmpty() ? config.id() : config.family(), NamingSchemes.TITLE_CASE);
			ConfigCategory category;
			for (TrackedValue<?> field : config.values()) {
				if (field.key().length() == 1) { // No Section
					category = categories.computeIfAbsent(configDisplayName.getString(), k -> builder.getOrCreateCategory(configDisplayName));
				} else { // With section, take topmost
					ValueTreeNode topSection = config.getNode(Collections.singletonList(field.key().getKeyComponent(0)));
					Text sectionDisplayName = getDisplayName(topSection, topSection.key().getLastComponent(), NamingSchemes.TITLE_CASE);
					category = categories.computeIfAbsent(sectionDisplayName.getString(), k -> builder.getOrCreateCategory(sectionDisplayName));
				}
				mapAndAddField(config, field, category, builder.entryBuilder());
			}
		}

		builder.setSavingRunnable(CONFIG::save);
		return builder.build();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void mapAndAddField(Config config, TrackedValue<?> field, ConfigCategory category, ConfigEntryBuilder builder) {
		Constraint.Range<?> tempRangeConstraint = null;
		for (Constraint<?> constraint : field.constraints()) {
			if (constraint instanceof Constraint.Range<?>) {
				tempRangeConstraint = (Constraint.Range<?>) constraint;
				break;
			}
		}
		final Constraint.Range<?> rangeConstraint = tempRangeConstraint;
		Object defaultValue = field.getDefaultValue();
		if (defaultValue instanceof Boolean) category.addEntry(option((TrackedValue<Boolean>) field, builder::startBooleanToggle).build());
		else if (defaultValue instanceof String) category.addEntry(option((TrackedValue<String>) field, builder::startStrField).build());
		else if (defaultValue instanceof Integer) slider(category, (TrackedValue<Integer>) field, builder::startIntField, builder::startIntSlider, rangeConstraint);
		else if (defaultValue instanceof Long) slider(category, (TrackedValue<Long>) field, builder::startLongField, builder::startLongSlider, rangeConstraint);
		else if (defaultValue instanceof Float) category.addEntry(option((TrackedValue<Float>) field, builder::startFloatField).build());
		else if (defaultValue instanceof Double) category.addEntry(option((TrackedValue<Double>) field, builder::startDoubleField).build());
		else if (defaultValue instanceof Enum) category.addEntry(option((TrackedValue<Enum>) field, (d, e) -> builder.startEnumSelector(d, e.getDeclaringClass(), e)).build());
		else if (defaultValue instanceof ValueListImpl<?>) {
			ValueListImpl<?> list = (ValueListImpl<?>) defaultValue;
			if (list.getDefaultValue() instanceof String) category.addEntry(option((TrackedValue<List<String>>) field, builder::startStrList).build());
			else if (list.getDefaultValue() instanceof Integer) category.addEntry(option((TrackedValue<List<Integer>>) field, builder::startIntList).build());
			else if (list.getDefaultValue() instanceof Long) category.addEntry(option((TrackedValue<List<Long>>) field, builder::startLongList).build());
			else if (list.getDefaultValue() instanceof Float) category.addEntry(option((TrackedValue<List<Float>>) field, builder::startFloatList).build());
			else if (list.getDefaultValue() instanceof Double) category.addEntry(option((TrackedValue<List<Double>>) field, builder::startDoubleList).build());
			else { // Boolean List, Enum List
				LOGGER.warn("[McQoy] Unfamiliar with list field {} of class {} - displaying placeholder.", field.key().getLastComponent(), list.getDefaultValue().getClass());
				incompatibleOption(config, category, field);
			}
		} else { // Maps
			LOGGER.warn("[McQoy] Unfamiliar with field {} of class {} - displaying placeholder.", field.key().getLastComponent(), field.getDefaultValue().getClass());
			incompatibleOption(config, category, field);
		}
	}

	private static void incompatibleOption(Config config, ConfigCategory category, TrackedValue<?> field) {
		Text fileHint = Text.of(String.format("Only editable via %s", (config.family().isEmpty() ? "" : (config.family() + "/")) + config.id() + ".toml"));
		Text exitHint = Text.of("Exit the game first.");
		Text[] desc = Stream.concat(getComments(field).stream().map(Text::of), Stream.of(Text.of(""), fileHint, exitHint)).toArray(Text[]::new);
		Text label = Text.of("[Edit in file...] " + getDisplayName(field, field.key().getLastComponent(), NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE).getString());
		// Cloth doesn't have click actions...
		category.addEntry(new TextListEntry(Text.of(""), label, 0xFFFFFF55, () -> Optional.of(desc)) {
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				Util.getOperatingSystem().open(FabricLoader.getInstance().getConfigDir().toFile());
				return true;
			}
		});
	}

	private static <T, B extends AbstractFieldBuilder<T, ?, B>> B option(TrackedValue<T> field, BiFunction<Text, T, B> constructor) {
		return constructor.apply(
			getDisplayName(field, field.key().getLastComponent(), NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE),
			field.value()
		).setTooltip(getComments(field).stream().map(Text::of).toArray(Text[]::new))
		.setSaveConsumer(field::setValue);
	}

	@SuppressWarnings("unchecked")
	private static <T, B extends AbstractFieldBuilder<T, ?, B>, B2 extends AbstractFieldBuilder<T, ?, B2>> void slider(ConfigCategory category, TrackedValue<T> field, BiFunction<Text, T, B> simple, Function4<Text, T, T, T, B2> slider, Constraint.Range<?> rangeConstraint) {
		if (rangeConstraint == null) {
			category.addEntry(option(field, simple).build());
		} else {
			category.addEntry(option(field, (t, v) -> slider.apply(t, v, (T) rangeConstraint.min(), (T) rangeConstraint.max())).build());
		}
	}

	public static Text getDisplayName(MetadataContainer value, String fallback, NamingScheme fallbackScheme) {
		if (value.hasMetadata(DisplayName.TYPE)) {
			return Text.of(value.metadata(DisplayName.TYPE).getName());
		} else {
			return Text.of((value.hasMetadata(DisplayNameConvention.TYPE) ? value.metadata(DisplayNameConvention.TYPE) : fallbackScheme).coerce(fallback));
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
