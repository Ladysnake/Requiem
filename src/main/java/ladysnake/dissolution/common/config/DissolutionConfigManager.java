package ladysnake.dissolution.common.config;

import com.google.common.collect.ImmutableSet;
import ladylib.config.ConfigUtil;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.networking.ConfigMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public final class DissolutionConfigManager {

    public static Configuration config;
    private static final ImmutableSet<Pattern> GHOST_HUNTER_WHITELIST = ImmutableSet.of();
    private static ImmutableSet<Pattern> BLOCK_WHITELIST;
    private static ImmutableSet<Pattern> POSSESSION_BLACKLIST;
    private static ImmutableSet<Pattern> POSSESSION_WHITELIST;
    static Set<ConfigCategory> rootCategories;
    public static Map<String, Property> syncedProps;
    /** Saves local config values */
    public static Map<Property, String> backupProps;

    public static Set<ConfigCategory> getRootCategories() {
        return rootCategories;
    }

    public static boolean isFlightSetTo(FlightModes flightMode) {
        return FlightModes.CUSTOM_FLIGHT == flightMode;
    }

    public static boolean canEctoplasmInteractWith(Block block) {
        if (block instanceof ISoulInteractable) {
            return true;
        }
        String name = String.valueOf(block.getRegistryName());
        for (Pattern checker : BLOCK_WHITELIST) {
            if (checker.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEntityBlacklisted(Entity entity) {
        String name = String.valueOf(EntityList.getKey(entity));
        for (Pattern checker : POSSESSION_BLACKLIST) {
            if (checker.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEntityWhitelisted(EntityEntry entity) {
        String name = String.valueOf(entity.getRegistryName());
        for (Pattern checker : POSSESSION_WHITELIST) {
            if (checker.matcher(name).matches()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEctoplasmImmuneTo(Entity entity) {
        if (entity instanceof ISoulInteractable) {
            return false;
        }
        if (GHOST_HUNTER_WHITELIST.isEmpty()) {
            return true;
        }
        Class<? extends Entity> entityClass = entity.getClass();
        EntityEntry entityEntry = EntityRegistry.getEntry(entityClass);
        if(entityEntry == null || entityEntry.getRegistryName() == null) {
            return true;
        }
        for(Pattern checker : GHOST_HUNTER_WHITELIST) {
            if(checker.matcher(entityEntry.getRegistryName().toString()).matches()) {
                return false;
            }
        }
        return true;
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
        config = new Configuration(configFile, String.valueOf(Ref.CONFIG_VERSION));
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
        BLOCK_WHITELIST = buildConfigList(Dissolution.config.ghost.authorizedBlocks);
        POSSESSION_BLACKLIST = buildConfigList(Dissolution.config.ghost.possessionBlacklist);
        POSSESSION_WHITELIST = buildConfigList(Dissolution.config.ghost.possessionWhitelist);
    }

    /**
     * If this config's last version is outdated, attempts to update defaults
     */
    private static void updateConfig() {
        // Updating configuration file to v3.0 (dissolution indev 5.2)
        if (config.hasKey("don't touch that", "version")) {
            Dissolution.LOGGER.warn("Config file is out of date. Replacing with new format. A backup will be created.");
            resetConfig(config.getConfigFile());
        }

        // Updating configuration file to v3.1 (dissolution indev 5.3)
        if (isBehind(config.getLoadedConfigVersion(), 3.1)) {
            Dissolution.LOGGER.info("Updating config from " + config.getLoadedConfigVersion() + " to v3.1");
            config.getCategory("client").remove("showLamentStones");
        }

        // Updating configuration file to v4.0 (dissolution 0.1.3)
        if (isBehind(config.getLoadedConfigVersion(), 4.0)) {
            Dissolution.LOGGER.info("Updating config from {} to v4.0", config.getLoadedConfigVersion());
            config.get("respawn", "skipDeathScreen", true).set(true);
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
            Path configPath = configFile.toPath();
            Path backup = Paths.get(configFile.getParent(), Ref.MOD_NAME + "_backup.txt");
            for (int i = 0; Files.exists(backup); i++) {
                backup = backup.resolveSibling(Ref.MOD_NAME + "_backup" + i + ".txt");
            }
            Files.copy(configPath, backup);
            Files.delete(configPath);
            config = new Configuration(configFile, String.valueOf(Ref.CONFIG_VERSION));
        } catch (IOException e) {
            Dissolution.LOGGER.error("Could not reset config !", e);
        }
    }

    /**
     * Sends a packet to sync required fields with the client
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayerMP playerMP = (EntityPlayerMP) event.player;
        if(playerMP.server.isSinglePlayer()) {
            return;      // don't sync with yourself, silly
        }
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, Property> entry : syncedProps.entrySet()) {
            values.put(entry.getKey(), entry.getValue().getString());
        }
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
        if (event.getModID().equals(Ref.MOD_ID)) {
            load();
            config.save();
        }
    }

    private static ImmutableSet<Pattern> buildConfigList(String[] entries) {
        ImmutableSet.Builder<Pattern> builder = ImmutableSet.builder();
        for (String entry : entries) {
            builder.add(ConfigUtil.wildcardToRegex(entry));
        }
        return builder.build();
    }

    public enum FlightModes {
        CUSTOM_FLIGHT,
        CREATIVE_FLIGHT,
        SPECTATOR_FLIGHT,
        NO_FLIGHT
    }

    public enum EnforcedSoulStrength {
        DEFAULT, TRUE, FALSE;

        public boolean getValue(boolean defaultStrength) {
            return (defaultStrength && (this == DEFAULT)) || (this == TRUE);
        }
    }
}
