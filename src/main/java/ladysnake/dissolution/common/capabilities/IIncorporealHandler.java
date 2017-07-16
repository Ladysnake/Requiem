package ladysnake.dissolution.common.capabilities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

/**
 * The interface providing methods related to the Incorporeal capability
 * @author Fabien
 *
 */
public interface IIncorporealHandler {
	
	public void setSynced (boolean synced);
	
	/**
	 * Whether this handler needs updating from the server
	 * @return true if this handler has already been synchronized at least once
	 */
	boolean isSynced();
	
	/**
	 * Sets the tangibility of the player specified, along with the corresponding attributes
	 * @param ghostMode True if the player should be intangible
	 * @param p The player upon which the change is applied
	 */
	void setIncorporeal(boolean ghostMode);
	
	/**
	 * Temporary status overriding the normal incorporeal one. Does not get saved upon reload.
	 * @param tangible
	 */
	void setSoulCandleNearby(boolean tangible, int CandleType);
	
	/**
	 * Determines if there is a soul candle near this player.
	 * @return true if a soul candle is in a valid radius
	 */
	boolean isSoulCandleNearby(int CandleType);
	
	
	/**
	 * Whether the player is in soul mode or not
	 * @return true if the player is a ghost
	 */
	boolean isIncorporeal();
	
	
	String getLastDeathMessage();
	
	void setLastDeathMessage(String lastDeath);
	
	void tick();
	
	/**
	 * Makes the player intangible (enables noclip)
	 * @param intangible
	 * @return true if the operation succeeded
	 */
	boolean setIntangible(boolean intangible);
	
	boolean isIntangible();

}
