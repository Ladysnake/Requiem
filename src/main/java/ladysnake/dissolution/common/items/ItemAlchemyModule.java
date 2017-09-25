package ladysnake.dissolution.common.items;

import ladysnake.dissolution.client.models.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ItemAlchemyModule extends Item implements ICustomLocation {

	/**a map containing every item mapped to a module type. For practical reasons, arrays start at one*/
	@SuppressWarnings("WeakerAccess")
	protected static final Map<AlchemyModuleTypes, ItemAlchemyModule[]> allModules = new HashMap<>();
	@SuppressWarnings("WeakerAccess")
	protected static final Map<AlchemyModule, ResourceLocation> modulesModels = new HashMap<>();
	private static final Map<AlchemyModule, ResourceLocation> activeModulesModels = new HashMap<>();

	private AlchemyModuleTypes type;
	private int tier;

	public ItemAlchemyModule(AlchemyModuleTypes type, int tier) {
		super();
		this.type = type;
		this.tier = tier;
		String name = type.maxTier == 1 ? type.name : type.name() + "_tier_" + tier;
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		allModules.computeIfAbsent(type, t -> new ItemAlchemyModule[type.maxTier+1])[tier] = this;
		modulesModels.put(this.toModule(BlockCasing.EnumPartType.BOTTOM), new ResourceLocation(Reference.MOD_ID, "machine/" + name));
	}
	
	public ItemAlchemyModule(AlchemyModuleTypes type, int tier, ResourceLocation activeState) {
		this(type, tier);
		activeModulesModels.put(this.toModule(BlockCasing.EnumPartType.BOTTOM), activeState);
	}

	public AlchemyModuleTypes getType() {
		return this.type;
	}

	public int getTier() {
		return tier;
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerModels() {
		for(Map.Entry<AlchemyModule, ResourceLocation> entry : modulesModels.entrySet()) {
			DissolutionModelLoader.addModel(entry.getValue(), ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
			DissolutionModelLoader.addModel(activeModulesModels.getOrDefault(entry.getKey(), entry.getValue()), ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
		}
	}

	public AlchemyModule toModule(BlockCasing.EnumPartType part) {
		return new AlchemyModule(this.getType(), this.getTier());
	}
	
	public static ItemAlchemyModule getFromType(AlchemyModuleTypes type, int tier) {
		return allModules.get(type)[tier];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelLocation() {
		assert this.getRegistryName() != null;
		return new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":machines/" + this.getRegistryName().getResourcePath());
	}

	public static class AlchemyModule {
		@Nonnull
		private final AlchemyModuleTypes type;
		private final int tier;

		public AlchemyModule(@Nonnull AlchemyModuleTypes type, int tier) {
			this.type = type;
			this.tier = tier;
		}

		@Nonnull
		public AlchemyModuleTypes getType() {
			return type;
		}

		public int getTier() {
			return tier;
		}

		public ResourceLocation getModel(boolean running) {
			return running ? activeModulesModels.getOrDefault(this, modulesModels.get(this)) : modulesModels.get(this);
		}

		public ItemAlchemyModule toItem() {
			return allModules.get(this.getType())[this.getTier()];
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			AlchemyModule that = (AlchemyModule) o;

			return tier == that.tier && type.equals(that.type);
		}

		@Override
		public int hashCode() {
			int result = type.hashCode();
			result = 31 * result + tier;
			return result;
		}

		@Override
		public String toString() {
			return "AlchemyModule{" +
					"type=" + type.name() +
					", tier=" + tier +
					'}';
		}
	}
	
}
