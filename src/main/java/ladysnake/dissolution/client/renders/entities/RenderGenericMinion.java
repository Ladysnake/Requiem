package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.common.entity.minion.EntityGenericMinion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RenderGenericMinion extends Render<EntityGenericMinion> {
    public RenderGenericMinion(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(@Nonnull EntityGenericMinion entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity.getDelegate()).doRender(entity.getDelegate(), x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityGenericMinion entity) {
        return null;
    }
}
