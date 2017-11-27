package ladysnake.dissolution.common.items;

import ladysnake.dissolution.client.models.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class ItemFilterModule extends ItemAlchemyModule {

    ItemFilterModule(AlchemyModuleTypes type, int tier) {
        super(type, tier);
    }

    @Override
    public AlchemyModule toModule() {
        return new FilterModule(this.getType(), this.getTier(), 0);
    }

    @Override
    public AlchemyModule toModule(BlockCasing.EnumPartType part, TileEntityModularMachine te) {
        int slot = Stream.of(0, 1, 2).filter(i -> te.getInstalledModules().stream()
                .noneMatch(mod -> mod instanceof FilterModule && ((FilterModule) mod).slot == i)).findAny()
                .orElse(0);
        return new FilterModule(this.getType(), this.getTier(), slot);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerRender() {
        super.registerRender();
        for (int i = 1; i <= 2; i++) {
            DissolutionModelLoader.addModel(new ResourceLocation(Reference.MOD_ID, String.format("machine/%s_%s", this.getRegistryName().getResourcePath(), i)),
                    ModelRotation.X0_Y90, ModelRotation.X0_Y180, ModelRotation.X0_Y270);
        }
    }

    public static class FilterModule extends AlchemyModule {
        private int slot;

        FilterModule(@Nonnull AlchemyModuleTypes type, int tier, int slot) {
            super(type, tier);
            this.slot = slot % 3;
        }

        FilterModule(@Nonnull NBTTagCompound compound) {
            super(compound);
            this.slot = compound.getInteger("slot");
        }

        @Override
        public NBTTagCompound toNBT() {
            NBTTagCompound ret = super.toNBT();
            ret.setInteger("slot", slot);
            return ret;
        }

        @Override
        public ResourceLocation getModel() {
            ResourceLocation baseModel = getType().getRegistryName();
            String extension = slot == 0 ? "" : "_" + slot;
            return baseModel == null ? null : modulesModels.computeIfAbsent(this, key ->
                    new ResourceLocation(baseModel.getResourceDomain(), "machine/" +
                            baseModel.getResourcePath() + extension));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            FilterModule that = (FilterModule) o;

            return slot == that.slot;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + slot;
            return result;
        }

        @Override
        public String toString() {
            return "FilterModule [type=" + getType() + "slot=" + slot + "]";
        }

    }
}
