package dev.sisby.mcqoy;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.LongFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.LongSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
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

	private static ConfigCategory.Builder createCategory(String section, LinkedHashMap<String, ConfigCategory.Builder> categories) {
		if (categories.containsKey(section)) {
			return categories.get(section);
		} else {
			String sectionKey = section;
			if (section == null) {
				sectionKey = "";
			} else {
				sectionKey += "_";
			}
			ConfigCategory.Builder category = ConfigCategory.createBuilder().name(Text.translatable("config.item-descriptions.%stitle".formatted(sectionKey)));
			categories.put(section, category);
			return category;
		}
	}

	public static Screen createScreen(Screen parent) {
		final YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder().title(Text.of(FabricLoader.getInstance().getModContainer(CONFIG.id()).map(m -> m.getMetadata().getName()).orElse(null)));
		LinkedHashMap<String, ConfigCategory.Builder> categories = new LinkedHashMap<>();
		for (TrackedValue<?> field : CONFIG.values()) {
			ConfigCategory.Builder category = ConfigCategory.createBuilder().name(getDisplayName(field));
			categories.put(field.key().toString(), category);
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
				case Boolean def -> category.option(Option.<Boolean>createBuilder().name(displayName).description(description).binding(def,
						()-> (Boolean) field.value(),
						(value)-> ((TrackedValue<Boolean>) field).setValue(value)
					)
					.controller(TickBoxControllerBuilder::create)
					.build());
				case String def -> category.option(Option.<String>createBuilder().name(displayName).description(description).binding(def,
						()-> (String) field.value(),
						(value)->((TrackedValue<String>) field).setValue(value)
					)
					.controller(StringControllerBuilder::create)
					.build());
				case Integer def -> category.option(Option.<Integer>createBuilder().name(displayName).description(description).binding(def,
						()-> (Integer) field.value(),
						(value)->((TrackedValue<Integer>) field).setValue(value)
					)
					.controller(rangeConstraint == null ? IntegerFieldControllerBuilder::create : opt -> IntegerSliderControllerBuilder.create(opt).range((Integer) rangeConstraint.min(), (Integer) rangeConstraint.max()).step(1))
					.build());
				case Long def -> category.option(Option.<Long>createBuilder().name(displayName).description(description).binding(def,
						()-> (Long) field.value(),
						(value)->((TrackedValue<Long>) field).setValue(value)
					)
					.controller(rangeConstraint == null ? LongFieldControllerBuilder::create : opt -> LongSliderControllerBuilder.create(opt).range((Long) rangeConstraint.min(), (Long) rangeConstraint.max()).step(1L))
					.build());
				default -> LOGGER.info("[McQoy] Unfamiliar with field {} of class {} - skipping it!", field.key().getLastComponent(), field.getDefaultValue().getClass());
			}
		}
		for (ConfigCategory.Builder s : categories.values()) {
			builder.category(s.build());
		}

		builder.save(CONFIG::save);
		return builder.build().generateScreen(parent);
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
