package ladysnake.dissolution;

import ladysnake.dissolution.common.block.DissolutionBlocks;
import ladysnake.dissolution.common.item.DissolutionItems;
import ladysnake.dissolution.common.network.DissolutionNetworking;
import ladysnake.dissolution.common.impl.DefaultRemnantHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dissolution implements ModInitializer {
	public static final String MODID = "dissolution";
	public static final Logger LOGGER = LogManager.getLogger("Dissolution");

	public static Identifier id(String path) {
	    return new Identifier(MODID, path);
    }

	@Override
	public void onInitialize() {
		DissolutionBlocks.init();
		DissolutionItems.init();
		DefaultRemnantHandler.init();
	}
}
