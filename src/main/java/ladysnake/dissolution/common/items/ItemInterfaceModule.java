package ladysnake.dissolution.common.items;

import ladysnake.dissolution.client.models.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemInterfaceModule extends ItemAlchemyModule {

    public ItemInterfaceModule() {
        super(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_BOTTOM, 1);
        allModules.put(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_TOP, new ItemAlchemyModule[]{null, this});
    }

    @Override
    public AlchemyModule toModule(BlockCasing.EnumPartType part, TileEntityModularMachine te) {
        return part == BlockCasing.EnumPartType.BOTTOM ? super.toModule() :
                new AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_TOP, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerRender() {
        super.registerRender();
        ResourceLocation model = new ResourceLocation(Reference.MOD_ID, "machine/interface_up");
        modulesModels.put(this.toModule(BlockCasing.EnumPartType.TOP, null), model);
        DissolutionModelLoader.addModel(model, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
    }
}
