package ladysnake.pandemonium.client.render.entity;

import ladysnake.pandemonium.common.entity.MorticianEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MorticianEntityRenderer extends MobEntityRenderer<MorticianEntity, VillagerResemblingModel<MorticianEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/wandering_trader.png");

    public MorticianEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new VillagerResemblingModel<>(0.0F), 0.5F);
        this.addFeature(new HeadFeatureRenderer<>(this));
        this.addFeature(new VillagerHeldItemFeatureRenderer<>(this));
    }

    @Override
    public Identifier getTexture(MorticianEntity entity) {
        return TEXTURE;
    }

    protected void scale(MorticianEntity morticianEntity, MatrixStack matrixStack, float f) {
        matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
