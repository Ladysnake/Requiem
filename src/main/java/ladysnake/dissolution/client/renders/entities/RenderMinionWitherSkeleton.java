package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.client.models.entities.ModelMinionSkeleton;
import ladysnake.dissolution.common.entity.minion.EntityMinionWitherSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderMinionWitherSkeleton extends RenderMinion<EntityMinionWitherSkeleton> {

    private static final ResourceLocation WITHER_SKELETON_MINION_TEXTURE = new ResourceLocation("dissolution:textures/entity/minions/minion_wither_skeleton.png");
    private static final ResourceLocation WITHER_SKELETON_TEXTURE = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

    public RenderMinionWitherSkeleton(RenderManager renderManagerIn) {
        super(renderManagerIn, ModelMinionSkeleton::new, WITHER_SKELETON_TEXTURE, WITHER_SKELETON_TEXTURE);
    }

    @Override
    protected void preRenderCallback(EntityMinionWitherSkeleton minionIn, float partialTickTime) {
        GlStateManager.scale(1.2F, 1.2F, 1.2F);
        super.preRenderCallback(minionIn, partialTickTime);
    }
}
