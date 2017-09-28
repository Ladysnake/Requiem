package ladysnake.dissolution.common.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ladysnake.dissolution.common.Reference;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * This is basically an enum that allows my fellow modders to add nice stuff
 * @author Pyrofab
 *
 */
@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public class AlchemyModuleTypes extends IForgeRegistryEntry.Impl<AlchemyModuleTypes> {

	private static IForgeRegistry<AlchemyModuleTypes> REGISTRY;
	private static final List<AlchemyModuleTypes> toRegister = new ArrayList<>();

	public static final AlchemyModuleTypes ALCHEMICAL_INTERFACE_TOP = new AlchemyModuleTypes(
			1, 0b100, false).setRegistryName(Reference.MOD_ID, "alchemical_interface_top");
	public static final AlchemyModuleTypes ALCHEMICAL_INTERFACE_BOTTOM = new AlchemyModuleTypes(
			1, 0b001, false).setRegistryName(Reference.MOD_ID, "alchemical_interface_bottom");
	public static final AlchemyModuleTypes CLOCHE = new AlchemyModuleTypes(1, 0b011).setRegistryName(Reference.MOD_ID, "cloche");
	//public static final AlchemyModuleTypes CONTAINER = new AlchemyModuleTypes(1, 0b001).setRegistryName("container");
	public static final AlchemyModuleTypes CRYSTALLIZER = new AlchemyModuleTypes(1, 0b010).setRegistryName(Reference.MOD_ID, "crystallizer");
	public static final AlchemyModuleTypes RESONANT_GENERATOR = new AlchemyModuleTypes(1, 0b011).setRegistryName(Reference.MOD_ID, "resonant_generator");
	public static AlchemyModuleTypes MINERAL_FILTER;
	public static AlchemyModuleTypes SOUL_FILTER;
	//public static final AlchemyModuleTypes TRIVALENT_FILTER = new AlchemyModuleTypes("trivalent_filter", 1, 0b010, FILTER);
	
	public final int maxTier;
	/**
	 * Indicates which machine slots are used by this module. <br/>
	 * If another module has an incompatible flag, they will not be able to be installed together.
	 */
	private final int slotsTaken;
	private final List<AlchemyModuleTypes> aliases;

	private AlchemyModuleTypes(int maxTier, int slotsTaken, AlchemyModuleTypes... aliases) {
		this(maxTier, slotsTaken, true, aliases);
	}

	/**
	 * Creates an alchemy module type to be used by modular machines
	 * @param maxTier the number of tiers this module type possesses
	 * @param slotsTaken a binary flag used to determine which modules are compatible
	 * @param addToList if true, this module and its associated items will be registered automatically
	 * @param aliases a list of other alchemy modules you can use in this module's place
	 */
	public AlchemyModuleTypes(int maxTier, int slotsTaken, boolean addToList, AlchemyModuleTypes... aliases) {
		this.maxTier = maxTier;
		this.slotsTaken = slotsTaken;
		this.aliases = Arrays.asList(aliases);
		if(addToList)
			toRegister.add(this);
	}
	
	public boolean isCompatible(AlchemyModuleTypes module) {
		return (this.slotsTaken & module.slotsTaken) == 0;
	}

	public boolean isEquivalent(AlchemyModuleTypes module) {
		return module.equals(this) || aliases.contains(module);
	}

	public ItemAlchemyModule.AlchemyModule readNBT(NBTTagCompound compound) {
		return new ItemAlchemyModule.AlchemyModule(compound);
	}
	
	@Override
	public String toString() {
		return this.getRegistryName() != null ? this.getRegistryName().toString() : super.toString();
	}

	public static AlchemyModuleTypes valueOf(String name) {
		return REGISTRY.getValue(new ResourceLocation(name));
	}

	public static void registerItems(Set<Item> allItems) {
		for (AlchemyModuleTypes module : AlchemyModuleTypes.toRegister) {
			for (int tier = 1; tier <= module.maxTier; tier++) {
				allItems.add(new ItemAlchemyModule(module, tier));
			}
		}
		allItems.add(new ItemFilterModule(MINERAL_FILTER =
				new AlchemyModuleFilter(1, true).setRegistryName(Reference.MOD_ID, "mineral_filter"), 1));
		allItems.add(new ItemFilterModule(SOUL_FILTER =
				new AlchemyModuleFilter(1, true).setRegistryName(Reference.MOD_ID, "soul_filter"), 1));
		allItems.add(new ItemInterfaceModule());
	}
	
	@SubscribeEvent
	public static void onRegistryRegister(RegistryEvent.NewRegistry event) {
		REGISTRY = new RegistryBuilder<AlchemyModuleTypes>()
				.setName(new ResourceLocation(Reference.MOD_ID, "alchemy_modules"))
				.setType(AlchemyModuleTypes.class).create();
	}

	@SubscribeEvent
	public static void onRegister(RegistryEvent.Register<AlchemyModuleTypes> event) {
		toRegister.forEach(REGISTRY::register);
		REGISTRY.registerAll(ALCHEMICAL_INTERFACE_BOTTOM, ALCHEMICAL_INTERFACE_TOP);
	}
	
}
