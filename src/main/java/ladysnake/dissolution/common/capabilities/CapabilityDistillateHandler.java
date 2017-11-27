package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.DistillateStack;
import ladysnake.dissolution.api.DistillateTypes;
import ladysnake.dissolution.api.GenericStack;
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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CapabilityDistillateHandler {

    @CapabilityInject(IDistillateHandler.class)
    @Nonnull
    public static Capability<IDistillateHandler> CAPABILITY_DISTILLATE;

    public static void register() {
        CapabilityManager.INSTANCE.register(IDistillateHandler.class, new Storage(), () -> new DefaultDistillateHandler(1));
    }

    public static IDistillateHandler getHandler(TileEntity te) {

        if (te.hasCapability(CAPABILITY_DISTILLATE, EnumFacing.DOWN))
            return te.getCapability(CAPABILITY_DISTILLATE, EnumFacing.DOWN);

        return null;
    }

    /**
     * Don't use these methods except for loading content from disk
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
     */
    public static class DefaultDistillateHandler implements IDistillateHandler, IDistillateHandlerModifiable {

        private final Map<DistillateTypes, Float> suction = new HashMap<>();
        private int maxSize;
        private final DistillateList content;

        public DefaultDistillateHandler(int size) {
            this(size, 1);
        }

        public DefaultDistillateHandler(int size, int channels) {
            this.maxSize = size;
            this.content = DistillateList.withSize(channels);
        }

        @Override
        public void setSuction(DistillateTypes type, float suction) {
            this.suction.put(type, suction);
        }

        @Override
        public float getSuction(DistillateTypes type) {
            return this.suction.getOrDefault(type, this.suction.getOrDefault(DistillateTypes.UNTYPED, 0f));
        }

        @Override
        public DistillateStack insert(GenericStack<DistillateTypes> stack) {
            int i = this.content.indexOf(stack.getType());
            if (i == -1)
                i = this.content.indexOf(DistillateTypes.UNTYPED);
            if (i > -1) {
                GenericStack<DistillateTypes> stack2 = this.content.get(i);
                int currentAmount = stack2.getCount();
                int amount = Math.min(stack.getCount() + currentAmount, maxSize);
                this.content.set(i, (DistillateStack) stack.withSize(amount));
                stack = stack.smaller(amount - currentAmount);
            }
            return (DistillateStack) stack;
        }

        @Override
        public DistillateStack extract(int amount, DistillateTypes type) {
            DistillateStack contentStack = (DistillateStack) this.content.get(type);
            if (contentStack.isEmpty())
                return (DistillateStack) DistillateStack.EMPTY;
            amount = Math.min(amount, contentStack.getCount());
            DistillateStack ret = (DistillateStack) contentStack.withSize(amount);
            contentStack.shrink(amount);
            return ret;
        }

        @Override
        public DistillateStack readContent(DistillateTypes type) {
            return new DistillateStack((DistillateStack) this.content.get(type));
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
        @Nonnull
        public Iterator<DistillateStack> iterator() {
            return new Iterator<DistillateStack>() {
                Iterator delegate = DefaultDistillateHandler.this.content.iterator();

                @Override
                public boolean hasNext() {
                    return delegate.hasNext();
                }

                @Override
                public DistillateStack next() {
                    return (DistillateStack) delegate.next();
                }
            };
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

        final IDistillateHandler instance = CAPABILITY_DISTILLATE.getDefaultInstance();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
            return capability == CAPABILITY_DISTILLATE;
        }

        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
            return hasCapability(capability, facing) ? CAPABILITY_DISTILLATE.cast(instance) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return (NBTTagCompound) CAPABILITY_DISTILLATE.getStorage().writeNBT(CAPABILITY_DISTILLATE, instance, null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            CAPABILITY_DISTILLATE.getStorage().readNBT(CAPABILITY_DISTILLATE, instance, null, nbt);
        }

    }

    public static class Storage implements Capability.IStorage<IDistillateHandler> {

        @Override
        public NBTBase writeNBT(Capability<IDistillateHandler> capability, IDistillateHandler instance, EnumFacing side) {
            NBTTagCompound ret = new NBTTagCompound();
            NBTTagList suctions = new NBTTagList();
            for (DistillateTypes distillateType : DistillateTypes.values()) {
                NBTTagCompound succ = new NBTTagCompound();
                ret.setString("suctionType", distillateType.name());
                succ.setFloat("suction", instance.getSuction(distillateType));
            }
            ret.setTag("suctions", suctions);
            ret.setInteger("maxSize", instance.getMaxSize());
            NBTTagList essentiaList = new NBTTagList();
            for (DistillateStack stack : instance)
                essentiaList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            ret.setTag("essentiaList", essentiaList);
            return ret;
        }

        @Override
        public void readNBT(Capability<IDistillateHandler> capability, IDistillateHandler instance, EnumFacing side,
                            NBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            for (NBTBase suction : compound.getTagList("suctions", 10)) {
                instance.setSuction(DistillateTypes.valueOf(((NBTTagCompound) suction).getString("suctionType")), ((NBTTagCompound) suction).getFloat("suction"));
            }
            if (instance instanceof IDistillateHandlerModifiable) {
                NBTTagList essentiaList = compound.getTagList("essentiaList", 10);
                for (int i = 0; i < essentiaList.tagCount(); i++)
                    ((IDistillateHandlerModifiable) instance).setContent(i, new DistillateStack((NBTTagCompound) essentiaList.get(i)));
                ((IDistillateHandlerModifiable) instance).setMaxSize(compound.getInteger("maxSize"));
            }
        }

    }

}
