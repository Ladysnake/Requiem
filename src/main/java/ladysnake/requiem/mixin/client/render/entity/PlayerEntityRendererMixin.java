/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.mixin.client.render.entity;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.entity.internal.VariableMobilityEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher_1, PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
        super(entityRenderDispatcher_1, entityModel_1, float_1);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Player rendering hijack part 1
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Prevents players possessing something from being rendered, and renders their possessed entity
     * instead. This both prevents visual stuttering from position desync and lets mods render the player
     * correctly.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelRender(AbstractClientPlayerEntity renderedPlayer, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int lightmap, CallbackInfo ci) {
        LivingEntity possessedEntity = ((RequiemPlayer) renderedPlayer).asPossessor().getPossessedEntity();
        if (possessedEntity != null) {
            EntityRenderDispatcher renderManager = MinecraftClient.getInstance().getEntityRenderManager();
//            matrices.pop(); // discard transforms made for the player rendering
            // TODO display the nameplate when some gamerule is enabled (yog)
            if (((VariableMobilityEntity)possessedEntity).requiem_isImmovable()) {
                double relativeX = possessedEntity.getX() - renderedPlayer.getX();
                double relativeY = possessedEntity.getY() - renderedPlayer.getY();
                double relativeZ = possessedEntity.getZ() - renderedPlayer.getZ();
                renderManager.render(possessedEntity, relativeX, relativeY, relativeZ, yaw, tickDelta, matrices, vertexConsumers, lightmap);
            } else {
                possessedEntity.bodyYaw = renderedPlayer.bodyYaw;
                possessedEntity.prevBodyYaw = renderedPlayer.prevBodyYaw;
                possessedEntity.yaw = renderedPlayer.yaw;
                possessedEntity.prevYaw = renderedPlayer.prevYaw;
                possessedEntity.pitch = renderedPlayer.pitch;
                possessedEntity.prevPitch = renderedPlayer.prevPitch;
                possessedEntity.headYaw = renderedPlayer.headYaw;
                possessedEntity.prevHeadYaw = renderedPlayer.prevHeadYaw;
                renderManager.render(possessedEntity, 0, 0, 0, yaw, tickDelta, matrices, vertexConsumers, lightmap);
            }
//            matrices.push();
            ci.cancel();
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Player rendering hijack part 2
     * We render incorporeal players on a different framebuffer for reuse.
     * The main framebuffer's depth has been previously copied to the alternate's,
     * so rendering should be visually equivalent.
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/* TODO reimplement using Blaze3D
    @Inject(
            method = "method_4215",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;method_4054(Lnet/minecraft/entity/LivingEntity;DDDFFLnet/minecraft/util/math/MatrixStack;Lnet/minecraft/client/render/LayeredVertexConsumerStorage;)V"
            )
    )
    private void preRender(AbstractClientPlayerEntity renderedPlayer, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, LayeredVertexConsumerStorage vertexConsumers, CallbackInfo info) {
        if (((RequiemPlayer) renderedPlayer).asRemnant().isIncorporeal()) {
            Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
            boolean isObserverRemnant = cameraEntity instanceof RequiemPlayer && ((RequiemPlayer) cameraEntity).asRemnant().getType().isDemon();
            float alpha = isObserverRemnant ? 1.0f : 0.05f;
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, alpha);
            RenderSystem.depthMask(false);
            ShadowPlayerFx.INSTANCE.beginPlayersFbWrite();
        } else if (((RequiemPlayer) renderedPlayer).getDeathSuspender().isLifeTransient()) {
            RenderSystem.color4f(1f, 1f, 1f, 0.5f);
            RenderSystem.depthMask(false);
        }
    }

    @Inject(
            method = "method_4215",
            at = @At(
                    value = "INVOKE",
                    shift = AFTER,
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;method_4054(Lnet/minecraft/entity/LivingEntity;DDDFFLnet/minecraft/util/math/MatrixStack;Lnet/minecraft/client/render/LayeredVertexConsumerStorage;)V"
            )
    )
    private void postRender(AbstractClientPlayerEntity renderedPlayer, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, LayeredVertexConsumerStorage vertexConsumers, CallbackInfo info) {
        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
        RenderSystem.depthMask(true);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
*/

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Hand rendering hijack
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Shadow
    protected abstract void setModelPose(AbstractClientPlayerEntity abstractClientPlayerEntity_1);

    @Inject(method = "renderRightArm", at = @At("HEAD"), cancellable = true)
    private void renderRightArm(MatrixStack matrices, VertexConsumerProvider vertices, int lightmap, AbstractClientPlayerEntity renderedPlayer, CallbackInfo ci) {
        if (requiem_renderPossessedArm(matrices, vertices, renderedPlayer, lightmap, true)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLeftArm", at = @At("HEAD"), cancellable = true)
    private void renderLeftArm(MatrixStack matrices, VertexConsumerProvider vertices, int lightmap, AbstractClientPlayerEntity renderedPlayer, CallbackInfo ci) {
        if (requiem_renderPossessedArm(matrices, vertices, renderedPlayer, lightmap, false)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean requiem_renderPossessedArm(MatrixStack matrices, VertexConsumerProvider vertices, AbstractClientPlayerEntity renderedPlayer, int lightmapCoordinates, boolean rightArm) {
        if (((RequiemPlayer) renderedPlayer).asRemnant().isSoul()) {
            if (((RequiemPlayer) renderedPlayer).asPossessor().isPossessing()) {
                LivingEntity possessed = ((RequiemPlayer) renderedPlayer).asPossessor().getPossessedEntity();
                if (possessed != null) {
                    EntityRenderer<? super LivingEntity> possessedRenderer = MinecraftClient.getInstance().getEntityRenderManager().getRenderer(possessed);
                    // If the mob has an arm, render it instead of the player's
                    if (possessedRenderer instanceof FeatureRendererContext) {
                        Model possessedModel = ((LivingEntityRenderer) possessedRenderer).getModel();
                        if (possessedModel instanceof BipedEntityModel) {
                            @SuppressWarnings("unchecked") BipedEntityModel<LivingEntity> bipedModel = (BipedEntityModel<LivingEntity>) possessedModel;
                            PlayerEntityModel<AbstractClientPlayerEntity> playerModel = this.getModel();
                            ModelPart arm = rightArm ? bipedModel.rightArm : bipedModel.leftArm;
                            this.setModelPose(renderedPlayer);
                            bipedModel.leftArmPose = playerModel.leftArmPose;
                            bipedModel.rightArmPose = playerModel.rightArmPose;
                            bipedModel.handSwingProgress = 0.0F;
                            bipedModel.isSneaking = false;
                            bipedModel.setAngles(possessed, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
                            arm.pitch = 0.0F;
                            arm.render(matrices, vertices.getBuffer(possessedModel.getLayer(possessedRenderer.getTexture(possessed))), lightmapCoordinates, OverlayTexture.DEFAULT_UV);
                        }
                    }
                }
            }
            // prevent rendering a soul's arm regardless
            return true;
        }
        return false;
    }
}