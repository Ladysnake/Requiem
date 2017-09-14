package ladysnake.dissolution.api;

import java.util.Optional;
import java.util.UUID;

/**
 * The interface providing methods related to the Incorporeal capability
 * @author Pyrofab
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
	 * Whether the player is in soul mode or not
	 * @return true if the player is a ghost
	 */
	boolean isIncorporeal();
	
	void tick();
	
	String getLastDeathMessage();
	
	void setLastDeathMessage(String lastDeath);
	
	void setDisguise(UUID usurpedId);
	
	Optional<UUID> getDisguise();
	
	/**
	 * Makes the player intangible (enables noclip)
	 * @param intangible
	 * @return true if the operation succeeded
	 */
	boolean setIntangible(boolean intangible);
	
	boolean isIntangible();

}
