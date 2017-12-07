package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.common.entity.EntityRunicCircle;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderRunicCircle extends Render<EntityRunicCircle> {
    public RenderRunicCircle(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityRunicCircle entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        //TODO cool shit
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityRunicCircle entity) {
        return null;
    }
}
