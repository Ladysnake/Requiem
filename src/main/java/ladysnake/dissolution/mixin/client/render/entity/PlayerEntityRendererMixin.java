package ladysnake.dissolution.mixin.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sortme.OptionMainHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher_1, PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
        super(entityRenderDispatcher_1, entityModel_1, float_1);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void cancelRender(AbstractClientPlayerEntity renderedPlayer, double x, double y, double z, float yaw, float tickDelta, CallbackInfo info) {
        if (((DissolutionPlayer)renderedPlayer).getPossessionManager().isPossessing()) {
            info.cancel();
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;DDDFF)V"
            )
    )
    public void preRender(AbstractClientPlayerEntity renderedPlayer, double x, double y, double z, float yaw, float tickDelta, CallbackInfo info) {
        RemnantHandler renderedPlayerRemnantHandler = ((DissolutionPlayer) renderedPlayer).getRemnantHandler();
        if (renderedPlayerRemnantHandler != null && renderedPlayerRemnantHandler.isIncorporeal()) {
            Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
            boolean isObserverRemnant = cameraEntity instanceof DissolutionPlayer && ((DissolutionPlayer) cameraEntity).getRemnantHandler() != null;
            float alpha = isObserverRemnant ? 0.8f : 0.05f;
            GlStateManager.color4f(0.9f, 0.9f, 1.0f, alpha); // Tints souls blue and transparent
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    shift = AFTER,
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;DDDFF)V"
            )
    )
    public void postRender(AbstractClientPlayerEntity renderedPlayer, double x, double y, double z, float yaw, float tickDelta, CallbackInfo info) {
        GlStateManager.color4f(1f, 1f, 1f, 1f);
    }

    @Shadow
    protected abstract void setModelPose(AbstractClientPlayerEntity player);

    @SuppressWarnings("InvalidMemberReference")
    @Inject(method = {"method_4220", "method_4221"}, at = @At("HEAD"), cancellable = true)
    public void renderPossessedHand(AbstractClientPlayerEntity renderedPlayer, CallbackInfo info) {
        RemnantHandler remnantHandler = ((DissolutionPlayer) renderedPlayer).getRemnantHandler();
        if (remnantHandler != null && remnantHandler.isSoul()) {
            if (((DissolutionPlayer) renderedPlayer).getPossessionManager().isPossessing()) {
                LivingEntity possessed = (LivingEntity) ((DissolutionPlayer) renderedPlayer).getPossessionManager().getPossessedEntity();
                if (possessed != null) {
                    EntityRenderer renderer = MinecraftClient.getInstance().getEntityRenderManager().getRenderer(possessed);
                    // If the mob has an arm, render it instead of the player's
                    if (renderer instanceof FeatureRendererContext) {
                        Model model = ((LivingEntityRenderer) renderer).getModel();
                        if (model instanceof BipedEntityModel) {
                            renderer.bindTexture(((AccessibleTextureEntityRenderer) renderer).getTexture(possessed));
                            boolean rightArm = renderedPlayer.getMainHand() == OptionMainHand.RIGHT;
                            GlStateManager.color3f(1.0F, 1.0F, 1.0F);
                            BipedEntityModel playerEntityModel_1 = (BipedEntityModel) model;
                            this.setModelPose(renderedPlayer);
                            GlStateManager.enableBlend();
                            playerEntityModel_1.swingProgress = 0.0F;
                            playerEntityModel_1.isSneaking = false;
                            playerEntityModel_1.field_3396 = 0.0F;
                            //noinspection unchecked
                            playerEntityModel_1.method_17087(possessed, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
                            Cuboid arm = rightArm ? playerEntityModel_1.armRight : playerEntityModel_1.armLeft;
                            arm.pitch = 0.0F;
                            arm.render(0.0625F);
                            GlStateManager.disableBlend();
                        }
                    }
                }
            }
            // prevent rendering a soul's arm regardless
            info.cancel();
        }
    }
}
