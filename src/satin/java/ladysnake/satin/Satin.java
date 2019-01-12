package ladysnake.satin;

import ladysnake.satin.client.event.ClientLoadingEvent;
import ladysnake.satin.client.shader.ShaderHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Satin implements ClientModInitializer {
    public static final String MOD_ID = "satin";
    public static final Logger LOGGER = LogManager.getLogger("Satin");

    public static boolean isDevEnv() {
        return FabricLauncherBase.getLauncher().isDevelopment();
    }

    @Override
    public void onInitializeClient() {
        ClientLoadingEvent.RESOURCE_MANAGER.register(ShaderHelper::init);
    }
}
