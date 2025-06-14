package dev.sisby.mcqoy;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.impl.util.ConfigsImpl;

import java.util.HashMap;
import java.util.Map;

public class McQoyModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> McQoy.createScreen(parent, McQoy.CONFIG);
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		Map<String, ConfigScreenFactory<?>> screenFactories = new HashMap<>();
		for (Config config : ConfigsImpl.getAll()) {
			screenFactories.put(config.family().isEmpty() ? config.id() : config.family(), parent -> McQoy.createScreen(parent, config));
		}
		return screenFactories;
	}
}
