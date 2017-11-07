package ladysnake.dissolution.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class DissolutionConfigManager {
	
	private static ImmutableSet<Class<? extends EntityMob>> TARGET_BLACKLIST;
	private static ImmutableSet<Pattern> GHOST_HUNTER_WHITELIST;
	private static ImmutableSet<Pattern> BLOCK_WHITELIST;

	public enum FlightModes {
		NO_FLIGHT,
		CUSTOM_FLIGHT,
		CREATIVE_FLIGHT,
		SPECTATOR_FLIGHT
	}

	public enum EnforcedSoulStrength {
		NONE, STRONG, WEAK;
		EnforcedSoulStrength() {

		}

		public boolean getValue(boolean defaultStrength) {
			return (defaultStrength && (this == NONE)) || (this == STRONG);
		}
	}
	
	public static boolean isFlightSetTo(FlightModes flightMode) {
		return DissolutionConfig.ghost.flightMode == flightMode;
	}
	
	public static boolean isEntityBlacklistedFromMinionAttacks(EntityMob EntityIn) {
		return TARGET_BLACKLIST.contains(EntityIn.getClass());
	}

	@SuppressWarnings("ConstantConditions")
	public static boolean canEctoplasmInteractWith(Block block) {
		if(block instanceof ISoulInteractable)
			return true;
		String name = block.getRegistryName().toString();
		return !BLOCK_WHITELIST.isEmpty() && BLOCK_WHITELIST.stream().anyMatch(p -> p.matcher(name).matches());
	}

	public static boolean canEctoplasmBeAttackedBy(Entity entity) {
		if(entity instanceof ISoulInteractable)
			return true;
		if(GHOST_HUNTER_WHITELIST.isEmpty()) return false;
		Class<? extends Entity> entityClass = entity.getClass();
		EntityEntry entityEntry = EntityRegistry.getEntry(entityClass);
		return entityEntry != null && entityEntry.getRegistryName() != null &&
				GHOST_HUNTER_WHITELIST.stream().anyMatch(p -> p.matcher(entityEntry.getRegistryName().toString()).matches());
	}

	public static boolean canEctoplasmInteractWith(Item item) {
		return item == ModItems.DEBUG_ITEM;
	}

	static void init() {
		buildEctoplasmAttackWhitelist();
		buildMinionAttackBlacklist();
		buildBlockWhitelist();
	}
	
	static void updateConfig(File configFile) {

		Configuration config = new Configuration(configFile);
		Property versionProp = config.get(
	       		"Don't touch that", 
	       		"version", 
	       		Reference.CONFIG_VERSION, 
	       		"The version of this configuration file. Don't modify this number unless you want your changes randomly reset.");
		Set<String> toReplace = new HashSet<>();
	    
	    // Updating configuration file to v2.0 (dissolution v0.4.2.3)
	    if(versionProp.getDouble() < 2.0) {
	    	try {
				Files.copy(configFile, new File(configFile.getParent(), Reference.MOD_NAME + "_backup.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	if(configFile.delete()) {
				versionProp.set(2.0);
			}
	    }

	    // Updating configuration file to v2.1 (dissolution v0.5)
	    if(versionProp.getDouble() < 2.1) {
	    	toReplace.add("I:flightMode=");
			toReplace.add("wowLikeRespawn");
	    	versionProp.set(2.1);
		}

		updateConfigProp(configFile, toReplace);
		ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);

	    config.save();
	}

	private static void updateConfigProp(File configFile, Collection<String> offendingLine) {
		File temp = new File(configFile.getParentFile(), "temp");
		boolean foundOffender = false;
		try (BufferedReader reader = Files.newReader(configFile, Charset.defaultCharset());
			 BufferedWriter writer = Files.newWriter(temp, Charset.defaultCharset())) {
			String readLine;
			while((readLine = reader.readLine()) != null) {
				if(offendingLine.stream().noneMatch(readLine::contains))
					writer.write(readLine + System.getProperty("line.separator"));
				else
					foundOffender = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(foundOffender && configFile.delete()) {
			if(!temp.renameTo(configFile))
				Logger.getGlobal().warning("Could not edit the config file");
		} else {
			if(!temp.delete())
				temp.deleteOnExit();
		}
	}

	@SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(Reference.MOD_ID)) {
            ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
            buildMinionAttackBlacklist();
			buildEctoplasmAttackWhitelist();
            buildBlockWhitelist();
        }
    }

	private static void buildMinionAttackBlacklist() {
		ImmutableSet.Builder<Class<? extends EntityMob>> builder = ImmutableSet.builder();
		builder.add(AbstractMinion.class);
		if (!DissolutionConfig.entities.minionsAttackCreepers)
			builder.add(EntityCreeper.class);
		TARGET_BLACKLIST = builder.build();
	}

	private static void buildEctoplasmAttackWhitelist() {
		ImmutableSet.Builder<Pattern> builder = ImmutableSet.builder();
		for(String entityName : DissolutionConfig.ghost.authorizedEntities.split("[; ]+")) {
			builder.add(Pattern.compile("^" + entityName + "$"));
		}
		GHOST_HUNTER_WHITELIST = builder.build();
	}

	private static void buildBlockWhitelist() {
		ImmutableSet.Builder<Pattern> builder = ImmutableSet.builder();
		IForgeRegistry<Block> reg = RegistryManager.ACTIVE.getRegistry(Block.class);
		if(reg != null) {
			for (String blockName : DissolutionConfig.ghost.authorizedBlocks.split("[; ]+")) {
				builder.add(Pattern.compile("^" + blockName + "$"));
			}
		}
		BLOCK_WHITELIST = builder.build();
	}
	
}
