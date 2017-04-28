package ladysnake.tartaros.client.renders.entities;

import javax.annotation.Nonnull;

import ladysnake.tartaros.common.entity.EntityMinion;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMinion extends RenderLiving<EntityMinion> {

	private ResourceLocation mobTexture = new ResourceLocation("textures/entity/zombie.png");

    public static final Factory FACTORY = new Factory();

    public RenderMinion(RenderManager rendermanagerIn) {
        // We use the vanilla zombie model here and we simply
        // retexture it. Of course you can make your own model
        super(rendermanagerIn, new ModelZombie(), 0.5F);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityMinion entity) {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<EntityMinion> {

        @Override
        public Render<? super EntityMinion> createRenderFor(RenderManager manager) {
            return new RenderMinion(manager);
        }

    }
}
