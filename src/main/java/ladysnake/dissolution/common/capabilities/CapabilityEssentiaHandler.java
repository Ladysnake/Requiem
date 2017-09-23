package ladysnake.dissolution.common.capabilities;

import java.util.Iterator;

import javax.annotation.Nonnull;

import ladysnake.dissolution.api.EssentiaStack;
import ladysnake.dissolution.api.EssentiaTypes;
import ladysnake.dissolution.api.IEssentiaHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

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

	/**
	 * Don't use this except for loading content from disk
	 */
	public interface IEssentiaHandlerModifiable {
		void setContent(int channel, EssentiaStack content);

		void setMaxSize(int maxSize);
	}

	/**
	 * A default implementation of IEssentiaHandler that has one slot with a defined
	 * size
	 * 
	 * @author Pyrofab
	 *
	 */
	public static class DefaultEssentiaHandler implements IEssentiaHandler, IEssentiaHandlerModifiable {

		private float suction = 0;
		private EssentiaTypes suctionType = EssentiaTypes.UNTYPED;
		private int maxSize;
		private EssentiaList content;

		public DefaultEssentiaHandler(int size) {
			this(size, 1);
		}

		public DefaultEssentiaHandler(int size, int channels) {
			this.maxSize = size;
			this.content = EssentiaList.withSize(channels);
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

		@Override
		public EssentiaStack insert(EssentiaStack stack) {
			int i = this.content.indexOf(stack.getType());
			if(i == -1)
				i = this.content.indexOf(EssentiaTypes.UNTYPED);
			if(i > -1) {
				EssentiaStack stack2 = this.content.get(i);
				int currentAmount = stack2.getCount();
				int amount = Math.min(stack.getCount() + currentAmount, maxSize);
				this.content.set(i, stack.withSize(amount));
				stack = stack.smaller(amount - currentAmount);
			}
			return stack;
		}

		@Override
		public EssentiaStack extract(int amount, EssentiaTypes type) {
			EssentiaStack contentStack = this.content.get(type);
			if(contentStack.isEmpty())
				return EssentiaStack.EMPTY;
			amount = Math.min(amount, contentStack.getCount());
			EssentiaStack ret = contentStack.withSize(amount);
			contentStack.shrink(amount);
			return ret;
		}

		@Override
		public EssentiaStack readContent(EssentiaTypes type) {
			return new EssentiaStack(this.content.get(type));
		}

		@Override
		public int getMaxSize() {
			return this.maxSize;
		}
		
		@Override
		public void setContent(int channel, EssentiaStack content) {
			this.content.set(channel, content);
		}

		@Override
		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}

		@Override
		public Iterator<EssentiaStack> iterator() {
			return this.content.iterator();
		}

		@Override
		public int getChannels() {
			return (int) this.content.stream().filter(e -> !e.isEmpty()).count();
		}

		@Override
		public int getMaxChannels() {
			return this.content.size();
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
			NBTTagList essentiaList = new NBTTagList();
			for(EssentiaStack stack : instance)
				essentiaList.appendTag(stack.writeToNBT(new NBTTagCompound()));
			ret.setTag("essentiaList", essentiaList);
			return ret;
		}

		@Override
		public void readNBT(Capability<IEssentiaHandler> capability, IEssentiaHandler instance, EnumFacing side,
				NBTBase nbt) {
			NBTTagCompound compound = (NBTTagCompound) nbt;
			instance.setSuction(compound.getFloat("suction"), EssentiaTypes.valueOf(compound.getString("suctionType")));
			if (instance instanceof IEssentiaHandlerModifiable) {
				NBTTagList essentiaList = compound.getTagList("essentiaList", 10);
				for(int i = 0; i < essentiaList.tagCount(); i++)
					((IEssentiaHandlerModifiable) instance).setContent(i, new EssentiaStack((NBTTagCompound)essentiaList.get(i)));
				((IEssentiaHandlerModifiable) instance).setMaxSize(compound.getInteger("maxSize"));
			}
		}

	}

}
