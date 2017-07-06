package ladysnake.dissolution.common;

import java.util.regex.Pattern;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class DissolutionConfig {
	
	public static final int NO_FLIGHT = -1;
	public static final int CUSTOM_FLIGHT = 0;
	public static final int CREATIVE_FLIGHT = 1;
	public static final int SPECTATOR_FLIGHT = 2;

	public static boolean anchorsXRay = false;
	public static boolean bodiesHoldInventory = true;
	public static boolean doSableDrop = true;
	public static boolean invisibleGhosts = false;
	public static int flightMode = CUSTOM_FLIGHT;
	public static boolean minionsAttackCreepers = true;
	public static boolean oneUseWaystone = true;
	public static boolean respawnInNether = true;
	public static int respawnDimension = -1;
	public static boolean skipDeathScreen = false;
	public static boolean soulCompass = true;
	public static boolean soulCompassAnchors = true;
	public static boolean spawnMercuryLakes = true;
	public static boolean useShaders = true;
	public static boolean wowRespawn = false;
	
	public static final String CATEGORY_RESPAWN = "Respawn";
	public static final String CATEGORY_GHOST = "Ghost";
	public static final String CATEGORY_WORLDGEN = "Worldgen";
	
	public static void syncConfig() {
		try {
	        Dissolution.config.load();
	        
	        Property versionProp = Dissolution.config.get(
	        		"Don't touch that", 
	        		"version", 
	        		1.1, 
	        		"The version of this configuration file. Don't modify this number unless you want your changes randomly reset.");

	        // RESPAWN SETTINGS
	        
	        Dissolution.config.addCustomCategoryComment(CATEGORY_RESPAWN, "Settings related to respawn mechanics. Please report any unwanted behaviour due to some combination of these.");
	        
	        Property wowRespawnProp = Dissolution.config.get(
	        		CATEGORY_RESPAWN, 
	        		"WoWlikeRespawn", 
	        		false,
	        		"If set to true, the player will respawn as a ghost at their spawnpoint. They will then have the choice to go to 0,0 to respawn without stuff or to reach their corpse under 5 minutes. (default : false)");
	        
	        Property shouldRespawnInNetherProp = Dissolution.config.get(
	        		CATEGORY_RESPAWN,
	                "shouldRespawnInNether",
	                false,
	                "Whether players should respawn in the nether when they die (default: false)");
	        
	        Property respawnDimensionProp = Dissolution.config.get(
	        		CATEGORY_RESPAWN, 
	        		"respawnDimension",
	        		-1,
	        		"If nether respawn is on, the player will respawn in this dimension instead. (default: -1)");
	        
	        Property shouldShowDeathScreenProp = Dissolution.config.get(
	        		CATEGORY_RESPAWN,
	                "skipDeathScreen",
	                false,
	                "Whether players should respawn instantly as souls without showing death screen (could mess with other mods) (default: false)");
	        
	        Property playerBodiesHoldInventoryProp = Dissolution.config.get(
	        		CATEGORY_RESPAWN, 
	        		"playerBodiesHoldInventoryProp", 
	        		true,
	        		"Whether long-lasting player corpses hold their inventory upon death. Recommended with WoWlikeRespawn. (default : true)");
	        
	        // GHOST SETTINGS
	        
	        Dissolution.config.addCustomCategoryComment(CATEGORY_GHOST, "Settings related to the spirit form.");
	        
	        Property invisibleGhostProp = Dissolution.config.get(
	        		CATEGORY_GHOST,
	        		"invisibleGhosts",
	        		false,
	        		"If set to true, dead players will be fully invisible (default: false)");
	        
	        Property flightModeProp = Dissolution.config.get(
	        		CATEGORY_GHOST,
	        		"flightMode",
	        		0,
	        		"-1= noflight, 0=custom flight, 1=creative, 2=spectator-lite (default: 0)");

	        Property interactableBlocksProp = Dissolution.config.get(
	        		CATEGORY_GHOST,
	        		"soulInteractableBlocks",
	        		"lever, glass_pane",
	        		"The blocks that can be right clicked/broken by ghosts (this config option doesn't affect anything currently)");
	        
	        // WORLD GEN SETTINGS
	        
	        Dissolution.config.addCustomCategoryComment(CATEGORY_WORLDGEN, "Settings related to world generation and structures");
	        
	        Property spawnMercuryLakesProp = Dissolution.config.get(
	        		CATEGORY_WORLDGEN,
	        		"spawnMercuryLakes",
	        		true,
	        		"If set to false, mercury lakes won't spawn in newly generated areas.");
	        
	        // GENERAL SETTINGS
	        
	        Property minionsAttackCreepersProp = Dissolution.config.get(
	        		Configuration.CATEGORY_GENERAL, 
	        		"minionsAttackCreepers",
	        		true,
	        		"If set to true, minions will attack creepers (and probably die in the process) (default: They didn't deserve to live anyway)");
	        
	        Property doSablePopProp = Dissolution.config.get(
	        		Configuration.CATEGORY_GENERAL,
	        		"doSablePop",
	        		true,
	        		"Whether output stacks from the extractor should spawn items in world when there is no appropriate container (default: true)");

	        // CLIENT SETTINGS
	        
	        Property shadersProp = Dissolution.config.get(
	        		Configuration.CATEGORY_CLIENT, 
	        		"useShaders", 
	        		true, 
	        		"Whether this mod should use shaders to try to make things prettier (default: true)");
	        
	        Property anchorsXRayProp = Dissolution.config.get(
	        		Configuration.CATEGORY_CLIENT,
	                "anchorsXRay", // Property name
	                false, // Default value
	                "Whether soul anchors should be visible through blocks to ghost players (graphical glitches might occur) (default: false)");
	        
	        Property showSoulCompassProp = Dissolution.config.get(
	        		Configuration.CATEGORY_CLIENT,
	                "showSoulCompass", // Property name
	                true, // Default value
	                "Whether the HUD pointing to respawn locations should appear (default: true)");
	        
	        Property showAnchorsInSoulCompassProp = Dissolution.config.get(
	        		Configuration.CATEGORY_CLIENT,
	                "showAnchorsInSoulCompass", // Property name
	                true, // Default value
	                "Whether soul anchors should have an indicator in the soul compass HUD (default: true)");
	        
	        // Updating configuration file to v1.1
	        if(versionProp.getDouble() < 1.1) {
	        	shouldRespawnInNetherProp.set(false);
	        	versionProp.set(1.1);
	        }

        	anchorsXRay = anchorsXRayProp.getBoolean();
	        doSableDrop = doSablePopProp.getBoolean();
	        invisibleGhosts = invisibleGhostProp.getBoolean();
        	flightMode = flightModeProp.getInt();
        	minionsAttackCreepers = minionsAttackCreepersProp.getBoolean();
	        respawnInNether = shouldRespawnInNetherProp.getBoolean();
	        respawnDimension = respawnDimensionProp.getInt();
	        skipDeathScreen = shouldShowDeathScreenProp.getBoolean();
	        soulCompass = showSoulCompassProp.getBoolean();
	        soulCompassAnchors = showAnchorsInSoulCompassProp.getBoolean();
	        spawnMercuryLakes = spawnMercuryLakesProp.getBoolean();
	        useShaders = shadersProp.getBoolean();
	        wowRespawn = wowRespawnProp.getBoolean();
	        interactableBlocksProp.getArrayEntryClass();
	    } finally {
	        if (Dissolution.config.hasChanged()) Dissolution.config.save();
	    }
	}
}
