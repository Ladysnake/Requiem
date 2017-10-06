package ladysnake.dissolution.common.capabilities;

import java.util.Iterator;

import javax.annotation.Nonnull;

import ladysnake.dissolution.api.DistillateStack;
import ladysnake.dissolution.api.DistillateTypes;
import ladysnake.dissolution.api.IDistillateHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityDistillateHandler {

	@CapabilityInject(IDistillateHandler.class)
	@Nonnull
	public static Capability<IDistillateHandler> CAPABILITY_ESSENTIA;

	public static void register() {
		CapabilityManager.INSTANCE.register(IDistillateHandler.class, new Storage(), () -> new DefaultDistillateHandler(1));
	}

	public static IDistillateHandler getHandler(TileEntity te) {

		if (te.hasCapability(CAPABILITY_ESSENTIA, EnumFacing.DOWN))
			return te.getCapability(CAPABILITY_ESSENTIA, EnumFacing.DOWN);

		return null;
	}

	/**
	 * Don't use this except for loading content from disk
	 */
	public interface IDistillateHandlerModifiable {
		void setContent(int channel, DistillateStack content);

		void setMaxSize(int maxSize);
	}

	/**
	 * A default implementation of IDistillateHandler that has one slot with a defined
	 * size
	 * 
	 * @author Pyrofab
	 *
	 */
	public static class DefaultDistillateHandler implements IDistillateHandler, IDistillateHandlerModifiable {

		private float suction = 0;
		private DistillateTypes suctionType = DistillateTypes.UNTYPED;
		private int maxSize;
		private DistillateList content;

		public DefaultDistillateHandler(int size) {
			this(size, 1);
		}

		public DefaultDistillateHandler(int size, int channels) {
			this.maxSize = size;
			this.content = DistillateList.withSize(channels);
		}

		@Override
		public void setSuction(float suction, DistillateTypes type) {
			this.suction = suction;
			this.suctionType = type;
		}

		@Override
		public float getSuction() {
			return this.suction;
		}

		@Override
		public DistillateTypes getSuctionType() {
			return suctionType;
		}

		@Override
		public DistillateStack insert(DistillateStack stack) {
			int i = this.content.indexOf(stack.getType());
			if(i == -1)
				i = this.content.indexOf(DistillateTypes.UNTYPED);
			if(i > -1) {
				DistillateStack stack2 = this.content.get(i);
				int currentAmount = stack2.getCount();
				int amount = Math.min(stack.getCount() + currentAmount, maxSize);
				this.content.set(i, stack.withSize(amount));
				stack = stack.smaller(amount - currentAmount);
			}
			return stack;
		}

		@Override
		public DistillateStack extract(int amount, DistillateTypes type) {
			DistillateStack contentStack = this.content.get(type);
			if(contentStack.isEmpty())
				return DistillateStack.EMPTY;
			amount = Math.min(amount, contentStack.getCount());
			DistillateStack ret = contentStack.withSize(amount);
			contentStack.shrink(amount);
			return ret;
		}

		@Override
		public DistillateStack readContent(DistillateTypes type) {
			return new DistillateStack(this.content.get(type));
		}

		@Override
		public int getMaxSize() {
			return this.maxSize;
		}
		
		@Override
		public void setContent(int channel, DistillateStack content) {
			this.content.set(channel, content);
		}

		@Override
		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}

		@Override
		public Iterator<DistillateStack> iterator() {
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

	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		IDistillateHandler instance = CAPABILITY_ESSENTIA.getDefaultInstance();

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

	public static class Storage implements Capability.IStorage<IDistillateHandler> {

		@Override
		public NBTBase writeNBT(Capability<IDistillateHandler> capability, IDistillateHandler instance, EnumFacing side) {
			NBTTagCompound ret = new NBTTagCompound();
			ret.setFloat("suction", instance.getSuction());
			ret.setString("suctionType", instance.getSuctionType().name());
			ret.setInteger("maxSize", instance.getMaxSize());
			NBTTagList essentiaList = new NBTTagList();
			for(DistillateStack stack : instance)
				essentiaList.appendTag(stack.writeToNBT(new NBTTagCompound()));
			ret.setTag("essentiaList", essentiaList);
			return ret;
		}

		@Override
		public void readNBT(Capability<IDistillateHandler> capability, IDistillateHandler instance, EnumFacing side,
							NBTBase nbt) {
			NBTTagCompound compound = (NBTTagCompound) nbt;
			instance.setSuction(compound.getFloat("suction"), DistillateTypes.valueOf(compound.getString("suctionType")));
			if (instance instanceof IDistillateHandlerModifiable) {
				NBTTagList essentiaList = compound.getTagList("essentiaList", 10);
				for(int i = 0; i < essentiaList.tagCount(); i++)
					((IDistillateHandlerModifiable) instance).setContent(i, new DistillateStack((NBTTagCompound)essentiaList.get(i)));
				((IDistillateHandlerModifiable) instance).setMaxSize(compound.getInteger("maxSize"));
			}
		}

	}

}
