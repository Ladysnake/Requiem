package ladysnake.dissolution.common.init;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("WeakerAccess")
public final class ModBlocks {

    /**
     * Used to register stuff
     */
    static final ModBlocks INSTANCE = new ModBlocks();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerRenders(ModelRegistryEvent event) {
/*
        DissolutionModelLoader.addModel(BlockCasing.PLUG, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
		DissolutionModelLoader.addModel(BlockCasing.PLUG_CHEST, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
		DissolutionModelLoader.addModel(BlockCasing.PLUG_HOPPER, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
    	DissolutionModelLoader.addAllModels(BlockCasing.CASING_BOTTOM, BlockCasing.CASING_TOP, CableBakedModel.INTERSECTION,
				DistillatePipeBakedModel.INTERSECTION);
		DissolutionModelLoader.addModel(CableBakedModel.START, ModelRotation.X0_Y90,
				ModelRotation.X0_Y180, ModelRotation.X0_Y270, ModelRotation.X90_Y0, ModelRotation.X270_Y0);
		DissolutionModelLoader.addModel(DistillatePipeBakedModel.START, ModelRotation.X0_Y90,
				ModelRotation.X0_Y180, ModelRotation.X0_Y270, ModelRotation.X90_Y0, ModelRotation.X270_Y0);
		DissolutionModelLoader.addModel(CableBakedModel.SECTION, ModelRotation.X90_Y0, ModelRotation.X0_Y90);
		DissolutionModelLoader.addModel(DistillatePipeBakedModel.SECTION, ModelRotation.X90_Y0, ModelRotation.X0_Y90);
    	registerSmartRender(POWER_CABLE, CableBakedModel.BAKED_MODEL);
    	registerSmartRender(DISTILLATE_PIPE, DistillatePipeBakedModel.BAKED_MODEL);
    	registerSmartRender(CASING, ModularMachineBakedModel.BAKED_MODEL);
*/
    }

    private ModBlocks() {
    }
}
