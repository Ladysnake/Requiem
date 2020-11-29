package ladysnake.pandemonium.client;

import ladysnake.pandemonium.Pandemonium;
import ladysnake.pandemonium.client.handler.HeadDownTransformHandler;
import ladysnake.pandemonium.client.render.entity.PlayerShellEntityRenderer;
import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.minecraft.client.ApplyCameraTransformsCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.requiem.client.RenderSelfPossessedEntityCallback;
import ladysnake.requiem.client.FractureKeyBinding;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.util.Identifier;

@CalledThroughReflection
public class PandemoniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        ApplyCameraTransformsCallback.EVENT.register(new HeadDownTransformHandler());
        EntityRendererRegistry.INSTANCE.register(PandemoniumEntities.PLAYER_SHELL, (r, it) -> new PlayerShellEntityRenderer(r));
        ClientTickEvents.END_WORLD_TICK.register(Pandemonium::tickAnchors);
        registerCallbacks();
    }

    private void registerCallbacks() {
        ClientTickEvents.END_CLIENT_TICK.register(FractureKeyBinding::update);
        PickEntityShaderCallback.EVENT.register((camera, loadShaderFunc, appliedShaderGetter) -> {
            if (camera instanceof WaterCreatureEntity) {
                loadShaderFunc.accept(RequiemFx.FISH_EYE_SHADER_ID);
            }
        });
        CrosshairRenderCallback.EVENT.unregister(new Identifier("requiem:enderman_color"));
        RenderSelfPossessedEntityCallback.EVENT.register(possessed -> possessed instanceof ShulkerEntity);
    }
}
