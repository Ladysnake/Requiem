package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.api.DistillateTypes;
import ladysnake.dissolution.api.IDistillateHandler;
import ladysnake.dissolution.common.capabilities.CapabilityDistillateHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class TileEntityDistillatePipe extends TileEntity implements ITickable {

    private final IDistillateHandler essentiaHandler;
    private int transferCooldown;

    public TileEntityDistillatePipe() {
        this.essentiaHandler = new CapabilityDistillateHandler.DefaultDistillateHandler(1, 4);
    }

    @Override
    public void update() {
        if (!world.isRemote && transferCooldown++ % 20 == 0) {
            for (DistillateTypes distillateType : DistillateTypes.values()) {
                IDistillateHandler strongestSuction = this.findStrongestSuction(distillateType);
                if (strongestSuction != null) {
                    this.essentiaHandler.setSuction(distillateType, strongestSuction.getSuction(distillateType) - 1);
                    this.essentiaHandler.flow(strongestSuction, distillateType);
                    this.markDirty();
                } else        // nothing sucks this type of essentia
                    this.essentiaHandler.setSuction(distillateType, 0);
            }
        }
    }

    private IDistillateHandler findStrongestSuction(DistillateTypes type) {
        IDistillateHandler strongestSuction = null;
        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te != null && te.hasCapability(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, facing.getOpposite())) {
                IDistillateHandler handler = te.getCapability(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, facing.getOpposite());
                if (handler != null && (strongestSuction == null || strongestSuction.getSuction(type) < handler.getSuction(type))) {
                    strongestSuction = handler;
                }
            }
        }

        return strongestSuction;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        //noinspection ConstantConditions
        return (capability == CapabilityDistillateHandler.CAPABILITY_DISTILLATE) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        //noinspection ConstantConditions
        if (capability == CapabilityDistillateHandler.CAPABILITY_DISTILLATE)
            return CapabilityDistillateHandler.CAPABILITY_DISTILLATE.cast(essentiaHandler);
        return super.getCapability(capability, facing);
    }

    @Override
    public @Nonnull
    NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        NBTBase essentiaCompound = CapabilityDistillateHandler.CAPABILITY_DISTILLATE.getStorage().writeNBT(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, this.essentiaHandler, null);
        if (essentiaCompound != null)
            compound.setTag("essentiaHandler", essentiaCompound);
        compound.setInteger("cooldown", transferCooldown);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        try {
            CapabilityDistillateHandler.CAPABILITY_DISTILLATE.getStorage().readNBT(CapabilityDistillateHandler.CAPABILITY_DISTILLATE, essentiaHandler, null, compound.getCompoundTag("essentiaHandler"));
            this.transferCooldown = compound.getInteger("cooldown");
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }
    }

}
