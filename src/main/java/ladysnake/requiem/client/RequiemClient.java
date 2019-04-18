package ladysnake.requiem.client;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.ItemTooltipCallback;
import ladysnake.requiem.api.v1.event.client.HotbarRenderCallback;
import ladysnake.requiem.client.network.ClientMessageHandling;
import ladysnake.requiem.client.render.entity.PlayerShellEntityRenderer;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import ladysnake.satin.api.event.EntitiesPostRenderCallback;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import ladysnake.satin.api.event.ResolutionChangeCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;

@CalledThroughReflection
public class RequiemClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        RequiemKeyBinding.init();
        EntityRendererRegistry.INSTANCE.register(PlayerShellEntity.class, (r, it) -> new PlayerShellEntityRenderer(r));
        registerCallbacks();
    }

    private void registerCallbacks() {
        ShaderEffectRenderCallback.EVENT.register(RequiemFx.INSTANCE);
        EntitiesPostRenderCallback.EVENT.register(RequiemFx.INSTANCE);
        ResolutionChangeCallback.EVENT.register(RequiemFx.INSTANCE);
        PickEntityShaderCallback.EVENT.register(EntityShaders::pickShader);
        ClientTickCallback.EVENT.register(RequiemFx.INSTANCE::update);
        ClientTickCallback.EVENT.register(RequiemKeyBinding::update);
        // Prevents the hotbar from being rendered when the player cannot use items
        HotbarRenderCallback.EVENT.register(tickDelta -> {
            MinecraftClient client = MinecraftClient.getInstance();
            RequiemPlayer player = (RequiemPlayer) client.player;
            if (!client.player.isCreative() && player.getRemnantState().isSoul()) {
                Entity possessed = (Entity) player.getPossessionComponent().getPossessedEntity();
                if (possessed == null || !RequiemEntityTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register(new PossessionTooltipCallback());
    }
}
