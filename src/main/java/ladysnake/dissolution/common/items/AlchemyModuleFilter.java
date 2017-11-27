package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.items.ItemAlchemyModule.AlchemyModule;
import net.minecraft.nbt.NBTTagCompound;

public class AlchemyModuleFilter extends AlchemyModuleTypes {

    /**
     * Creates an alchemy module type to be used by modular machines
     *
     * @param maxTier   the number of tiers this module type possesses
     * @param addToList if true, this module and its associated items will be registered automatically
     * @param aliases   a list of other alchemy modules you can use in this module's place
     */
    AlchemyModuleFilter(int maxTier, boolean addToList, AlchemyModuleTypes... aliases) {
        super(maxTier, 0b010, addToList, aliases);
    }

    @Override
    public boolean isCompatible(AlchemyModuleTypes other) {
        return other instanceof AlchemyModuleFilter || super.isCompatible(other);
    }

    @Override
    public AlchemyModule readNBT(NBTTagCompound compound) {
        return new ItemFilterModule.FilterModule(compound);
    }
}
