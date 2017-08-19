package ladysnake.dissolution.client.models.blocks;

import ladysnake.dissolution.common.Reference;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class BakedModelLoader implements ICustomModelLoader {
	
	public static final PowerCableISBM CABLE_MODEL = new PowerCableISBM();

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return modelLocation.getResourceDomain().equals(Reference.MOD_ID) && 
				CableBakedModel.LOCATION_NAME.equals(modelLocation.getResourcePath());
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		return CABLE_MODEL;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {}

}
