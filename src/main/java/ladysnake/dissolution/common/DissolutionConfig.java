package ladysnake.dissolution.common;

import java.io.File;

import net.minecraftforge.common.config.Config;

@Config(modid=Reference.MOD_ID, name=Reference.MOD_NAME)
public class DissolutionConfig {
	
	static {
		DissolutionConfigManager.fixConfigTypes(new File("config/Dissolution.cfg"));
	}
	
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
		
		@Config.LangKey("config.dissolution.blocks.doSablePop")
		@Config.Comment("Whether machines should output items in world when there is no appropriate container available")
		public boolean doSablePop = true;
		
	}
	
	public static class Client {
		
		@Config.LangKey("config.dissolution.client.useShaders")
		@Config.Comment("Whether this mod should use shaders as an attempt to make things prettier")
		public boolean useShaders = true;
		
		@Config.LangKey("config.dissolution.client.soulCompass")
		@Config.Comment("Whether the HUD pointing to respawn locations should display")
		public boolean soulCompass = true;
		
	}
	
	public static class Entities {
		
		@Config.LangKey("config.dissolution.entities.minionsAttackCreepers")
		@Config.Comment("If true, minions will attack creepers (and probably die in the process)")
		public boolean minionsAttackCreepers = true;
		
		public List<String> allowedTargets = new ArrayList<>();
		
	}
	
	public static class Respawn {
		
		@Config.LangKey("config.dissolution.respawn.wowLikeRespawn")
		@Config.Comment("If set to true, the player will respawn as a ghost at their spawnpoint. \nThey will then have the choice to go to 0,0 to respawn without stuff or to reach their corpse under 5 minutes.")
		public boolean wowLikeRespawn = true;
		
		@Config.LangKey("config.dissolution.respawn.bodiesDespawn")
		@Config.Comment("If set to false, player bodies will not require any special circumstances to prevent decay.")
		public boolean bodiesDespawn = true;
		
		@Config.LangKey("config.dissolution.respawn.bodiesHoldInventory")
		@Config.Comment("Whether long-lasting player corpses hold their inventory upon death.")
		public boolean bodiesHoldInventory = true;
		
		@Config.LangKey("config.dissolution.respawn.respawnInNether")
		@Config.Comment("Whether players should respawn in a specific dimension when they die")
		public boolean respawnInNether = false;
		
		@Config.LangKey("config.dissolution.respawn.respawnDimension")
		@Config.Comment("If dimension respawn is on, the player will always respawn in this dimension.")
		public int respawnDimension = -1;
		
		@Config.LangKey("config.dissolution.respawn.skipDeathScreen")
		@Config.Comment("Whether players should respawn instantly as souls without showing death screen (could mess with other mods)")
		public boolean skipDeathScreen = false;
		
	}
	
	public static class Ghost {
		
		@Config.LangKey("config.dissolution.ghost.flightMode")
		@Config.Comment("Changes the way ghosts fly.")
		public DissolutionConfigManager.FlightModes flightMode = DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT;
		
		@Config.LangKey("config.dissolution.ghost.invisibleGhosts")
		@Config.Comment("If set to true, dead players will be fully invisible.")
		public boolean invisibleGhosts = false;
		
	}
	
	public static class Worldgen {
		
		@Config.LangKey("config.dissolution.worldgen.spawnMercuryLakesFreq")
		@Config.Comment("A mercury lake has a 1 in N chances to spawn in a given chunk (the higher the number here, the less lakes). -1 to disable.")
		public int spawnMercuryLakesFreq = 100;
		
	}
	
	public static class Wip {
		@Config.Comment("Lets ghosts go through blocks for a few seconds upon left clicking.")
		public boolean enableSoulDash = false;
		
	}
	
}
