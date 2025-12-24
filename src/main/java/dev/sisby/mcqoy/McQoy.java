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
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import folk.sisby.kaleido.lib.quiltconfig.impl.util.ConfigsImpl;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueListImpl;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.ColorFieldBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraftforge.fml.ModContainer;
import org.apache.commons.lang3.StringUtils;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(McQoy.ID)
@Mod.EventBusSubscriber(modid = McQoy.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class McQoy {
	public static final String ID = "mcqoy";
	public static final Logger LOGGER = LogManager.getLogger(McQoy.class);
	public static final McQoyConfig CONFIG = McQoyConfig.createToml(FMLPaths.CONFIGDIR.get(), "", ID, McQoyConfig.class);
	public static final Set<String> matchedMods = new HashSet<>();
	public static final Set<String> missingMods = new HashSet<>();

	public static final List<String> RGB_CONSTRAINTS = Arrays.asList(
		"matches r'#[0-9a-fA-F]{6}'",
		"matches r'#[0-9A-Fa-f]{6}'",
		"matches r'#[a-fA-F0-9]{6}'",
		"matches r'#[A-Fa-f0-9]{6}'"
	);

	public static final List<String> ARGB_CONSTRAINTS = Arrays.asList(
		"matches r'#[0-9a-fA-F]{8}'",
		"matches r'#[0-9A-Fa-f]{8}'",
		"matches r'#[a-fA-F0-9]{8}'",
		"matches r'#[A-Fa-f0-9]{8}'"
	);

	public McQoy() {
		LOGGER.info("[McQoy] I’m beginning to think I can cure a rainy day!");
	}

	@SubscribeEvent
	public static void complete(FMLLoadCompleteEvent event) {
		getScreenFactories().forEach((id, factory) -> ModList.get().getModContainerById(id).ifPresent(c -> c
			.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((cl, p) -> factory.apply(p)))));
	}

	public static Map<String, Function<Screen, Screen>> getScreenFactories() {
		Multimap<String, Config> modConfigs = HashMultimap.create();
		for (Config config : ConfigsImpl.getAll()) {
			String modId = config.family().isEmpty() ? config.id() : config.family();
			boolean found = false;
			for (String s : Arrays.asList(
				modId,
				modId.replace("-", ""),
				modId.replace("_", ""),
				modId.replace("_", "-"),
				modId.replace("-", "_")
			)) {
				ModContainer mod = ModList.get().getModContainerById(s).orElse(null);
				if (mod != null) {
					if (!matchedMods.contains(s)) LOGGER.info("[McQoy] Matched config {} to \"{}\" ({})", getShortPath(config), mod.getModInfo().getDisplayName(), s);
					modConfigs.put(s, config);
					found = true;
					break;
				}
			}
			if (!found && !missingMods.contains(modId)) {
				missingMods.add(modId);
				LOGGER.warn("[McQoy] Failed to match config {} to any loaded mod", getShortPath(config));
			}
		}
		Map<String, Function<Screen, Screen>> screenFactories = new HashMap<>();
		modConfigs.asMap().forEach((id, configs) -> screenFactories.put(id, parent -> createScreen(parent, id, configs)));
		matchedMods.addAll(modConfigs.keys());
		return screenFactories;
	}

	public static Screen createScreen(Screen parent, String modId, Collection<Config> configs) {
		String modName = ModList.get().getModContainerById(modId).map(c -> c.getModInfo().getDisplayName()).orElse(modId);
		final ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.of("Config: " + modName));
		LinkedHashMap<String, ConfigCategory> categories = new LinkedHashMap<>();
		for (Config config : configs) {
			String simpleName = config.family().isEmpty() ? config.id() : config.family();
			Text configDisplayName = configs.size() == 1 ? Text.of(modName) : getDisplayName(config, simpleName, NamingSchemes.TITLE_CASE);
			ConfigCategory category;
			for (TrackedValue<?> field : config.values()) {
				if (field.key().length() == 1) { // No Section
					category = categories.computeIfAbsent(configDisplayName.getString(), k -> builder.getOrCreateCategory(configDisplayName));
				} else { // With section, take topmost
					ValueTreeNode topSection = config.getNode(Collections.singletonList(field.key().getKeyComponent(0)));
					Text sectionDisplayName = getDisplayName(topSection, topSection.key().getLastComponent(), NamingSchemes.TITLE_CASE);
					category = categories.computeIfAbsent(sectionDisplayName.getString(), k -> builder.getOrCreateCategory(sectionDisplayName));
				}
				mapAndAddField(config, field, category, builder.entryBuilder(), getDisplayName(field, field.key().getLastComponent(), NamingSchemes.SPACE_SEPARATED_LOWER_CASE_INITIAL_UPPER_CASE), getComments(field).stream().map(Text::of).toArray(Text[]::new));
			}
		}

		builder.setSavingRunnable(CONFIG::save);
		return builder.build();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void mapAndAddField(Config config, TrackedValue<?> field, ConfigCategory category, ConfigEntryBuilder builder, Text displayName, Text[] description) {
		Constraint.Range<?> tempRangeConstraint = null;
		boolean color = false;
		boolean tempAlpha = false;
		for (Constraint<?> constraint : field.constraints()) {
			if (constraint instanceof Constraint.Range<?>) tempRangeConstraint = (Constraint.Range<?>) constraint;
			if (RGB_CONSTRAINTS.stream().anyMatch(s -> constraint.getRepresentation().contains(s))) color = true;
			if (ARGB_CONSTRAINTS.stream().anyMatch(s -> constraint.getRepresentation().contains(s))) {
				color = true;
				tempAlpha = true;
			}
		}
		final boolean alpha = tempAlpha;
		final Constraint.Range<?> rangeConstraint = tempRangeConstraint;
		Object defaultValue = field.getDefaultValue();
		if (defaultValue instanceof Boolean) category.addEntry(option((TrackedValue<Boolean>) field, displayName, description, builder::startBooleanToggle).build());
		else if (defaultValue instanceof String && color) category.addEntry(option((TrackedValue<String>) field, displayName, description, (BiFunction<Text, Integer, ColorFieldBuilder>) builder::startAlphaColorField, s -> colorOrWhite(s, alpha), i -> colorToString(i, alpha)).build());
		else if (defaultValue instanceof String) category.addEntry(option((TrackedValue<String>) field, displayName, description, builder::startStrField).build());
		else if (defaultValue instanceof Integer) slider(category, (TrackedValue<Integer>) field, displayName, description, builder::startIntField, builder::startIntSlider, rangeConstraint);
		else if (defaultValue instanceof Long) slider(category, (TrackedValue<Long>) field, displayName, description, builder::startLongField, builder::startLongSlider, rangeConstraint);
		else if (defaultValue instanceof Float) category.addEntry(option((TrackedValue<Float>) field, displayName, description, builder::startFloatField).build());
		else if (defaultValue instanceof Double) category.addEntry(option((TrackedValue<Double>) field, displayName, description, builder::startDoubleField).build());
		else if (defaultValue instanceof Enum) category.addEntry(option((TrackedValue<Enum>) field, displayName, description, (d, e) -> builder.startEnumSelector(d, e.getDeclaringClass(), e)).build());
		else if (defaultValue instanceof ValueListImpl<?>) {
			ValueListImpl<?> list = (ValueListImpl<?>) defaultValue;
			if (list.getDefaultValue() instanceof String) category.addEntry(listOption((TrackedValue<ValueList<String>>) field, displayName, description, builder::startStrList).build());
			else if (list.getDefaultValue() instanceof Integer) category.addEntry(listOption((TrackedValue<ValueList<Integer>>) field, displayName, description, builder::startIntList).build());
			else if (list.getDefaultValue() instanceof Long) category.addEntry(listOption((TrackedValue<ValueList<Long>>) field, displayName, description, builder::startLongList).build());
			else if (list.getDefaultValue() instanceof Float) category.addEntry(listOption((TrackedValue<ValueList<Float>>) field, displayName, description, builder::startFloatList).build());
			else if (list.getDefaultValue() instanceof Double) category.addEntry(listOption((TrackedValue<ValueList<Double>>) field, displayName, description, builder::startDoubleList).build());
			else { // Boolean List, Enum List
				LOGGER.warn("[McQoy] Unfamiliar with list field {} of class {} - displaying placeholder.", field.key().getLastComponent(), list.getDefaultValue().getClass());
				incompatibleOption(config, category, field, displayName, description, builder);
			}
		} else { // Maps
			LOGGER.warn("[McQoy] Unfamiliar with field {} of class {} - displaying placeholder.", field.key().getLastComponent(), field.getDefaultValue().getClass());
			incompatibleOption(config, category, field, displayName, description, builder);
		}
	}

	private static Integer colorOrWhite(String string, boolean alpha) {
		try {
			return Integer.parseUnsignedInt(string.replace("#", ""), 16) | (alpha ? 0 : 0xFF_000000);
		} catch (NumberFormatException e) {
			return alpha ? 0xFF_FFFFFF : 0xFFFFFF;
		}
	}

	private static String colorToString(Integer color, boolean alpha) {
		return "#" + StringUtils.leftPad(Integer.toHexString(color & (alpha ? 0xFF_FFFFFF : 0x00_FFFFFF)), alpha ? 8 : 6, "0");
	}

	private static void incompatibleOption(Config config, ConfigCategory category, TrackedValue<?> field, Text displayName, Text[] description, ConfigEntryBuilder builder) {
		Text fileHint = Text.of(String.format("Only editable via %s", (config.family().isEmpty() ? "" : (config.family() + "/")) + config.id() + ".toml"));
		Text exitHint = Text.of("Exit the game first.");
		Text desc = Text.of(Stream.concat(Arrays.stream(description), Stream.of(Text.of(""), fileHint, exitHint)).map(Text::getString).collect(Collectors.joining("\n")));
		Text label = Text.of("[Edit in file...] " + displayName);
		category.addEntry(builder.startTextDescription(label.copy().styled(s -> s
			.withColor(TextColor.fromRgb(0xFF_FFFF55))
			.withHoverEvent(desc.getString().isEmpty() ? null : new HoverEvent(HoverEvent.Action.SHOW_TEXT, desc))
			.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, FMLPaths.CONFIGDIR.get().toFile().toString()))
		)).build());
	}

	private static <T, C, B extends AbstractFieldBuilder<C, ?, B>> B option(TrackedValue<T> field, Text displayName, Text[] description, BiFunction<Text, C, B> constructor, Function<T, C> getBinder, Function<C, T> setBinder) {
		return constructor.apply(displayName, getBinder.apply(field.value())).setTooltip(description).setSaveConsumer(v -> {
			if (field.checkForFailingConstraints(setBinder.apply(v)).isPresent()) return;
			field.setValue(setBinder.apply(v));
		});
	}

	private static <T, C, B extends AbstractFieldBuilder<List<C>, ?, B>> B listOption(TrackedValue<ValueList<T>> field, Text displayName, Text[] description, BiFunction<Text, ValueList<C>, B> constructor, Function<T, C> getBinder, Function<C, T> setBinder) {
		return constructor.apply(displayName, new ValueListImpl<C>(getBinder.apply(field.getDefaultValue().getDefaultValue()), field.value().stream().map(getBinder).collect(Collectors.toList()))).setTooltip(description).setSaveConsumer(v -> {
			ValueList<T> list = new ValueListImpl<T>(field.getDefaultValue().getDefaultValue(), v.stream().map(setBinder).collect(Collectors.toList()));
			if (field.checkForFailingConstraints(list).isPresent()) return;
			field.setValue(list);
		});
	}

	private static <T, B extends AbstractFieldBuilder<T, ?, B>> B option(TrackedValue<T> field, Text displayName, Text[] description, BiFunction<Text, T, B> constructor) {
		return option(field, displayName, description, constructor, t -> t, t -> t);
	}

	private static <T, B extends AbstractFieldBuilder<List<T>, ?, B>> B listOption(TrackedValue<ValueList<T>> field, Text displayName, Text[] description, BiFunction<Text, ValueList<T>, B> constructor) {
		return listOption(field, displayName, description, constructor, t -> t, t -> t);
	}

	@SuppressWarnings("unchecked")
	private static <T, B extends AbstractFieldBuilder<T, ?, B>, B2 extends AbstractFieldBuilder<T, ?, B2>> void slider(ConfigCategory category, TrackedValue<T> field, Text displayName, Text[] description, BiFunction<Text, T, B> simple, Function4<Text, T, T, T, B2> slider, Constraint.Range<?> rangeConstraint) {
		if (rangeConstraint == null) {
			category.addEntry(option(field, displayName, description, simple).build());
		} else {
			category.addEntry(option(field, displayName, description, (t, v) -> slider.apply(t, v, (T) rangeConstraint.min(), (T) rangeConstraint.max())).build());
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

	public static String getShortPath(Config config) {
		return config.family().isEmpty() ? config.id() : config.family() + "/" + config.id() + ".toml";
	}
}
