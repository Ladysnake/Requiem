package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.souls.EntityFaerie;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class RenderFaerie extends RenderWillOWisp<EntityFaerie>{
    private static final ResourceLocation FAERIE_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/faerie.png");
    private static final ResourceLocation FAERIE_TIRED_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/faerie_weak.png");

    public RenderFaerie(RenderManager renderManager) {
        super(renderManager);
    }

    @Nonnull
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityFaerie entity) {
        return entity.isTired() ? FAERIE_TIRED_TEXTURE : FAERIE_TEXTURE;
    }
}
