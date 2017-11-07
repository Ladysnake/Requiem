package ladysnake.dissolution.common;

import net.minecraftforge.common.config.Config;

import java.io.File;

@Config(modid=Reference.MOD_ID, name=Reference.MOD_NAME)
public class DissolutionConfig {
	
	static {
		DissolutionConfigManager.updateConfig(new File("config/Dissolution.cfg"));
	}
	
	@Config.Name("block")
	public static Blocks blocks = new Blocks();
	
	@Config.Name("client")
	public static Client client = new Client();

	@Config.Name("dialogues")
	public static Dialogues dialogues = new Dialogues();
	
	@Config.Name("entities")
	public static Entities entities = new Entities();
	
	@Config.Name("respawn")
	public static Respawn respawn = new Respawn();
	
	@Config.Name("ghost")
	public static Ghost ghost = new Ghost();
	
	@Config.Name("worldGen")
	public static WorldGen worldGen = new WorldGen();
	
	@Config.Name("wip")
	public static Wip wip = new Wip();
	
	public static class Blocks {
		
//		@Config.LangKey("config.dissolution.blocks.doSablePop")
//		@Config.Comment("Whether machines should output items in world when there is no appropriate container available")
//		public boolean doSablePop = true;
		
	}
	
	public static class Client {

/*
		@Config.LangKey("config.dissolution.client.plugsEverywhere")
		@Config.Comment("If set to true, modular machines will display plugs on all faces to preserve performance")
		public boolean plugsEverywhere = false;
*/

		@Config.LangKey("config.dissolution.client.useShaders")
		@Config.Comment("Whether this mod should use shaders as an attempt to make things prettier")
		public boolean useShaders = true;
		
		@Config.LangKey("config.dissolution.client.soulCompass")
		@Config.Comment("Whether the HUD pointing to respawn locations should display")
		public boolean soulCompass = true;

		@Config.LangKey("config.dissolution.client.soulCompassLamentStones")
		@Config.Comment("Whether the soul compass HUD should also display lament stones")
		public boolean showLamentStones = true;
		
	}

	public static class Dialogues {
		@Config.LangKey("config.dissolution.dialogues.enforcedSoulStrength")
		@Config.Comment("If set to anything other than \"none\", will force a soul strength upon players and prevent the dialogue from appearing")
		@Config.RequiresWorldRestart
		public DissolutionConfigManager.EnforcedSoulStrength enforcedSoulStrength = DissolutionConfigManager.EnforcedSoulStrength.NONE;
//		@Config.LangKey("config.dissolution.dialogues.broadcastPlayerDialogue")
//		@Config.Comment("If set to true, every dialogue choice made by the player will be broadcasted to all other players")
//		public boolean broadcastPlayerDialogue = false;
//
//		@Config.LangKey("config.dissolution.dialogues.broadcastMajorNPCDialogue")
//		@Config.Comment("If set to true, dialogues emitted by global entities (gods) will be broadcasted to all players")
//		public boolean broadcastMajorNPCDialogue = false;
	}
	
	public static class Entities {
		
		@Config.LangKey("config.dissolution.entities.minionsAttackCreepers")
		@Config.Comment("If true, minions will attack creepers (and probably die in the process)")
		boolean minionsAttackCreepers = true;
		
	}

	public static class Ghost {
		
		@Config.LangKey("config.dissolution.ghost.flightMode")
		@Config.Comment("Changes the way ghosts fly.")
		DissolutionConfigManager.FlightModes flightMode = DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT;
		
		@Config.LangKey("config.dissolution.ghost.invisibleGhosts")
		@Config.Comment("If set to true, dead players will be fully invisible.")
		public boolean invisibleGhosts = false;

		@Config.LangKey("config.dissolution.ghost.authorizedBlocks")
		@Config.Comment("A list of blocks that players in ectoplasm form can interact with")
		public String authorizedBlocks = "minecraft:lever; minecraft:.*door;" +
				"minecraft:wooden_button";

		@Config.LangKey("config.dissolution.ghost.authorizedEntity")
		@Config.Comment("A list of entities that can attack a player in ectoplasm form")
		public String authorizedEntities = "";
		
	}

	public static class Respawn {

		@Config.LangKey("config.dissolution.respawn.wowLikeRespawn")
		@Config.Comment("If set to true, the player will respawn as a ghost at their spawnpoint. \nThey will then have the choice to go to 0,0 to respawn without stuff or to reach their corpse under 5 minutes.")
		public boolean wowLikeRespawn = false;

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

	public static class WorldGen {

		@Config.LangKey("config.dissolution.worldGen.spawnLamentStones")
		@Config.Comment("A Lament Stone has a 1 in N chances to spawn in a given chunk (the higher the number here, the less stones). -1 to disable.")
		public int spawnLamentStonesFreq = 50;

	}

	public static class Wip {

	}
	
}
