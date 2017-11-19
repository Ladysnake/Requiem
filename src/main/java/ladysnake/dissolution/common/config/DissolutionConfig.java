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
	@Config.Comment("If set to anything other than \"none\", will force a soul strength upon players and prevent the dialogue from appearing")
	public static DissolutionConfigManager.EnforcedSoulStrength enforcedSoulStrength = DissolutionConfigManager.EnforcedSoulStrength.NONE;

	@Config.Comment("Because some people need dialogue to be explicit")
	public static boolean technicianDialogue = false;
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

		@Config.Comment("Whether this mod should use shaders as an attempt to make things prettier")
		public boolean useShaders = true;

		@Config.Comment("If set to true, a hud element displaying relevant locations will appear when incorporeal")
		public boolean soulCompass = true;

		@Config.Comment("The maximum distance at which lament stones will render on the soul compass. Set it <= 0 to disable")
		public int lamentStonesCompassDistance = 100;

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

		@Config.Comment("If set to false, Eye of the Undead's minions won't do kamikaze attacks on creepers")
		public boolean minionsAttackCreepers = true;

		@Config.Comment("If set to true, only the corpse's owner and admins will be able to interact with it")
		public boolean lockPlayerCorpses = false;

		@Config.Comment("If set to false, player bodies will not require any special circumstances to prevent decay")
		public boolean bodiesDespawn = true;

	}

	public static class Ghost {

		@Config.Comment("Changes the way players fly as souls")
		public DissolutionConfigManager.FlightModes flightMode = DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT;

		@Config.Comment("If set to true, dead players will be fully invisible")
		public boolean invisibleGhosts = false;

		@Config.RangeDouble(min=0D,max=1D)
		@Config.Comment("Any blocks having an average edge length below that value will let souls pass through")
		public double maxThickness = 0.9;

		@Config.Comment("A regex based list of block IDs that ectoplasm can interact with")
		public String[] authorizedBlocks = new String[] {"minecraft:lever", "minecraft:.*door",
				"minecraft:wooden_button"};

		@Config.Comment("A regex based list of entity names that can be hurt by incorporeal players and that can attack ectoplasms")
		public String[] authorizedEntities = new String[] {};

	}

	public static class Respawn {

		@Config.Comment("If set to true, the player will respawn as a ghost at their spawnpoint. They will then have the choice to go to 0,0 to respawn without stuff or to reach their corpse under 5 minutes.")
		public boolean wowLikeRespawn = false;

		@Config.Comment("Whether player corpses hold their owner's inventory upon death")
		public boolean bodiesHoldInventory = true;

		@Config.Comment("Whether players should respawn in a specific dimension when they die")
		public boolean respawnInNether = false;

		@Config.Comment("If dimension respawn is on, the player will always respawn in this dimension")
		public int respawnDimension = -1;

		@Config.Comment("Controls players with strong souls' corporeal state when they respawn")
		public IIncorporealHandler.CorporealityStatus respawnCorporealityStatus = IIncorporealHandler.CorporealityStatus.SOUL;

		@Config.Comment("Whether players should respawn instantly as souls without showing death screen (could mess with other mods)")
		public boolean skipDeathScreen = false;

	}

	public static class WorldGen {

		@Config.Comment("A Lament Stone has a 1 in N chances to spawn in a given chunk (the higher the number here, the less stones). -1 to disable.")
		public int spawnLamentStonesFreq = 50;

	}

	public static class Wip {

	}

}
