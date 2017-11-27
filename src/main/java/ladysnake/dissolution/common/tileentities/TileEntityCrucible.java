package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.GenericStack;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModFluids;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class TileEntityCrucible extends PowderContainer implements ITickable {

    public static final int MAX_VOLUME = 8;

    private FluidTank fluidInventory = new CrucibleFluidTank(Fluid.BUCKET_VOLUME);
    private MagnetPowerMode magnetPowerMode = MagnetPowerMode.NO_MAGNET;
    private int meltingTimer, separatingTimer, evaporationTimer;

    public TileEntityCrucible() {
        super();
        this.itemInventory = new PowderContainer.ItemHandler(Items.AIR);
        this.evaporationTimer = (int) (Math.random() * 100) + 100;
    }

    @Override
    public void update() {
        IBlockState state = world.getBlockState(pos.down());
        if (state.getBlock().equals(ModBlocks.MAGNET)) {
            MagnetPowerMode newMagnetPowerMode = world.isBlockPowered(pos.down()) ? MagnetPowerMode.MAGNET_ON : MagnetPowerMode.MAGNET_OFF;
            if (magnetPowerMode.isOpposite(newMagnetPowerMode) && !this.powderInventory.isEmpty() && ++separatingTimer % 10 == 0) {
                GenericStack<EnumPowderOres> powderStack = this.powderInventory.extract(1, null);
                GenericStack<EnumPowderOres> result = new GenericStack<>(powderStack.getType().getRefinedPowder(), powderStack.getCount());
                this.powderInventory.insert(result.isEmpty() ? powderStack : result);
            }
            magnetPowerMode = newMagnetPowerMode;
        } else {
            magnetPowerMode = MagnetPowerMode.NO_MAGNET;
            boolean evaporation = false;
            if (this.getFluidInventory().getFluid() != null &&
                    Objects.equals(this.getFluidInventory().getFluid().getFluid(), FluidRegistry.WATER)) {
                if (--evaporationTimer <= 0) {
                    this.getFluidInventory().drain(new FluidStack(FluidRegistry.WATER, 1), true);
                    if (this.getFluidInventory().getFluidAmount() % (Fluid.BUCKET_VOLUME / getMaxVolume()) == 0) {
                        GenericStack<EnumPowderOres> powder = powderInventory.extract(1, null);
                        if (!powder.isEmpty()) {
                            ItemStack crystal = new ItemStack(powder.getType().getComponent());
                            if (!world.isRemote)
                                world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, crystal));
//                            itemInventory.insertItem(0, crystal, false);
                        }
                    }
                    evaporationTimer = world.rand.nextInt(100) + 100;
                }
                evaporation = true;
            }
            if ((state.getMaterial().equals(Material.FIRE)
                    || state.getMaterial().equals(Material.LAVA)
                    || state.getBlock() instanceof BlockFluidBase && ((BlockFluidBase) state.getBlock()).getFluid().getTemperature(world, pos.down()) > 400)) {
                GenericStack<EnumPowderOres> cinnabarPowder = powderInventory.readContent(EnumPowderOres.CINNABAR);
                if (evaporation) {
                    if (Math.random() < 0.075f)
                        world.spawnParticle(EnumParticleTypes.CLOUD, (double) pos.getX() + 0.5, (double) pos.getY() + 1.1,
                                (double) pos.getZ() + 0.5, 0.0D, 0.3D, 0.0D);
                    evaporationTimer -= 100;
                }
                if (!evaporation && !cinnabarPowder.isEmpty()) {
                    if (++meltingTimer % 200 == 0) {
                        powderInventory.extract(1, EnumPowderOres.CINNABAR);
                        FluidStack mercury = new FluidStack(ModFluids.MERCURY.fluid(), Fluid.BUCKET_VOLUME / MAX_VOLUME);
                        fluidInventory.fill(mercury, true);
                        this.markDirty();
                    }
                }
            }
        }
    }

    @Override
    protected int getMaxVolume() {
        return MAX_VOLUME;
    }

    public FluidTank getFluidInventory() {
        return fluidInventory;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidInventory);
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("fluidInventory"))
            this.fluidInventory.readFromNBT(compound.getCompoundTag("fluidInventory"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("fluidInventory", fluidInventory.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    protected boolean isFull() {
        return this.powderInventory.getTotalAmount() + this.itemInventory.getStackInSlot(0).getCount() +
                ((this.fluidInventory.getFluidAmount() * MAX_VOLUME) / Fluid.BUCKET_VOLUME) >= MAX_VOLUME;
    }

    class CrucibleFluidTank extends FluidTank {
        private CrucibleFluidTank(int capacity) {
            super(capacity);
        }

        @Override
        public int getCapacity() {
            return super.getCapacity();// - TileEntityCrucible.this.powderInventory.getTotalAmount();
        }

        @Override
        public boolean canFill() {
            return !TileEntityCrucible.this.isFull() && super.canFill();
        }

        @Override
        public int fillInternal(FluidStack resource, boolean doFill) {
            int ret = super.fillInternal(resource, doFill);
            if (doFill)
                markDirty();
            return ret;
        }

        @Nullable
        @Override
        public FluidStack drainInternal(int maxDrain, boolean doDrain) {
            FluidStack ret = super.drainInternal(maxDrain, doDrain);
            markDirty();
            return ret;
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
