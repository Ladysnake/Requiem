package ladysnake.dissolution.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

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
	List<Soul> removeAll(SoulTypes... filter);
	
	/**
	 * Sets the size of this soul inventory
	 * @param size
	 * @return if the size of the new inventory is too small, all the elements that were removed
	 */
	List<Soul> setSize(int size);

	/**
	 * @param soulType
	 * @return the number of soul of this specific type this inventory contains
	 */
	long getSoulCount(SoulTypes... filter);

	/**
	 * 
	 * @return an immutable version of this inventory's content
	 */
	ImmutableList<Soul> getSoulList();
}
