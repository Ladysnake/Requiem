package ladysnake.dissolution.common.registries.modularsetups;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Class describing a specific setup for the {@link TileEntityModularMachine}. Such setups need to be registered on {@link net.minecraftforge.event.RegistryEvent.Register}
 * @author Pyrofab
 * 
 */
public abstract class ModularMachineSetup extends IForgeRegistryEntry.Impl<ModularMachineSetup> {
	
	/**
	 * @return the set of alchemy modules that describes this setup
	 */
	public abstract ImmutableSet<ItemAlchemyModule> getSetup();
	
	public abstract ISetupInstance getInstance(TileEntityModularMachine te);
	
	/**
	 * Returns true if the currently installed modules correspond to this setup
	 * @param installedModules Modules currently installed in the machine
	 */
	public boolean isValidSetup (Set<ItemAlchemyModule> installedModules) {
		return installedModules.size() == getSetup().size() && getSetup().stream()
				.allMatch(module -> 
						installedModules.stream().anyMatch(mod2 -> 
								mod2.getType().isEquivalent(module.getType()) && 
								mod2.getTier() >= module.getTier()));
	}

}
