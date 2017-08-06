package ladysnake.dissolution.common.config;

import ladysnake.dissolution.common.Reference;
import net.minecraftforge.common.config.Config;

@Config(modid=Reference.MOD_ID, name=Reference.MOD_NAME)
public class DissolutionConfig {
	
	@Config.Name("block")
	public static Blocks blocks = new Blocks();
	
	@Config.Name("client")
	public static Client client = new Client();
	
	@Config.Name("entities")
	public static Entities entities = new Entities();
	
	@Config.Name("respawn")
	public static Respawn respawn = new Respawn();
	
	@Config.Name("ghost")
	public static Ghost ghost = new Ghost();
	
	@Config.Name("worldgen")
	public static Worldgen worldgen = new Worldgen();
	
	@Config.Name("wip")
	public static Wip wip = new Wip();
	
	public static class Blocks {
		
		@Config.LangKey("config.dissolution:blocks.doSablePop")
		public boolean doSablePop = true;

		@Config.LangKey("config.dissolution:blocks.oneUseWaystone")
		public boolean oneUseWaystone = true;
		
	}
	
	public static class Client {
		
		@Config.LangKey("config.dissolution:client.useShaders")
		public boolean useShaders = true;
		
		@Config.LangKey("config.dissolution:client.soulCompass")
		public boolean soulCompass = true;
		
		@Config.LangKey("config.dissolution:client.soulCompassAnchors")
		public boolean soulCompassAnchors = true;
		
	}
	
	public static class Entities {
		
		@Config.LangKey("config.dissolution:entities.minionsAttackCreepers")
		@Config.RequiresMcRestart
		public boolean minionsAttackCreepers = true;
		
	}
	
	public static class Respawn {
		
		@Config.LangKey("config.dissolution:respawn.wowLikeRespawn")
		public boolean wowLikeRespawn = true;
		
		@Config.LangKey("config.dissolution:respawn.bodiesDespawn")
		public boolean bodiesDespawn = true;
		
		@Config.LangKey("config.dissolution:respawn.bodiesHoldInventory")
		public boolean bodiesHoldInventory = true;
		
		@Config.LangKey("config.dissolution:respawn.respawnInNether")
		public boolean respawnInNether = false;
		
		@Config.LangKey("config.dissolution:respawn.respawnDimension")
		public int respawnDimension = -1;
		
		@Config.LangKey("config.dissolution:respawn.skipDeathScreen")
		public boolean skipDeathScreen = false;
		
	}
	
	public static class Ghost {
		
		@Config.LangKey("config.dissolution:ghost.flightMode")
		@Config.RangeInt(min= -1, max= 3)
		public int flightMode = 0;
		
		@Config.LangKey("config.dissolution:ghost.invisibleGhosts")
		public boolean invisibleGhosts = false;
		
	}
	
	public static class Worldgen {
		
		@Config.LangKey("config.dissolution:worldgen.spawnMercuryLakesFreq")
		public int spawnMercuryLakesFreq = 100;
		
	}
	
	public static class Wip {
		
		public boolean enableSoulDash = false;
		
	}
	
}

	/*
	public static final int NO_FLIGHT = -1;
	public static final int CUSTOM_FLIGHT = 0;
	public static final int CREATIVE_FLIGHT = 1;
	public static final int SPECTATOR_FLIGHT = 2;

	public static boolean anchorsXRay = false;
	public static boolean bodiesDespawn = true;
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
	public static int spawnMercuryLakesFreq = 100;
	public static boolean useShaders = true;
	public static boolean wowRespawn = true;
	
	public static boolean enableSoulDash = false;
	
	public static final String CATEGORY_RESPAWN = "Respawn";
	public static final String CATEGORY_GHOST = "Ghost";
	public static final String CATEGORY_WORLDGEN = "Worldgen";
	public static final String CATEGORY_WIP = "WIP";
	
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
	        		true,
	        		"If set to true, the player will respawn as a ghost at their spawnpoint. They will then have the choice to go to 0,0 to respawn without stuff or to reach their corpse under 5 minutes. (default : true)");
	        
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
	        
	        Property playerBodiesDecayProp = Dissolution.config.get(
	        		CATEGORY_RESPAWN,
	        		"playerBodiesDespawn",
	        		true,
	        		"If set to false, player bodies will not require any special circumstances to prevent decay. (default: true)");
	        
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
	        		spawnMercuryLakesFreq,
	        		"A mercury lake has a 1 in N chances to spawn in a given chunk (the higher the number here, the less lakes). -1 to disable.");
	        
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
	        
	        // WIP SETTINGS
	        
	        Dissolution.config.addCustomCategoryComment(CATEGORY_WIP, "Allows you to enable WIP mechanics. Warning : very susceptible to change and potentially unstable.");
	        
	        Property enableSoulDashProp = Dissolution.config.get(
	        		CATEGORY_WIP,
	        		"enableSoulDash",
	        		enableSoulDash,
	        		"Allows players to left click blocks to be temporarily intangible");
	        
	        // Updating configuration file to v1.1
	        if(versionProp.getDouble() < 1.1) {
	        	shouldRespawnInNetherProp.set(false);
	        	versionProp.set(1.1);
	        }

        	anchorsXRay = anchorsXRayProp.getBoolean();
	        soulCompass = showSoulCompassProp.getBoolean();
	        soulCompassAnchors = showAnchorsInSoulCompassProp.getBoolean();
	        useShaders = shadersProp.getBoolean();
        	
        	bodiesHoldInventory = playerBodiesHoldInventoryProp.getBoolean();
        	bodiesDespawn = playerBodiesDecayProp.getBoolean();
	        respawnInNether = shouldRespawnInNetherProp.getBoolean();
	        respawnDimension = respawnDimensionProp.getInt();
	        skipDeathScreen = shouldShowDeathScreenProp.getBoolean();
	        wowRespawn = wowRespawnProp.getBoolean();
	        
        	doSableDrop = doSablePopProp.getBoolean();
        	minionsAttackCreepers = minionsAttackCreepersProp.getBoolean();

	        invisibleGhosts = invisibleGhostProp.getBoolean();
        	flightMode = flightModeProp.getInt();

        	spawnMercuryLakesFreq = spawnMercuryLakesProp.getInt();
	        
	        enableSoulDash = enableSoulDashProp.getBoolean();
	        
	    } finally {
	        if (Dissolution.config.hasChanged()) Dissolution.config.save();
	    }
	}
}*/
