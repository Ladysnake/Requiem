package ladysnake.dissolution.common.registries.modularsetups;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Set;

/**
 * Class describing a specific setup for the {@link TileEntityModularMachine}. Such setups need to be registered on {@link net.minecraftforge.event.RegistryEvent.Register}
 *
 * @author Pyrofab
 */
public abstract class ModularMachineSetup extends IForgeRegistryEntry.Impl<ModularMachineSetup> {

    /**
     * @return the set of alchemy modules that describes this setup
     */
    public abstract ImmutableSet<ItemAlchemyModule.AlchemyModule> getSetup();

    public abstract ISetupInstance getInstance(TileEntityModularMachine te);

    /**
     * Returns true if the currently installed modules correspond to this setup
     *
     * @param installedModules Modules currently installed in the machine
     */
    public boolean isValidSetup(Set<ItemAlchemyModule.AlchemyModule> installedModules) {
        return installedModules.size() >= getSetup().size() && getSetup().stream()
                .allMatch(module ->
                        installedModules.stream().anyMatch(mod2 ->
                                mod2.getType().isEquivalent(module.getType()) &&
                                        mod2.getTier() >= module.getTier()));
    }

}
