package ladysnake.requiem.mixin.client.render.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.client.ShadowPlayerFx;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

// Note: this cannot use the right generics because of bridge methods
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererLayerMixin<T extends LivingEntity, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
    public PlayerRendererLayerMixin(EntityRenderDispatcher dispatcher, M model, float shadowRadius) {
        super(dispatcher, model, shadowRadius);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Player rendering hijack part 2
     * We render incorporeal players on a different framebuffer for reuse.
     * The main framebuffer's depth has been previously copied to the alternate's,
     * so rendering should be visually equivalent.
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Nullable
    @Intrinsic
    @Override
    protected RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean bl) {
        return super.getRenderLayer(entity, showBody, translucent, bl);
    }

    @SuppressWarnings("UnresolvedMixinReference")   // the method is injected through the intrinsic above
    @Inject(method = "getRenderLayer", at = @At("RETURN"), cancellable = true)
    private void replaceRenderLayer(T entity, boolean showBody, boolean translucent, boolean bl, CallbackInfoReturnable<RenderLayer> cir) {
        RequiemPlayer player = (RequiemPlayer) entity;
        if (player.asRemnant().isIncorporeal() || player.getDeathSuspender().isLifeTransient()) {
            cir.setReturnValue(ShadowPlayerFx.INSTANCE.getRenderLayer(cir.getReturnValue()));
        }
    }
}
