package ladysnake.pandemonium.client;

import ladysnake.pandemonium.Pandemonium;
import ladysnake.pandemonium.client.render.entity.PlayerShellEntityRenderer;
import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.util.Identifier;

@CalledThroughReflection
public class PandemoniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        EntityRendererRegistry.INSTANCE.register(PandemoniumEntities.PLAYER_SHELL, (r, it) -> new PlayerShellEntityRenderer(r));
        ClientTickEvents.END_WORLD_TICK.register(Pandemonium::tickAnchors);
        registerCallbacks();
    }

    private void registerCallbacks() {
        CrosshairRenderCallback.EVENT.unregister(new Identifier("requiem:enderman_color"));
    }
}
