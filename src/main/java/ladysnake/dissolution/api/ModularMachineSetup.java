package ladysnake.dissolution.api;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Class describing a specific setup for the {@link TileEntityModularMachine}. Such setups need to be registered on {@link net.minecraftforge.event.RegistryEvent.Register}
 * @author Pyrofab
 * 
 */
public abstract class ModularMachineSetup extends IForgeRegistryEntry.Impl<ModularMachineSetup> {
	
	public abstract void init(TileEntityModularMachine te);
	
	/**
	 * Called each tick when this setup is active in the modular machine
	 * @param The tile entity being ticked
	 */
	public abstract void onTick(TileEntityModularMachine te);
	
	/**
	 * @return the set of alchemy modules that describes this setup
	 */
	public abstract ImmutableSet<ItemAlchemyModule> getSetup();
	
	/**
	 * Returns true if the currently installed modules correspond to this setup
	 * @param installedModules Modules currently installed in the machine
	 */
	public boolean isValidSetup (Set<ItemAlchemyModule> installedModules) {
		return installedModules.size() == getSetup().size() && getSetup().stream()
				.allMatch(module -> 
						installedModules.stream().anyMatch(mod2 -> 
								mod2.getType().equals(module.getType()) && 
								mod2.getTier() >= module.getTier()));
	}
	
	/**
	 * Called when a player interacts with the {@link BlockCasing} hosting the tile entity
	 * @param te The {@link TileEntityModularMachine} hosting this setup
	 * @param playerIn the player interacting with the casing
	 * @param hand the hand used to interact
	 * @param part 
	 * @param facing
	 * @param hitX
	 * @param hitY
	 * @param hitZ
	 */
	public void onInteract(TileEntityModularMachine te, EntityPlayer playerIn,  EnumHand hand, BlockCasing.EnumPartType part, EnumFacing facing, float hitX, float hitY, float hitZ) {}
	
	/**
	 * Called when the setup is invalidated
	 * @param te
	 */
	public void onRemoval(TileEntityModularMachine te) {}
	
	public void onScheduledUpdate(TileEntityModularMachine te) {}
	
	/**
	 * Loads the extra information of this setup from the save
	 * @param te the tile entity being read
	 * @param compound
	 */
	public void readFromNBT(TileEntityModularMachine te, NBTTagCompound compound) {}
	
	/**
	 * Stores this setup's extra information (inventory, power, state, etc.)
	 * @param te the tile entity being saved
	 * @param compound
	 * @return the compound with stored information in it
	 */
	public NBTTagCompound writeToNBT(TileEntityModularMachine te, NBTTagCompound compound) {
		return compound;
	}

}
