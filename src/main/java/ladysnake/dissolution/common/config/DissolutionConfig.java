package ladysnake.dissolution.common.config;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.Reference;
import net.minecraftforge.common.config.Config;

import java.io.File;
import java.util.Arrays;

public class DissolutionConfig {
	
//	public static Blocks blocks = new Blocks();
	
	public static Client client = new Client();

//	public static Dialogues dialogues = new Dialogues();
	
	public static Entities entities = new Entities();
	
	public static Respawn respawn = new Respawn();
	
	public static Ghost ghost = new Ghost();
	
	public static WorldGen worldGen = new WorldGen();

	@Config.RequiresWorldRestart
	public static DissolutionConfigManager.EnforcedSoulStrength enforcedSoulStrength = DissolutionConfigManager.EnforcedSoulStrength.NONE;
//	public static Wip wip = new Wip();
	
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

		public boolean useShaders = true;
		
		public boolean soulCompass = true;

		public boolean showLamentStones = true;
		
	}

	public static class Dialogues {
		//		@Config.LangKey("config.dissolution.dialogues.broadcastPlayerDialogue")
//		@Config.Comment("If set to true, every dialogue choice made by the player will be broadcasted to all other players")
//		public boolean broadcastPlayerDialogue = false;
//
//		@Config.LangKey("config.dissolution.dialogues.broadcastMajorNPCDialogue")
//		@Config.Comment("If set to true, dialogues emitted by global entities (gods) will be broadcasted to all players")
//		public boolean broadcastMajorNPCDialogue = false;
	}
	
	public static class Entities {
		
		public boolean minionsAttackCreepers = true;

		public boolean lockPlayerCorpses = false;

		public boolean bodiesDespawn = true;

	}

	public static class Ghost {
		
		public DissolutionConfigManager.FlightModes flightMode = DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT;
		
		public boolean invisibleGhosts = false;

		public String[] authorizedBlocks = new String[] {"minecraft:lever", "minecraft:.*door",
				"minecraft:wooden_button"};

		public String[] authorizedEntities = new String[] {};
		
	}

	public static class Respawn {

		public boolean wowLikeRespawn = false;

		public boolean bodiesHoldInventory = true;

		public boolean respawnInNether = false;

		public int respawnDimension = -1;

		public IIncorporealHandler.CorporealityStatus respawnCorporealityStatus = IIncorporealHandler.CorporealityStatus.SOUL;

		public boolean skipDeathScreen = false;

	}

	public static class WorldGen {

		public int spawnLamentStonesFreq = 50;

	}

	public static class Wip {

	}
	
}
