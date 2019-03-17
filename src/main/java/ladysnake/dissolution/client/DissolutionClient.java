package ladysnake.dissolution.client;

import ladysnake.dissolution.api.v1.annotation.CalledThroughReflection;
import ladysnake.dissolution.api.v1.event.client.HotbarRenderCallback;
import ladysnake.dissolution.client.gui.hud.PossessionHud;
import ladysnake.dissolution.client.network.ClientMessageHandling;
import ladysnake.dissolution.client.render.entity.PlayerShellEntityRenderer;
import ladysnake.dissolution.common.entity.PlayerShellEntity;
import ladysnake.satin.api.event.EntitiesPostRenderCallback;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import ladysnake.satin.api.event.ResolutionChangeCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;

@CalledThroughReflection
public class DissolutionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        DissolutionKeyBinding.init();
        ShaderEffectRenderCallback.EVENT.register(DissolutionFx.INSTANCE::renderShaders);
        EntitiesPostRenderCallback.EVENT.register(DissolutionFx.INSTANCE);
        ResolutionChangeCallback.EVENT.register(DissolutionFx.INSTANCE);
        PickEntityShaderCallback.EVENT.register(EntityShaders::pickShader);
        ClientTickCallback.EVENT.register(DissolutionFx.INSTANCE::update);
        ClientTickCallback.EVENT.register(DissolutionKeyBinding::update);
        HotbarRenderCallback.EVENT.register(PossessionHud.INSTANCE::onRenderHotbar);
        EntityRendererRegistry.INSTANCE.register(PlayerShellEntity.class, (r, it) -> new PlayerShellEntityRenderer(r));
    }
}
