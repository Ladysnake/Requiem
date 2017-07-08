package ladysnake.dissolution.common.capabilities;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ISoulHandler {
	
	/**
	 * Adds a soul to this inventory
	 * @param soul the soul to add
	 * @return true if the soul was added
	 */
	boolean addSoul(Soul soul);
	
	/**
	 * Removes a soul from this inventory
	 * @param soul the soul to remove
	 * @return true if the deletion happened
	 */
	boolean removeSoul(Soul soul);
	
	/**
	 * Removes all souls of a specific type
	 * @param type
	 * @return a list with all removed souls
	 */
	List<Soul> removeAll(SoulTypes type);
	
	/**
	 * @return the number of souls this inventory contains
	 */
	int getSoulCount();

	/**
	 * @param soulType
	 * @return the number of soul of this specific type this inventory contains
	 */
	int getSoulCount(SoulTypes soulType);
	
	Soul get(Predicate<Soul> condition);

	void forEach(Consumer<Soul> action);
	
	List<Soul> getSoulList();
}
