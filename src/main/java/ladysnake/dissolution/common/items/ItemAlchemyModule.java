package ladysnake.dissolution.common.items;

import ladysnake.dissolution.client.models.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ItemAlchemyModule extends Item implements ICustomLocation {

    /**
     * a map containing every item mapped to a module type. For practical reasons, arrays in this map start at one
     */
    @SuppressWarnings("WeakerAccess")
    protected static final Map<AlchemyModuleTypes, ItemAlchemyModule[]> allModules = new HashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected static final Map<AlchemyModule, ResourceLocation> modulesModels = new HashMap<>();

    private AlchemyModuleTypes type;
    private int tier;

    public ItemAlchemyModule(AlchemyModuleTypes type, int tier) {
        super();
        this.type = type;
        this.tier = tier;
        //noinspection ConstantConditions
        String name = type.maxTier == 1
                ? type.getRegistryName().getResourcePath()
                : String.format("%s_tier_%s", type.getRegistryName().getResourcePath(), +tier);
        this.setUnlocalizedName(name);
        this.setRegistryName(type.getRegistryName().getResourceDomain(), name);
        allModules.computeIfAbsent(type, t -> new ItemAlchemyModule[type.maxTier + 1])[tier] = this;
    }

    public AlchemyModuleTypes getType() {
        return this.type;
    }

    public int getTier() {
        return tier;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerRender() {
        ICustomLocation.super.registerRender();
        ResourceLocation model = new ResourceLocation(Reference.MOD_ID, "machine/" + this.getRegistryName().getResourcePath());
//		ResourceLocation model = new ResourceLocation(this.getModelLocation().toString());
        modulesModels.put(this.toModule(), model);
        DissolutionModelLoader.addModel(model, ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
    }

    public AlchemyModule toModule() {
        return new AlchemyModule(this.getType(), this.getTier());
    }

    public AlchemyModule toModule(BlockCasing.EnumPartType part, TileEntityModularMachine te) {
        return new AlchemyModule(this.getType(), this.getTier());
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

        public AlchemyModule(@Nonnull NBTTagCompound compound) {
            type = AlchemyModuleTypes.valueOf(compound.getString("type"));
            tier = compound.getInteger("tier");
        }

        @Nonnull
        public AlchemyModuleTypes getType() {
            return type;
        }

        public int getTier() {
            return tier;
        }

        public ResourceLocation getModel() {
            return modulesModels.get(this);
        }

        public ItemAlchemyModule toItem() {
            return allModules.get(this.getType())[this.getTier()];
        }

        public NBTTagCompound toNBT() {
            NBTTagCompound ret = new NBTTagCompound();
            ret.setInteger("tier", getTier());
            //noinspection ConstantConditions
            ret.setString("type", getType().getRegistryName().toString());
            return ret;
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
                    "type=" + type.getRegistryName() +
                    ", tier=" + tier +
                    '}';
        }
    }

}
