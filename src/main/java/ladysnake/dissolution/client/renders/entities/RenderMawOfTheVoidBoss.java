package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.common.entity.BossMawOfTheVoid;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderWither;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderMawOfTheVoidBoss extends RenderBiped<BossMawOfTheVoid> {

	public RenderMawOfTheVoidBoss(RenderManager renderManagerIn, ModelBiped modelBipedIn, float shadowSize) {
		super(renderManagerIn, modelBipedIn, shadowSize);
	}
	
	public static class Factory implements IRenderFactory<BossMawOfTheVoid> {

        @Override
        public Render<BossMawOfTheVoid> createRenderFor(RenderManager manager) {
        		return new RenderBiped(manager, new ModelBiped(), 1.0f);
        }

    }

}
