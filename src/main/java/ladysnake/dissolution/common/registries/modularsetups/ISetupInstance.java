package ladysnake.dissolution.common.registries.modularsetups;

import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;

public interface ISetupInstance {
	
	/**
	 * Called each tick when this setup is active in the modular machine
	 * @param The tile entity being ticked
	 */
	void onTick();
	
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
	default void onInteract(EntityPlayer playerIn,  EnumHand hand, BlockCasing.EnumPartType part, EnumFacing facing, float hitX, float hitY, float hitZ) {}
	
	/**
	 * Called when the setup is invalidated
	 * @param te
	 */
	default  void onRemoval() {}
	
	/**
	 * Loads the extra information of this setup from the save
	 * @param te the tile entity being read
	 * @param compound
	 */
	default void readFromNBT(NBTTagCompound compound) {}
	
	/**
	 * Stores this setup's extra information (inventory, power, state, etc.)
	 * @param te the tile entity being saved
	 * @param compound
	 * @return the compound with stored information in it
	 */
	default NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return compound;
	}

	boolean hasCapability(Capability<?> capability, EnumFacing facing, BlockCasing.EnumPartType part);

	<T> T getCapability(Capability<T> capability, EnumFacing facing, BlockCasing.EnumPartType part);

}
