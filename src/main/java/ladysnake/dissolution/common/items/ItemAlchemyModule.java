package ladysnake.dissolution.common.items;

import java.util.HashMap;
import java.util.Map;

import ladysnake.dissolution.client.renders.blocks.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.AlchemyModule;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemAlchemyModule extends Item {
	
	private static final Map<AlchemyModule, ItemAlchemyModule[]> allModules = new HashMap<>();
	private static final Map<ItemAlchemyModule, ResourceLocation> modulesModels = new HashMap<>();
	private static final Map<ItemAlchemyModule, ResourceLocation> activeModulesModels = new HashMap<>();

	private AlchemyModule type;
	private int tier;

	public ItemAlchemyModule(AlchemyModule type, int tier) {
		super();
		this.type = type;
		this.tier = tier;
		String name = type.name() + "_tier_" + tier;
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		allModules.computeIfAbsent(type, t -> new ItemAlchemyModule[type.maxTier+1])[tier] = this;
		modulesModels.put(this, new ResourceLocation(Reference.MOD_ID, "machine/" + name));
	}
	
	public ItemAlchemyModule(AlchemyModule type, int tier, ResourceLocation activeState) {
		this(type, tier);
		activeModulesModels.put(this, activeState);
	}

	public AlchemyModule getType() {
		return type;
	}
	
	public int getTier() {
		return tier;
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerModels() {
		for(Map.Entry<ItemAlchemyModule, ResourceLocation> entry : modulesModels.entrySet()) {
			DissolutionModelLoader.addModel(entry.getValue());
			DissolutionModelLoader.addModel(activeModulesModels.getOrDefault(entry.getKey(), entry.getValue()));
		}
	}
	
	public ResourceLocation getModel() {
		return modulesModels.get(this);
	}
	
	public static ItemAlchemyModule getFromType(AlchemyModule type, int tier) {
		return allModules.get(type)[tier];
	}
	
}
