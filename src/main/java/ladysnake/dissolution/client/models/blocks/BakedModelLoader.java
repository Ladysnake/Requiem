package ladysnake.dissolution.client.models.blocks;

import java.util.HashMap;
import java.util.Map;

import ladysnake.dissolution.common.Reference;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BakedModelLoader implements ICustomModelLoader {
	
	private Map<String, IModel> models;
	
	public BakedModelLoader() {
		models = new HashMap<>();
		models.put(CableBakedModel.LOCATION_NAME, (state, format, mapper) -> new CableBakedModel());
		models.put(DistillatePipeBakedModel.LOCATION_NAME, (state, format, mapper) -> new DistillatePipeBakedModel());
		models.put(ModularMachineBakedModel.LOCATION_NAME, (state, format, mapper) -> new ModularMachineBakedModel());
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return modelLocation.getResourceDomain().equals(Reference.MOD_ID) && models.containsKey(modelLocation.getResourcePath());
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		return models.get(modelLocation.getResourcePath());
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {}

}
