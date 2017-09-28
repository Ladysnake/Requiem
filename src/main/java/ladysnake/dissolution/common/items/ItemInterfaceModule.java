package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.util.ResourceLocation;

public class ItemInterfaceModule extends ItemAlchemyModule {

    public ItemInterfaceModule() {
        super(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_BOTTOM, 1);
        allModules.put(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_TOP, new ItemAlchemyModule[]{null, this});
        modulesModels.put(this.toModule(BlockCasing.EnumPartType.TOP, null), new ResourceLocation(Reference.MOD_ID,
                "machine/alchemical_interface_top"));
    }

    @Override
    public AlchemyModule toModule(BlockCasing.EnumPartType part, TileEntityModularMachine te) {
        return part == BlockCasing.EnumPartType.BOTTOM ? super.toModule() :
                new AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_TOP, this.getTier());
    }
}
