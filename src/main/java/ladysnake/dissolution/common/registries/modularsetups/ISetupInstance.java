package ladysnake.dissolution.common.registries.modularsetups;

import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public interface ISetupInstance {
	
	/**
	 * Called each tick when this setup is active in the modular machine
	 */
	void onTick();
	
	/**
	 * Called when a player interacts with the {@link BlockCasing} hosting the tile entity
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
	 */
	default  void onRemoval() {}
	
	/**
	 * Loads the extra information of this setup from the save
	 * @param compound
	 */
	default void readFromNBT(NBTTagCompound compound) {}
	
	/**
	 * Stores this setup's extra information (inventory, power, state, etc.)
	 * @param compound
	 * @return the compound with stored information in it
	 */
	default NBTTagCompound writeToNBT(NBTTagCompound compound) {
		return compound;
	}

	/**
	 *
	 * @param facing the <em>not adjusted</em> checked face of the machine
	 * @param part top or bottom of the machine
	 * @return true if a plug should appear at this location
	 */
	boolean isPlugAttached(EnumFacing facing, BlockCasing.EnumPartType part);
	
	/**
	 * see {@link #getCapability}
	 */
	boolean hasCapability(Capability<?> capability, EnumFacing facing, BlockCasing.EnumPartType part);

	/**
	 * Proxy method that allows this setup to give capabilities to the machine
	 * @param capability
	 * @param facing the face being checked, <em>compensated</em> with the machine's rotation
	 * @param part
	 * @return
	 */
	<T> T getCapability(Capability<T> capability, EnumFacing facing, BlockCasing.EnumPartType part);

}
