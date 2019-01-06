package ladysnake.dissolution;

import ladysnake.dissolution.common.block.DissolutionBlocks;
import ladysnake.dissolution.common.entity.DissolutionEntities;
import ladysnake.dissolution.common.impl.DefaultRemnantHandler;
import ladysnake.dissolution.common.item.DissolutionItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dissolution implements ModInitializer {
	public static final String MOD_ID = "dissolution";
	public static final Logger LOGGER = LogManager.getLogger("Dissolution");

	public static Identifier id(String path) {
	    return new Identifier(MOD_ID, path);
    }

	@Override
	public void onInitialize() {
		DissolutionBlocks.init();
		DissolutionEntities.init();
		DissolutionItems.init();
		DefaultRemnantHandler.init();
	}
}
