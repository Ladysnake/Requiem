package ladysnake.pandemonium.client;

import ladysnake.pandemonium.client.handler.HeadDownTransformHandler;
import ladysnake.pandemonium.client.render.entity.PlayerShellEntityRenderer;
import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.ApplyCameraTransformsCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.satin.api.event.EntitiesPostRenderCallback;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.ShulkerEntityRenderer;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.util.Identifier;

@CalledThroughReflection
public class PandemoniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        FractureKeyBinding.init();
        ApplyCameraTransformsCallback.EVENT.register(new HeadDownTransformHandler());
        EntityRendererRegistry.INSTANCE.register(PandemoniumEntities.PLAYER_SHELL, (r, it) -> new PlayerShellEntityRenderer(r));
        registerCallbacks();
    }

    private void registerCallbacks() {
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register(new PossessionTooltipCallback());
        ClientTickCallback.EVENT.register(FractureKeyBinding::update);
        PickEntityShaderCallback.EVENT.register((camera, loadShaderFunc, appliedShaderGetter) -> {
            if (camera instanceof WaterCreatureEntity) {
                loadShaderFunc.accept(RequiemFx.FISH_EYE_SHADER_ID);
            }
        });
        CrosshairRenderCallback.EVENT.unregister(new Identifier("requiem:enderman_color"));
        EntitiesPostRenderCallback.EVENT.register((camera, frustum, tickDelta) -> {
            if (!camera.isThirdPerson()) {
                MinecraftClient client = MinecraftClient.getInstance();
                assert client.player != null;
                Entity possessed = ((RequiemPlayer)client.player).asPossessor().getPossessedEntity();
                if (possessed instanceof ShulkerEntity) {
                    ShulkerEntity shulker = (ShulkerEntity) possessed;
                    EntityRenderDispatcher renderManager = client.getEntityRenderManager();
                    ShulkerEntityRenderer renderer = (ShulkerEntityRenderer) renderManager.getRenderer(shulker);
                    if (renderer != null) {
                        ShulkerEntityModel<?> model = renderer.getModel();
                        ModelPart nerdFace = model.getHead();
                        nerdFace.visible = false;
                        // TODO update to blaze3D
//                        renderManager.render(shulker, tickDelta, true);
                        nerdFace.visible = true;
                    }
                }
            }
        });
    }
}
