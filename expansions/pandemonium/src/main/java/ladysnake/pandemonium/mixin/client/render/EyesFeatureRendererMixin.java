package ladysnake.pandemonium.mixin.client.render;

import ladysnake.pandemonium.client.render.entity.ClientWololoComponent;
import ladysnake.pandemonium.common.entity.WololoComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EyesFeatureRenderer.class)
public abstract class EyesFeatureRendererMixin<T extends Entity, M extends EntityModel<T>> {
    @ModifyVariable(method = "render", at = @At("STORE"))
    private VertexConsumer changeLayer(VertexConsumer consumer, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        WololoComponent wololo = WololoComponent.KEY.getNullable(entity);
        if (wololo != null) {
            RenderLayer eyesLayer = ((ClientWololoComponent) wololo).getEyesLayer();
            if (eyesLayer != null) {
                return vertexConsumers.getBuffer(eyesLayer);
            }
        }
        return consumer;
    }
}
