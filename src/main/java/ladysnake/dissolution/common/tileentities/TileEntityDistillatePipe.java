package ladysnake.dissolution.common.tileentities;

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
	
	private IDistillateHandler essentiaHandler;
	private int transferCooldown;
	
	public TileEntityDistillatePipe() {
		this.essentiaHandler = new CapabilityDistillateHandler.DefaultDistillateHandler(1, 4);
	}

	@Override
	public void update() {
		if(!world.isRemote && transferCooldown++ % 20 == 0) {
			IDistillateHandler strongestSuction = this.findStrongestSuction();
			if(strongestSuction != null) {
				if(strongestSuction.getSuction() > this.essentiaHandler.getSuction())
					this.essentiaHandler.setSuction(strongestSuction.getSuction() - 1, this.essentiaHandler.getSuctionType());
				this.essentiaHandler.flow(strongestSuction);
				this.markDirty();
			}
		}
	}
	
	private IDistillateHandler findStrongestSuction() {
		IDistillateHandler strongestSuction = null;
		for (EnumFacing facing : EnumFacing.values()) {
			TileEntity te = world.getTileEntity(pos.offset(facing));
			if(te != null && te.hasCapability(CapabilityDistillateHandler.CAPABILITY_ESSENTIA, facing.getOpposite())) {
				IDistillateHandler handler = te.getCapability(CapabilityDistillateHandler.CAPABILITY_ESSENTIA, facing.getOpposite());
				if(handler != null && (strongestSuction == null || strongestSuction.getSuction() < handler.getSuction())) {
					strongestSuction = handler;
				}
			}
		}
		
		return strongestSuction;
	}
	
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		//noinspection ConstantConditions
		return (capability == CapabilityDistillateHandler.CAPABILITY_ESSENTIA) || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		//noinspection ConstantConditions
		if(capability == CapabilityDistillateHandler.CAPABILITY_ESSENTIA)
			return CapabilityDistillateHandler.CAPABILITY_ESSENTIA.cast(essentiaHandler);
		return super.getCapability(capability, facing);
	}
	
	@Override
	public @Nonnull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		NBTBase essentiaCompound = CapabilityDistillateHandler.CAPABILITY_ESSENTIA.getStorage().writeNBT(CapabilityDistillateHandler.CAPABILITY_ESSENTIA, this.essentiaHandler, null);
		if(essentiaCompound != null)
			compound.setTag("essentiaHandler", essentiaCompound);
		compound.setInteger("cooldown", transferCooldown);
		return compound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		try {
			CapabilityDistillateHandler.CAPABILITY_ESSENTIA.getStorage().readNBT(CapabilityDistillateHandler.CAPABILITY_ESSENTIA, essentiaHandler, null, compound.getCompoundTag("essentiaHandler"));
			this.transferCooldown = compound.getInteger("cooldown");
		} catch (NullPointerException | IllegalArgumentException ignored) {}
	}

}
