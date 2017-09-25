package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import net.minecraft.util.ResourceLocation;

public class ItemInterfaceModule extends ItemAlchemyModule {

    public ItemInterfaceModule() {
        super(AlchemyModuleTypes.ALCHEMY_INTERFACE_BOTTOM, 1);
        allModules.put(AlchemyModuleTypes.ALCHEMY_INTERFACE_TOP, new ItemAlchemyModule[]{null, this});
        modulesModels.put(this.toModule(BlockCasing.EnumPartType.TOP), new ResourceLocation(Reference.MOD_ID,
                "machine/" + AlchemyModuleTypes.ALCHEMY_INTERFACE_TOP.name()));
    }

    @Override
    public AlchemyModule toModule(BlockCasing.EnumPartType part) {
        return part == BlockCasing.EnumPartType.BOTTOM ? super.toModule(part) :
                new AlchemyModule(AlchemyModuleTypes.ALCHEMY_INTERFACE_TOP, this.getTier());
    }
}
