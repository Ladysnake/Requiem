package ladysnake.dissolution.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import ladysnake.dissolution.common.entity.EntityWanderingSoul;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class DissolutionConfigManager {
	
	private static ImmutableSet<Class<? extends EntityMob>> TARGET_BLACKLIST;

	public enum FlightModes {
		NO_FLIGHT,
		CUSTOM_FLIGHT,
		CREATIVE_FLIGHT,
		SPECTATOR_FLIGHT
	}
	
	public static boolean isFlightEnabled(FlightModes flightMode) {
		return DissolutionConfig.ghost.flightMode == flightMode;
	}
	
	public static boolean isEntityBlacklistedFromMinionAttacks(EntityMob EntityIn) {
		return TARGET_BLACKLIST.contains(EntityIn.getClass());
	}
	
	static void loadConfig(File configFile) {

		Configuration config = new Configuration(configFile);
		Property versionProp = config.get(
	       		"Don't touch that", 
	       		"version", 
	       		Reference.CONFIG_VERSION, 
	       		"The version of this configuration file. Don't modify this number unless you want your changes randomly reset.");
		 
	    
	    // Updating configuration file to v2.0 (dissolution v0.4.2.3)
	    if(versionProp.getDouble() < 2.0) {
	    	try {
				Files.copy(configFile, new File(configFile.getParent(), Reference.MOD_NAME + "_backup.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	if(configFile.delete()) {
				ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
				versionProp.set(2.0);
			}
	    }
	    
        buildMinionAttackBlacklist();
	    
	    config.save();
	    
	}

	@SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.getModID().equals(Reference.MOD_ID))
        {
            ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
            buildMinionAttackBlacklist();
        }
    }
	
	static void fixConfigTypes(File configFile) {
    	File temp = new File(configFile.getParentFile(), "temp");
    	boolean foundOffender = false;
    	try (BufferedReader reader = Files.newReader(configFile, Charset.defaultCharset()); 
    			BufferedWriter writer = Files.newWriter(temp, Charset.defaultCharset())) {
    		String offendingLine = "I:flightMode=";
    		String readLine;
    		while((readLine = reader.readLine()) != null) {
    			if(!readLine.contains(offendingLine))
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
	
	private static void buildMinionAttackBlacklist() {
		ImmutableSet.Builder<Class<? extends EntityMob>> builder = ImmutableSet.builder();
		builder.add(EntityWanderingSoul.class);
		if (!DissolutionConfig.entities.minionsAttackCreepers)
			builder.add(EntityCreeper.class);
		TARGET_BLACKLIST = builder.build();
	}
	
}
