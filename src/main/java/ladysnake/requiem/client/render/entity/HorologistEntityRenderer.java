package ladysnake.requiem.client.render.entity;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.entity.HorologistEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class HorologistEntityRenderer extends MobEntityRenderer<HorologistEntity, VillagerResemblingModel<HorologistEntity>> {
    private static final Identifier TEXTURE = Requiem.id("textures/entity/horologist.png");

    public HorologistEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new VillagerResemblingModel<>(0.0F), 0.5F);
        this.addFeature(new HeadFeatureRenderer<>(this));
        this.addFeature(new VillagerHeldItemFeatureRenderer<>(this));
    }

    @Override
    public Identifier getTexture(HorologistEntity HorologistEntity) {
        return TEXTURE;
    }

    @Override
    protected void scale(HorologistEntity HorologistEntity, MatrixStack matrixStack, float f) {
        final float scale = 0.9375F;
        matrixStack.scale(scale, scale, scale);
    }
}
