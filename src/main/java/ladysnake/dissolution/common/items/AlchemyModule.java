package ladysnake.dissolution.common.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is basically an enum that allows my fellow modders to add nice stuff
 * @author Pyrofab
 *
 */
public class AlchemyModule {
	
	public static final List<AlchemyModule> values = new ArrayList<>();
	
	public static final AlchemyModule CONTAINER = new AlchemyModule("container", 1, 0b001);
	public static final AlchemyModule INTERFACE = new AlchemyModule("alchemical_interface", 1, 0b100);
	public static final AlchemyModule GENERATOR = new AlchemyModule("resonant_generator", 1, 0b011);
	public static final AlchemyModule FILTER = new AlchemyModule("mineral_filter", 3, 0b010);
	public static final AlchemyModule TRIVALENT_FILTER = new AlchemyModule("trivalent_filter", 1, 0b010, FILTER);
	
	public final int maxTier;
	public final String name;
	/**
	 * Indicates which machine slots are used by this module. <br/>
	 * If another module has an incompatible flag, they will not be able to be installed together.
	 */
	public final int slotsTaken;
	private final List<AlchemyModule> aliases;

	public AlchemyModule(String name, int maxTier, int slotsTaken, AlchemyModule... aliases) {
		this.name = name;
		this.maxTier = maxTier;
		this.slotsTaken = slotsTaken;
		this.aliases = Arrays.asList(aliases);
		values.add(this);
	}
	
	public boolean isCompatible(AlchemyModule module) {
		return (this.slotsTaken & module.slotsTaken) == 0;
	}
	
	public String name() {
		return name;
	}
	
	public static List<AlchemyModule> values() {
		return values;
	}
	
	public static AlchemyModule valueOf(String name) {
		return values.stream().filter(mod -> mod.name.equals(name)).findAny().orElse(null);
	}
	
	public boolean isEquivalent(AlchemyModule module) {
		return module.equals(this) || aliases.contains(module);
	}
	
}
