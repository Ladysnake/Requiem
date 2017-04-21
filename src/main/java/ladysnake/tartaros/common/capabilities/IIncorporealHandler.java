package ladysnake.tartaros.common.capabilities;

import net.minecraft.entity.player.EntityPlayer;

public interface IIncorporealHandler {
	
	public void setIncorporeal(boolean ghostMode, EntityPlayer p);

	/**
	 * Directly sets the value of the incorporeal capability. Should not be used except for loading data.
	 * @param ghostMode
	 */
	@Deprecated
	public void setIncorporeal(int ghostMode);
	
	public boolean isIncorporeal();
	
	/**
	 * @return The integer that represents the value of the incorporeal capability. Should not be used except for saving data.
	 */
	@Deprecated
	public int getIncorporeal();

}
