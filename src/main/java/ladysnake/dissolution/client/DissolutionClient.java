package ladysnake.dissolution.client;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.annotation.CalledThroughReflection;
import ladysnake.dissolution.api.v1.event.ItemTooltipCallback;
import ladysnake.dissolution.api.v1.event.client.HotbarRenderCallback;
import ladysnake.dissolution.client.network.ClientMessageHandling;
import ladysnake.dissolution.client.render.entity.PlayerShellEntityRenderer;
import ladysnake.dissolution.common.entity.PlayerShellEntity;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
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
public class DissolutionClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        DissolutionKeyBinding.init();
        EntityRendererRegistry.INSTANCE.register(PlayerShellEntity.class, (r, it) -> new PlayerShellEntityRenderer(r));
        registerCallbacks();
    }

    private void registerCallbacks() {
        ShaderEffectRenderCallback.EVENT.register(DissolutionFx.INSTANCE);
        EntitiesPostRenderCallback.EVENT.register(DissolutionFx.INSTANCE);
        ResolutionChangeCallback.EVENT.register(DissolutionFx.INSTANCE);
        PickEntityShaderCallback.EVENT.register(EntityShaders::pickShader);
        ClientTickCallback.EVENT.register(DissolutionFx.INSTANCE::update);
        ClientTickCallback.EVENT.register(DissolutionKeyBinding::update);
        // Prevents the hotbar from being rendered when the player cannot use items
        HotbarRenderCallback.EVENT.register(tickDelta -> {
            MinecraftClient client = MinecraftClient.getInstance();
            DissolutionPlayer player = (DissolutionPlayer) client.player;
            if (!client.player.isCreative() && player.getRemnantState().isSoul()) {
                Entity possessed = (Entity) player.getPossessionComponent().getPossessedEntity();
                if (possessed == null || !DissolutionEntityTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register(new PossessionTooltipCallback());
    }
}
