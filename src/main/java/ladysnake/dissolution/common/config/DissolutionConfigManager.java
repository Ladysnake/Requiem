package ladysnake.dissolution.common.config;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import ladysnake.dissolution.common.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class DissolutionConfigManager {
	
	public static Configuration config;
	
	public static enum FlightModes {
		NO_FLIGHT(-1),
		CUSTOM_FLIGHT(0),
		CREATIVE_FLIGHT(1),
		SPECTATOR_FLIGHT(2);
		
		public final int id;
		
		FlightModes (int id) {
			this.id = id;
		}
	}
	
	public static boolean isFlightEnabled(FlightModes flightMode) {
		if(flightMode == FlightModes.NO_FLIGHT)
			return DissolutionConfig.ghost.flightMode < 0 || DissolutionConfig.ghost.flightMode > 3;
		return DissolutionConfig.ghost.flightMode == flightMode.id;
	}
	
	public static void loadConfig(File configFile) {
		config = new Configuration(configFile);
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
	    	configFile.delete();
	    	ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
	    	versionProp.set(2.0);
	    }
	    
	    config.save();
	}

	@SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
		System.out.println("CONFIG CHANGED");
        if(event.getModID().equals(Reference.MOD_ID))
        {
            ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
        }
    }
	
}
