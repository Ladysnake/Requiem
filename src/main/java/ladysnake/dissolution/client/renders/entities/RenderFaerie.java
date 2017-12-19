package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.common.entity.SoulType;
import ladysnake.dissolution.common.entity.souls.EntityFaerie;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class RenderFaerie extends RenderWillOWisp<EntityFaerie>{

    public RenderFaerie(RenderManager renderManager) {
        super(renderManager);
    }

    @Nonnull
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityFaerie entity) {
        return entity.isTired() ? SoulType.TIRED_FAERIE.texture : SoulType.FAERIE.texture;
    }
}
