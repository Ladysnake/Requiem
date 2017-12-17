package ladysnake.dissolution.common.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.networking.ConfigMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class DissolutionConfigManager {

    public static Configuration config;
    private static ImmutableSet<Class<? extends EntityMob>> TARGET_BLACKLIST;
    private static ImmutableSet<StringChecker> GHOST_HUNTER_WHITELIST;
    private static ImmutableSet<StringChecker> BLOCK_WHITELIST;
    static Set<ConfigCategory> rootCategories;
    public static Map<String, Property> syncedProps;
    /** Saves local config values */
    public static Map<Property, String> backupProps;

    public static Set<ConfigCategory> getRootCategories() {
        return rootCategories;
    }

    public static boolean isFlightSetTo(FlightModes flightMode) {
        return Dissolution.config.ghost.flightMode == flightMode;
    }

    public static boolean isEntityBlacklistedFromMinionAttacks(EntityMob EntityIn) {
        return TARGET_BLACKLIST.contains(EntityIn.getClass());
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean canEctoplasmInteractWith(Block block) {
        if (block instanceof ISoulInteractable)
            return true;
        String name = block.getRegistryName().toString();
        for (StringChecker checker : BLOCK_WHITELIST)
            if (checker.matches(name))
                return true;
        return false;
    }

    public static boolean canEctoplasmBeAttackedBy(Entity entity) {
        if (entity instanceof ISoulInteractable)
            return true;
        if (GHOST_HUNTER_WHITELIST.isEmpty()) return false;
        Class<? extends Entity> entityClass = entity.getClass();
        EntityEntry entityEntry = EntityRegistry.getEntry(entityClass);
        if(entityEntry == null || entityEntry.getRegistryName() == null) return false;
        for(StringChecker checker : GHOST_HUNTER_WHITELIST)
            if(checker.matches(entityEntry.getRegistryName().toString()))
                return true;
        return false;
    }

    public static boolean canEctoplasmInteractWith(Item item) {
        return item == ModItems.DEBUG_ITEM;
    }

    /**
     * Called once during the initialization phase to initialize the config system
     *
     * @param configFile the file in which the config information is stored
     */
    public static void init(File configFile) {
        config = new Configuration(configFile, String.valueOf(Reference.CONFIG_VERSION));
        rootCategories = new TreeSet<>(Comparator.comparing(ConfigCategory::getName));
        syncedProps = new HashMap<>();
        backupProps = new HashMap<>();
        updateConfig();
        load();
        config.save();
    }

    /**
     * Called once during initialization and then every time the config needs to be reloaded
     */
    public static void load() {
        DissolutionConfigReader.readAndInitializeConfig(config);
        buildEctoplasmAttackWhitelist();
        buildMinionAttackBlacklist();
        buildBlockWhitelist();
    }

    /**
     * If this config's last version is outdated, attempts to update defaults
     */
    private static void updateConfig() {
        // Updating configuration file to v3.0 (dissolution v0.5.2)
        if (config.hasKey("don't touch that", "version")) {
            Dissolution.LOGGER.warn("Config file is out of date. Replacing with new format. A backup will be created.");
            resetConfig(config.getConfigFile());
        }

        // Updating configuration file to v3.1 (dissolution v0.5.3)
        if (isBehind(config.getLoadedConfigVersion(), 3.1)) {
            Dissolution.LOGGER.info("Updating config from " + config.getLoadedConfigVersion() + " to v3.1");
            config.getCategory("client").remove("showLamentStones");
        }

        config.save();
    }

    private static boolean isBehind(String compared, double reference) {
        try {
            return compared == null || Double.compare(Double.parseDouble(compared), reference) < 0;
        } catch (NumberFormatException e) {
            Dissolution.LOGGER.warn("Someone tempered with the config's version number. Please don't do that.");
            return true;
        }
    }

    private static void resetConfig(File configFile) {
        try {
            Files.copy(configFile, new File(configFile.getParent(), Reference.MOD_NAME + "_backup.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (configFile.delete()) {
            config = new Configuration(configFile, String.valueOf(Reference.CONFIG_VERSION));
        }
    }

    /**
     * Sends a packet to sync required fields with the client
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayerMP playerMP = (EntityPlayerMP) event.player;
        if(playerMP.mcServer.isSinglePlayer()) return;      // don't sync with yourself, silly
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, Property> entry : syncedProps.entrySet())
            values.put(entry.getKey(), entry.getValue().getString());
        PacketHandler.NET.sendTo(new ConfigMessage(values), playerMP);
    }

    @SubscribeEvent
    public static void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Dissolution.LOGGER.info("Restoring client config");
        backupProps.forEach(Property::set);
        backupProps.clear();
        load();
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Reference.MOD_ID)) {
            load();
            config.save();
        }
    }

    private static void buildMinionAttackBlacklist() {
        ImmutableSet.Builder<Class<? extends EntityMob>> builder = ImmutableSet.builder();
        builder.add(AbstractMinion.class);
        if (!Dissolution.config.entities.minionsAttackCreepers)
            builder.add(EntityCreeper.class);
        TARGET_BLACKLIST = builder.build();
    }

    private static void buildEctoplasmAttackWhitelist() {
        ImmutableSet.Builder<StringChecker> builder = ImmutableSet.builder();
        for (String entityName : Dissolution.config.ghost.authorizedEntities)
            builder.add(StringChecker.from(entityName));
        GHOST_HUNTER_WHITELIST = builder.build();
    }

    private static void buildBlockWhitelist() {
        ImmutableSet.Builder<StringChecker> builder = ImmutableSet.builder();
        for (String blockName : Dissolution.config.ghost.authorizedBlocks)
            builder.add(StringChecker.from(blockName));
        BLOCK_WHITELIST = builder.build();
    }

    public enum FlightModes {
        CUSTOM_FLIGHT,
        CREATIVE_FLIGHT,
        SPECTATOR_FLIGHT,
        NO_FLIGHT
    }

    public enum EnforcedSoulStrength {
        NONE, STRONG, WEAK;

        public boolean getValue(boolean defaultStrength) {
            return (defaultStrength && (this == NONE)) || (this == STRONG);
        }
    }
}
