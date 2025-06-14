package dev.sisby.mcqoy;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
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
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
				k -> ConfigCategory.createBuilder().name(Text.of(k))
			);
			Text displayName = getDisplayName(field);
			OptionDescription description = OptionDescription.of(getComments(field).stream().map(Text::of).toArray(Text[]::new));
			Constraint.Range<?> tempRangeConstraint = null;
			for (Constraint<?> constraint : field.constraints()) {
				if (constraint instanceof Constraint.Range<?> range) {
					tempRangeConstraint = range;
					break;
				}
			}
			final Constraint.Range<?> rangeConstraint = tempRangeConstraint;
			switch (field.getDefaultValue()) {
				case Boolean ignored -> boolOption((TrackedValue<Boolean>) field, category, displayName, description);
				case String ignored -> stringOption((TrackedValue<String>) field, category, displayName, description);
				case Integer ignored -> intOption((TrackedValue<Integer>) field, category, displayName, description, rangeConstraint);
				case Long ignored -> longOption((TrackedValue<Long>) field, category, displayName, description, rangeConstraint);
				case Enum def -> enumOption(category, displayName, description, field, def);
				default -> LOGGER.info("[McQoy] Unfamiliar with field {} of class {} - skipping it!", field.key().getLastComponent(), field.getDefaultValue().getClass());
			}
		}
		for (ConfigCategory.Builder s : categories.values()) {
			builder.category(s.build());
		}

		builder.save(CONFIG::save);
		return builder.build().generateScreen(parent);
	}

	private static void boolOption(TrackedValue<Boolean> field, ConfigCategory.Builder category, Text displayName, OptionDescription description) {
		category.option(Option.<Boolean>createBuilder().name(displayName).description(description).binding(field.getDefaultValue(), field::value, field::setValue)
			.controller(TickBoxControllerBuilder::create)
			.build());
	}

	private static void stringOption(TrackedValue<String> field, ConfigCategory.Builder category, Text displayName, OptionDescription description) {
		category.option(Option.<String>createBuilder().name(displayName).description(description).binding(field.getDefaultValue(), field::value, field::setValue)
			.controller(StringControllerBuilder::create)
			.build());
	}

	private static void intOption(TrackedValue<Integer> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, Constraint.Range<?> rangeConstraint) {
		category.option(Option.<Integer>createBuilder().name(displayName).description(description).binding(field.getDefaultValue(), field::value, field::setValue).controller(
			rangeConstraint == null ? IntegerFieldControllerBuilder::create : opt -> IntegerSliderControllerBuilder.create(opt)
				.range((Integer) rangeConstraint.min(), (Integer) rangeConstraint.max())
				.step(1)
		).build());
	}

	private static void longOption(TrackedValue<Long> field, ConfigCategory.Builder category, Text displayName, OptionDescription description, Constraint.Range<?> rangeConstraint) {
		category.option(Option.<Long>createBuilder().name(displayName).description(description).binding(field.getDefaultValue(), field::value, field::setValue).controller(
			rangeConstraint == null ? LongFieldControllerBuilder::create : opt -> LongSliderControllerBuilder.create(opt)
				.range((Long) rangeConstraint.min(), (Long) rangeConstraint.max())
				.step(1L)
		).build());
	}

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> void enumOption(ConfigCategory.Builder category, Text displayName, OptionDescription description, TrackedValue<?> field, T defaultValue) {
		category.option(Option.<T>createBuilder().name(displayName).description(description).binding(defaultValue, () -> (T) field.value(), v -> ((TrackedValue<T>) field).setValue(v))
			.controller(o -> EnumControllerBuilder.create(o).enumClass(defaultValue.getDeclaringClass()))
			.build());
	}

	public static Text getDisplayName(ValueTreeNode value) {
		if (value.hasMetadata(DisplayName.TYPE)) {
			if (value.metadata(DisplayName.TYPE).isTranslatable()) {
				return Text.translatable(value.metadata(DisplayName.TYPE).getName());
			}
			return Text.literal(value.metadata(DisplayName.TYPE).getName());
		} else if (value.hasMetadata(DisplayNameConvention.TYPE)) {
			return Text.literal(value.metadata(DisplayNameConvention.TYPE).coerce(value.key().getLastComponent()));
		} else {
			return Text.literal(value.key().getLastComponent());
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
