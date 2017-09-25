package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.registries.modularsetups.ModularMachineSetup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is basically an enum that allows my fellow modders to add nice stuff
 * @author Pyrofab
 *
 */
@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public class AlchemyModuleTypes extends IForgeRegistryEntry.Impl<AlchemyModuleTypes> {
	
	public static final List<AlchemyModuleTypes> automatedEntries = new ArrayList<>();
	public static final IForgeRegistry<AlchemyModuleTypes> REGISTRY = new RegistryBuilder<AlchemyModuleTypes>().setName(
			new ResourceLocation(Reference.MOD_ID, "alchemy_modules")).setType(AlchemyModuleTypes.class).create();

	public static final AlchemyModuleTypes ALCHEMY_INTERFACE_TOP = new AlchemyModuleTypes("alchemical_interface_top", 1, 0b100, false);
	public static final AlchemyModuleTypes ALCHEMY_INTERFACE_BOTTOM = new AlchemyModuleTypes("alchemical_interface", 1, 0b100, false);
	public static final AlchemyModuleTypes CLOCHE = new AlchemyModuleTypes("cloche", 1, 0b011);
	public static final AlchemyModuleTypes CONTAINER = new AlchemyModuleTypes("container", 1, 0b001);
	public static final AlchemyModuleTypes CRYSTALLIZER = new AlchemyModuleTypes("crystallizer", 1, 0b010);
	public static final AlchemyModuleTypes GENERATOR = new AlchemyModuleTypes("resonant_generator", 1, 0b011);
	public static final AlchemyModuleTypes MINERAL_FILTER = new AlchemyModuleTypes("mineral_filter", 3, 0b010);
	public static final AlchemyModuleTypes SOUL_FILTER = new AlchemyModuleTypes("soul_filter", 3, 0b010);
	//public static final AlchemyModuleTypes TRIVALENT_FILTER = new AlchemyModuleTypes("trivalent_filter", 1, 0b010, FILTER);
	
	public final int maxTier;
	public final String name;
	/**
	 * Indicates which machine slots are used by this module. <br/>
	 * If another module has an incompatible flag, they will not be able to be installed together.
	 */
	private final int slotsTaken;
	private final List<AlchemyModuleTypes> aliases;

	public AlchemyModuleTypes(String name, int maxTier, int slotsTaken, AlchemyModuleTypes... aliases) {
		this(name, maxTier, slotsTaken, true, aliases);
	}

	public AlchemyModuleTypes(String name, int maxTier, int slotsTaken, boolean addToList, AlchemyModuleTypes... aliases) {
		this.name = name;
		this.maxTier = maxTier;
		this.slotsTaken = slotsTaken;
		this.aliases = Arrays.asList(aliases);
		if(addToList)
			automatedEntries.add(this);
	}
	
	public boolean isCompatible(AlchemyModuleTypes module) {
		return (this.slotsTaken & module.slotsTaken) == 0;
	}
	
	public String name() {
		return name;
	}
	
	public static List<AlchemyModuleTypes> automatedValues() {
		return automatedEntries;
	}
	
	public static AlchemyModuleTypes valueOf(String name) {
		return REGISTRY.getValues().stream().filter(mod -> mod.name.equals(name)).findAny().orElse(null);
	}
	
	public boolean isEquivalent(AlchemyModuleTypes module) {
		return module.equals(this) || aliases.contains(module);
	}

	@SubscribeEvent
	public static void onRegister(RegistryEvent.Register<AlchemyModuleTypes> event) {
		IForgeRegistry<AlchemyModuleTypes> reg = event.getRegistry();
		automatedEntries.forEach(reg::register);
		reg.registerAll(ALCHEMY_INTERFACE_BOTTOM, ALCHEMY_INTERFACE_TOP);
	}
	
}
