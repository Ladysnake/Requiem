package ladysnake.dissolution.common.items;

import java.util.HashMap;
import java.util.Map;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.AlchemyModules;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ItemAlchemyModule extends Item {
	
	private static final Map<AlchemyModules, ItemAlchemyModule[]> allModules = new HashMap<>();
	private static final Map<ItemAlchemyModule, ResourceLocation> modulesModels = new HashMap<>();

	private AlchemyModules type;
	private int tier;

	public ItemAlchemyModule(AlchemyModules type, int tier) {
		super();
		this.type = type;
		String name = type.name() + "_tier_" + tier;
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		allModules.computeIfAbsent(type, t -> new ItemAlchemyModule[type.maxTier+1])[tier] = this;
		modulesModels.put(this, new ResourceLocation(Reference.MOD_ID, "block/machine_parts/" + name));
	}

	public AlchemyModules getType() {
		return type;
	}
	
	public int getTier() {
		return tier;
	}
	
	public static Map<ItemAlchemyModule, ResourceLocation> getModulesModels() {
		return modulesModels;
	}
	
	public static ItemAlchemyModule getFromType(AlchemyModules type, int tier) {
		return allModules.get(type)[tier];
	}
	
}
