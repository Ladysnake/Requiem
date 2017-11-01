package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.GenericStack;
import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.api.IGenericInventoryProvider;
import ladysnake.dissolution.api.INBTSerializableType;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModFluids;
import ladysnake.dissolution.common.inventory.InputItemHandler;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TileEntityCrucible extends TileEntity implements ITickable, IPowderContainer {

    private static Map<Item, EnumPowderOres> conversions = new HashMap<>();

    static {
        conversions.put(Item.getItemFromBlock(Blocks.CLAY), EnumPowderOres.HALITE);
        conversions.put(Item.getItemFromBlock(Blocks.COAL_BLOCK), EnumPowderOres.SULFUR);
        conversions.put(Item.getItemFromBlock(Blocks.MAGMA), EnumPowderOres.CINNABAR);
    }

    private IGenericInventoryProvider inventoryProvider = new CapabilityGenericInventoryProvider.DefaultGenericInventoryProvider();
    private IItemHandler itemInventory = new InputItemHandler(Blocks.CLAY, Blocks.MAGMA, Blocks.COAL_BLOCK);
    private GenericStackInventory<EnumPowderOres> powderInventory = new CruciblePowderInventory(EnumPowderOres.class, EnumPowderOres.SERIALIZER);
    private FluidTank fluidInventory = new CrucibleFluidTank(8);
    private MagnetPowerMode magnetPowerMode = MagnetPowerMode.NO_MAGNET;
    private int meltingTimer, separatingTimer;

    public TileEntityCrucible() {
        inventoryProvider.setInventory(EnumPowderOres.class, powderInventory);
        fluidInventory.fillInternal(new FluidStack(FluidRegistry.WATER, 1), true);
    }

    @Override
    public void update() {
        IBlockState state = world.getBlockState(pos.down());
        GenericStack<EnumPowderOres> cinnabarPowder = powderInventory.readContent(EnumPowderOres.CINNABAR);
        if(state.getBlock().equals(ModBlocks.MAGNET)) {
           MagnetPowerMode newMagnetPowerMode = world.isBlockPowered(pos.down()) ? MagnetPowerMode.MAGNET_ON : MagnetPowerMode.MAGNET_OFF;
           if(magnetPowerMode.isOpposite(newMagnetPowerMode) && powderInventory.canInsert() && ++separatingTimer % 10 == 0) {
               ItemStack itemStack = itemInventory.extractItem(0, 1, false);
                this.powderInventory.insert(new GenericStack<>(conversions.get(itemStack.getItem())));
           }
           magnetPowerMode = newMagnetPowerMode;
        } else {
            magnetPowerMode = MagnetPowerMode.NO_MAGNET;
            if(!cinnabarPowder.isEmpty() &&
                    (state.getMaterial().equals(Material.FIRE)
                            || state.getMaterial().equals(Material.LAVA)
                            || state.getBlock() instanceof BlockFluidBase && ((BlockFluidBase) state.getBlock()).getFluid().getTemperature(world, pos.down()) > 400)) {
                if(meltingTimer++ % 200 == 0) {
                    powderInventory.extract(1, EnumPowderOres.CINNABAR);
                    FluidStack mercury = new FluidStack(ModFluids.MERCURY.fluid(), 1);
                    fluidInventory.fillInternal(mercury, true);
                }
            }
        }
    }

    public ItemStack insertItem(ItemStack itemStack) {
        return ItemHandlerHelper.insertItem(itemInventory, itemStack, false);
    }

    @Override
    public GenericStackInventory<EnumPowderOres> getPowderInventory() {
        return powderInventory;
    }

    public FluidTank getFluidInventory() {
        return fluidInventory;
    }

    @Override
    public NBTTagCompound saveToNbt(NBTTagCompound nbtTagCompound) {
        return nbtTagCompound;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityGenericInventoryProvider.CAPABILITY_GENERIC
                || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityGenericInventoryProvider.CAPABILITY_GENERIC)
            return CapabilityGenericInventoryProvider.CAPABILITY_GENERIC.cast(inventoryProvider);
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidInventory);
        return super.getCapability(capability, facing);
    }

    class CrucibleFluidTank extends FluidTank {
        public CrucibleFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public int getCapacity() {
            return super.getCapacity() - TileEntityCrucible.this.powderInventory.getTotalAmount();
        }

        @Override
        public boolean canFill() {
            return TileEntityCrucible.this.powderInventory.getTotalAmount() < 8 && super.canFill(); //FIXME should return true when the powder inventory is not full
        }
    }

    class CruciblePowderInventory extends GenericStackInventory<EnumPowderOres> {

        public CruciblePowderInventory(Class<EnumPowderOres> typeClass, INBTSerializableType.INBTTypeSerializer<EnumPowderOres> serializer) {
            super(8, 1, typeClass, serializer);
        }

        @Override
        public int getSlotLimit(int slot) {
            return super.getSlotLimit(slot) - TileEntityCrucible.this.fluidInventory.getFluidAmount();
        }

        @Override
        public boolean canInsert() {
            return TileEntityCrucible.this.fluidInventory.getFluidAmount() < 8 && super.canInsert();
        }
    }

    enum MagnetPowerMode {
        NO_MAGNET,
        MAGNET_ON,
        MAGNET_OFF;

        public boolean isOpposite(MagnetPowerMode other) {
            return this == MAGNET_OFF && other == MAGNET_ON || this == MAGNET_ON && other == MAGNET_OFF;
        }
    }
}
