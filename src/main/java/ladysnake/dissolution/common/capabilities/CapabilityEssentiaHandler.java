package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.EssentiaTypes;
import ladysnake.dissolution.api.EssentiaStack;
import ladysnake.dissolution.api.IEssentiaHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;

public class CapabilityEssentiaHandler {
	
	@SuppressWarnings("ConstantConditions")
	@CapabilityInject(IEssentiaHandler.class)
	@Nonnull
	public static final Capability<IEssentiaHandler> CAPABILITY_ESSENTIA = null;
	
	public static void register() {
        CapabilityManager.INSTANCE.register(IEssentiaHandler.class, new Storage(), () -> new DefaultEssentiaHandler(1));
    }
    
    public static IEssentiaHandler getHandler(TileEntity te) {

        if (te.hasCapability(CAPABILITY_ESSENTIA, EnumFacing.DOWN))
            return te.getCapability(CAPABILITY_ESSENTIA, EnumFacing.DOWN);
        
        return null;
    }
    
    public interface IEssentiaHandlerModifiable {
    	void setContent(EssentiaStack content);
    	void setMaxSize(int maxSize);
    }
    
    /**
     * A default implementation of IEssentiaHandler that has one slot with a defined size
     * @author Pyrofab
     *
     */
    public static class DefaultEssentiaHandler implements IEssentiaHandler, IEssentiaHandlerModifiable {
    	
    	private float suction = 0;
    	private EssentiaTypes suctionType = EssentiaTypes.UNTYPED;
    	private int maxSize;
    	private EssentiaStack content;
    	
    	public DefaultEssentiaHandler(int size) {
    		this.maxSize = size;
    		this.content = EssentiaStack.EMPTY;
    	}
    	
    	@Override
    	public void setSuction(float suction, EssentiaTypes type) {
    		this.suction = suction;
    		this.suctionType = type;
    	}

		@Override
		public float getSuction() {
			return this.suction;
		}
		
		@Override
		public EssentiaTypes getSuctionType() {
			return suctionType;
		}
		
		public EssentiaStack insert(EssentiaStack stack) {
			if(this.content.isEmpty() || stack.getType() == this.content.getType()) {
				int currentAmount = this.content.getCount();
				int amount = Math.min(stack.getCount() + currentAmount, maxSize);
				this.content = stack.withSize(amount);
				stack = stack.smaller(amount - currentAmount);
			}
			return stack;
		}
		
		public EssentiaStack extract(int amount) {
			if(this.content.isEmpty())
				return EssentiaStack.EMPTY;
			amount = Math.min(amount, this.content.getCount());
			EssentiaStack ret = this.content.withSize(amount);
			this.content.shrink(amount);
			return ret;
		}

		@Override
		public EssentiaStack readContent() {
			return new EssentiaStack(this.content);
		}

		@Override
		public int getMaxSize() {
			return this.maxSize;
		}
		
		@Override
		public boolean isFull() {
			return this.content.getCount() >= this.maxSize;
		}

		@Override
		public void setContent(EssentiaStack content) {
			this.content = content;
		}

		@Override
		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}
    	
    }
    
    @SuppressWarnings("ConstantConditions")
	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		IEssentiaHandler instance = CAPABILITY_ESSENTIA.getDefaultInstance();
		
		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
			return capability == CAPABILITY_ESSENTIA;
		}

		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
			return hasCapability(capability, facing) ? CAPABILITY_ESSENTIA.cast(instance) : null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return (NBTTagCompound) CAPABILITY_ESSENTIA.getStorage().writeNBT(CAPABILITY_ESSENTIA, instance, null);
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			CAPABILITY_ESSENTIA.getStorage().readNBT(CAPABILITY_ESSENTIA, instance, null, nbt);
		}
    	
    }
    
    public static class Storage implements Capability.IStorage<IEssentiaHandler> {

		@Override
		public NBTBase writeNBT(Capability<IEssentiaHandler> capability, IEssentiaHandler instance, EnumFacing side) {
			NBTTagCompound ret = new NBTTagCompound();
			ret.setFloat("suction", instance.getSuction());
			ret.setString("suctionType", instance.getSuctionType().name());
			ret.setInteger("maxSize", instance.getMaxSize());
			instance.readContent().writeToNBT(ret);
			return ret;
		}

		@Override
		public void readNBT(Capability<IEssentiaHandler> capability, IEssentiaHandler instance, EnumFacing side,
				NBTBase nbt) {
			NBTTagCompound compound = (NBTTagCompound) nbt;
			instance.setSuction(compound.getFloat("suction"), EssentiaTypes.valueOf(compound.getString("suctionType")));
			if(instance instanceof IEssentiaHandlerModifiable) {
				((IEssentiaHandlerModifiable)instance).setContent(new EssentiaStack(compound));
				((IEssentiaHandlerModifiable)instance).setMaxSize(compound.getInteger("maxSize"));
			}
		}
    	
    }

}
