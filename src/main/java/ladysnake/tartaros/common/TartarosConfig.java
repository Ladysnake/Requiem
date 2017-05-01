package ladysnake.tartaros.common;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class TartarosConfig {

	public static boolean respawnInNether = false;
	
	public static void syncConfig() {
		try {
	        Tartaros.config.load();

	        // Read props from config
	        Property shouldRespawnInNetherProp = Tartaros.config.get(
	        		Configuration.CATEGORY_GENERAL,
	                "shouldRespawnInNether", // Property name
	                "false", // Default value
	                "Whether players should respawn in the nether when they die");

	        respawnInNether = shouldRespawnInNetherProp.getBoolean();
	    } catch (Exception e) {
	    } finally {
	        if (Tartaros.config.hasChanged()) Tartaros.config.save();
	    }
	}
}
