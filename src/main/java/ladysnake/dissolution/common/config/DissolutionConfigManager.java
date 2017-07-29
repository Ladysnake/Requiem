package ladysnake.dissolution.common.config;

import ladysnake.dissolution.common.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class DissolutionConfigManager {
	
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
		return DissolutionConfig.ghost.flightMode == flightMode.id;
	}

	@SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.getModID().equals(Reference.MOD_ID))
        {
            ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
        }
    }
	
}
