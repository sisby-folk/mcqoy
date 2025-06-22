package dev.sisby.mcqoy;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

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
		Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
		McQoy.getScreenFactories().forEach((id, factory) -> factories.put(id, (ConfigScreenFactory<?>) factory));
		return factories;
	}
}
