package ladysnake.dissolution.client;

import ladysnake.dissolution.client.network.ClientMessageHandling;
import ladysnake.dissolution.lib.client.shader.ShaderHelper;
import net.fabricmc.api.ClientModInitializer;

public class DissolutionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        ShaderHelper.init();
    }
}
