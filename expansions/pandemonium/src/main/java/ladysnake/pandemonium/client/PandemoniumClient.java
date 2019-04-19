package ladysnake.pandemonium.client;

import ladysnake.pandemonium.client.render.entity.PlayerShellEntityRenderer;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.entity.WaterCreatureEntity;

@CalledThroughReflection
public class PandemoniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        FractureKeyBinding.init();
        EntityRendererRegistry.INSTANCE.register(PlayerShellEntity.class, (r, it) -> new PlayerShellEntityRenderer(r));
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
    }
}
