package ladysnake.tartaros.common.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class IncorporealDataHandler {
	
	@CapabilityInject(IIncorporealHandler.class)
    public static final Capability<IIncorporealHandler> CAPABILITY_INCORPOREAL = null;
	
    public static void register() {
        CapabilityManager.INSTANCE.register(IIncorporealHandler.class, new Storage(), DefaultIncorporealHandler.class);
        MinecraftForge.EVENT_BUS.register(new IncorporealDataHandler());
    }
    
    public static IIncorporealHandler getHandler(Entity entity) {

        if (entity.hasCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN))
            return entity.getCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN);
        
        return null;
    }
	
	public static class DefaultIncorporealHandler implements IIncorporealHandler {
		
		private int incorporeal;
	
		@Override
		public void setIncorporeal(boolean enable, EntityPlayer p) {
			incorporeal = (enable) ? 1 : 0;
			if(!p.isCreative()) {
				//p.capabilities.allowEdit = (!enable);
				p.capabilities.allowFlying = (enable && p.experienceLevel > 0);
				p.capabilities.disableDamage = enable;
				p.capabilities.isFlying = (p.capabilities.isFlying && p.experienceLevel > 0);
				p.setEntityInvulnerable(enable);
				//System.out.println(p.capabilities.allowFlying + " " + (p.experienceLevel > 0));
			}
		}
		
		@Override 
		public void setIncorporeal(int ghostMode) {
			incorporeal = (ghostMode == 0) ? 0 : 1;
		}
	
		@Override
		public boolean isIncorporeal() {
			return incorporeal == 1;
		}
		
		public int getIncorporeal() {
			return incorporeal;
		}
	}
	
	 public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
	        
	        IIncorporealHandler instance = CAPABILITY_INCORPOREAL.getDefaultInstance();

	        @Override
	        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
	            
	            return capability == CAPABILITY_INCORPOREAL;
	        }

	        @Override
	        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
	            
	            return hasCapability(capability, facing) ? CAPABILITY_INCORPOREAL.<T>cast(instance) : null;
	        }

	        @Override
	        public NBTTagCompound serializeNBT() {
	            
	            return (NBTTagCompound) CAPABILITY_INCORPOREAL.getStorage().writeNBT(CAPABILITY_INCORPOREAL, instance, null);
	        }

	        @Override
	        public void deserializeNBT(NBTTagCompound nbt) {
	            
	            CAPABILITY_INCORPOREAL.getStorage().readNBT(CAPABILITY_INCORPOREAL, instance, null, nbt);
	        }
	    }
	 
	 public static class Storage implements Capability.IStorage<IIncorporealHandler> {

		    @Override
		    public NBTBase writeNBT (Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side) {
		        
		        final NBTTagCompound tag = new NBTTagCompound();           
		        tag.setInteger("incorporeal", instance.getIncorporeal());          
		        return tag;
		    }

		    @Override
		    public void readNBT (Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side, NBTBase nbt) {
		        
		        final NBTTagCompound tag = (NBTTagCompound) nbt;
		        instance.setIncorporeal(tag.getInteger("incorporeal"));
		    }
		}

}
