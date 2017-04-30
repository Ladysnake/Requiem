package ladysnake.tartaros.common.capabilities;

import net.minecraft.entity.player.EntityPlayer;

public interface IIncorporealHandler {
	
	public void setSynced (boolean synced);
	
	/**
	 * Whether this handler needs updating from the server
	 * @return true if this handler has already been synchronized at least once
	 */
	public boolean isSynced();
	
	/**
	 * Sets the tangibility of the player specified, along with the corresponding attributes
	 * @param ghostMode True if the player should be intangible
	 * @param p The player upon which the change is applied
	 */
	public void setIncorporeal(boolean ghostMode, EntityPlayer p);

	/**
	 * Directly sets the value of the incorporeal capability. Should not be used except for loading data.
	 * @param ghostMode
	 */
	public void setIncorporeal(boolean ghostMode);
	
	/**
	 * Whether the player is in soul mode or not
	 * @return true if the player is a ghost
	 */
	public boolean isIncorporeal();
	
	public String getLastDeathMessage();
	
	public void setLastDeathMessage(String lastDeath);

}
