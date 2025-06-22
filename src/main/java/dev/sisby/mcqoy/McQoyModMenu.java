package dev.sisby.mcqoy;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.impl.util.ConfigsImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class McQoyModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> McQoy.createScreen(parent, McQoy.ID, List.of(McQoy.CONFIG));
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
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
		Map<String, ConfigScreenFactory<?>> screenFactories = new HashMap<>();
		modConfigs.asMap().forEach((id, configs) -> screenFactories.put(id, parent -> McQoy.createScreen(parent, id, configs)));
		return screenFactories;
	}
}
