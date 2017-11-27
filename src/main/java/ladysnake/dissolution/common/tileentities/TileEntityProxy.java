package ladysnake.dissolution.common.tileentities;

import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityProxy extends TileEntity {

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        TileEntity te = world.getTileEntity(pos.down());
        return te instanceof TileEntityModularMachine ? ((TileEntityModularMachine) te).hasCapability(capability, facing, BlockCasing.EnumPartType.TOP) : super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        TileEntity te = world.getTileEntity(pos.down());
        return te instanceof TileEntityModularMachine ? ((TileEntityModularMachine) te).getCapability(capability, facing, BlockCasing.EnumPartType.TOP) : super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return compound;
    }

}
