package ladysnake.dissolution.common.config;

import net.minecraftforge.common.config.Config;

public class DissolutionConfig {

    public Client client = new Client();

    public Respawn respawn = new Respawn();

    public Ghost ghost = new Ghost();

    @Config.RequiresWorldRestart
    @Config.Comment("If set to anything other than \"none\", will force a soul strength upon players and prevent the dialogue from appearing")
    public DissolutionConfigManager.EnforcedSoulStrength enforcedSoulStrength = DissolutionConfigManager.EnforcedSoulStrength.NONE;

    @Config.Comment("Because some people need dialogue to be explicit")
    public boolean technicianDialogue = false;

    @Config.Comment("Make human flesh consumption add warp if Thaumcraft is installed")
    public boolean warpyFlesh = true;

    public class Client {

        @Config.Comment("Whether this mod should use shaders as an attempt to make things prettier")
        public boolean useShaders = true;

    }

//    public class Dialogues {
//        @Config.LangKey("config.dissolution.dialogues.broadcastPlayerDialogue")
//		@Config.Comment("If set to true, every dialogue choice made by the player will be broadcasted to all other players")
//		public boolean broadcastPlayerDialogue = false;
//
//		@Config.LangKey("config.dissolution.dialogues.broadcastMajorNPCDialogue")
//		@Config.Comment("If set to true, dialogues emitted by global entities (gods) will be broadcasted to all players")
//		public boolean broadcastMajorNPCDialogue = false;
//    }

    public class Ghost {

        @Sync
        @Config.Comment("Changes the way players fly as souls")
        public DissolutionConfigManager.FlightModes flightMode = DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT;

        @Config.Comment("If set to true, dead players will be fully invisible")
        public boolean invisibleGhosts = false;

        @Sync
        @Config.RangeDouble(min = 0D, max = 1D)
        @Config.Comment("Any blocks having an average edge length below that value will let souls pass through")
        public double maxThickness = 0.9;

        @RegExCheck("^/.+/$|^\\w+:\\w+$")
        @Config.Comment("A list of block IDs that ectoplasm can interact with. " +
                "If the name begins and ends with a \'/\', it will be interpreted as a regular expression.")
        public String[] authorizedBlocks = new String[]{"minecraft:lever", "/minecraft:.*door/",
                "minecraft:wooden_button"};

        @Config.Comment("A list of entity names that can be hurt by incorporeal players and that can attack ectoplasms. " +
                "If the name begins and ends with a \'/\', it will be interpreted as a regular expression.")
        public String[] authorizedEntities = new String[]{};

        @Config.Comment("If set to false, incorporeal players won't be able to use the /dissolution stuck command to get back to their spawnpoint")
        public boolean allowStuckCommand = true;

    }

    public class Respawn {

//        @Config.Comment("If set to true, a player corpse will be created each time a player dies")
//        public boolean spawnCorpses = false;

        //        @Config.Comment("Whether player corpses hold their owner's inventory upon death")
//        public boolean bodiesHoldInventory = true;

        @Config.Comment("Whether players should respawn in a specific dimension when they die")
        public boolean respawnInNether = false;

        @Config.Comment("If dimension respawn is on, the player will always respawn in this dimension")
        public int respawnDimension = -1;

        @Config.Comment("Controls players with strong souls' corporeal state when they respawn")
        public EnumCorporealityStatus respawnCorporealityStatus = EnumCorporealityStatus.SOUL;

        @Config.Comment("Whether players should respawn instantly as souls without showing death screen (could mess with other mods)")
        public boolean skipDeathScreen = false;

    }

}
