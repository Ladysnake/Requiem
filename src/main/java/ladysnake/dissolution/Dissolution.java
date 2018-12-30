package ladysnake.dissolution;

import ladysnake.dissolution.init.DissolutionBlocks;
import ladysnake.dissolution.init.DissolutionItems;
import ladysnake.dissolution.remnant.DefaultRemnantHandler;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dissolution implements ModInitializer {
	public static final String MODID = "dissolution";
	public static final Logger LOGGER = LogManager.getLogger("Dissolution");

	@Override
	public void onInitialize() {
		DissolutionBlocks.init();
		DissolutionItems.init();
		DefaultRemnantHandler.init();
	}
}
