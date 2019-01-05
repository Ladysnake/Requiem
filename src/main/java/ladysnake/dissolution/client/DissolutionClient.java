package ladysnake.dissolution.client;

import ladysnake.dissolution.client.network.ClientMessageHandling;
import ladysnake.dissolution.lib.client.event.ClientLoadingEvent;
import ladysnake.dissolution.lib.client.event.RenderEvent;
import ladysnake.dissolution.lib.client.shader.ShaderHelper;
import ladysnake.dissolution.lib.misc.CalledThroughReflection;
import net.fabricmc.api.ClientModInitializer;

@CalledThroughReflection
public class DissolutionClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        ClientLoadingEvent.RESOURCE_MANAGER.register(ShaderHelper::init);
        RenderEvent.SHADER_EFFECT.register(ShaderHandler.INSTANCE);
    }
}
