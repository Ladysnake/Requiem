package ladysnake.dissolution.common.capabilities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

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
	
	/**
	 * @return the recorded message corresponding to this player last death
	 */
	public String getLastDeathMessage();
	
	public void setLastDeathMessage(String lastDeath);
	
	/**
	 * Temporary status overriding the normal incorporeal one. Does not get saved upon reload.
	 * @param tangible
	 */
	public void setMercuryCandleNearby(boolean tangible);
	
	/**
	 * Temporary status overriding the normal incorporeal one. Does not get saved upon reload.
	 * @param tangible
	 */
	public void setSulfurCandleNearby(boolean intangible);
	
	/**
	 * Determines if there is a mercury candle near this player.
	 * @return true if a soul candle is in a valid radius
	 */
	public boolean isMercuryCandleNearby();
	
	/**
	 * Determines if there is a sulfur candle near this player.
	 * @return true if a soul candle is in a valid radius
	 */
	public boolean isSulfurCandleNearby();
	
	public void tick(PlayerTickEvent event);
	
}
