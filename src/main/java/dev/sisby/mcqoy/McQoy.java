package dev.sisby.mcqoy;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McQoy implements ModInitializer {
	public static final String ID = "mcqoy";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[McQoy] I’m beginning to think I can cure a rainy day!");
	}
}
