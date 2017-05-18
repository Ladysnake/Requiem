package ladysnake.dissolution.common.capabilities;

import java.util.ArrayList;

import ladysnake.dissolution.common.TartarosConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class IncorporealDataHandler {
	
	public static ArrayList<Block> soulInteractableBlocks = new ArrayList<Block>();
	
	static {
		soulInteractableBlocks.add(Blocks.LEVER);
		soulInteractableBlocks.add(Blocks.GLASS_PANE);
	}
	
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
		
		private boolean incorporeal;
		private int mercuryCandleNearby = -1;
		private int sulfurCandleNearby = -1;
		private String lastDeathMessage;
		public boolean synced = false;
	
		@Override
		public void setIncorporeal(boolean enable, EntityPlayer p) {
			incorporeal = enable;
			p.setEntityInvulnerable(enable);
			if(TartarosConfig.flightMode == TartarosConfig.CUSTOM_FLIGHT)
				p.capabilities.setFlySpeed(enable ? 0.025f : 0.05f);
			ObfuscationReflectionHelper.setPrivateValue(Entity.class, p, true, "isImmuneToFire", "field_70178_ae");
			p.setInvisible(enable && TartarosConfig.invisibleGhosts);
			if(!p.isCreative()) {
				boolean enableFlight = (TartarosConfig.flightMode != TartarosConfig.NO_FLIGHT) && (TartarosConfig.flightMode != TartarosConfig.CUSTOM_FLIGHT);
				//p.capabilities.allowEdit = (!enable);
				p.capabilities.disableDamage = enable;
				p.capabilities.allowFlying = (enable && (p.experienceLevel > 0) && enableFlight);
				p.capabilities.isFlying = (enable && p.capabilities.isFlying && p.experienceLevel > 0 && enableFlight);
				//System.out.println(p.capabilities.allowFlying + " " + (p.experienceLevel > 0));
			}
			synced = true;
		}
		
		/**
		 * Used to load data
		 */
		@Override 
		public void setIncorporeal(boolean ghostMode) {
			incorporeal = ghostMode;
			synced = true;
		}
		
		@Override
		public void setSoulCandleNearby(boolean soulCandle, int CandleType) {
			if(CandleType == 1){
				this.mercuryCandleNearby = soulCandle ? 100 : -1;
			}
			else if(CandleType == 2){
				this.sulfurCandleNearby = soulCandle ? 100 : -1;
			}
			
		}
		
		@Override
		public boolean isSoulCandleNearby(int CandleType) {
			if(CandleType == 1){
				return this.mercuryCandleNearby > 0;
			}
			else if(CandleType == 2){
				return this.sulfurCandleNearby > 0;
			}
			else return this.mercuryCandleNearby > 0;
		}
	
		@Override
		public boolean isIncorporeal() {
			return this.incorporeal && !this.isSoulCandleNearby(1) || this.incorporeal && !this.isSoulCandleNearby(2);
		}
		
		@Override
		public void setSynced(boolean synced) {
			this.synced = synced;
		}

		@Override
		public boolean isSynced() {
			return this.synced;
		}

		@Override
		public String getLastDeathMessage() {
			return this.lastDeathMessage;
		}

		@Override
		public void setLastDeathMessage(String deathMessage) {
			this.lastDeathMessage = deathMessage;
		}
		
		@Override
		public void tick(PlayerTickEvent event) {
			if(this.isSoulCandleNearby(1)) this.mercuryCandleNearby--;
			if(this.isSoulCandleNearby(2)) this.sulfurCandleNearby--;
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
		        tag.setBoolean("incorporeal", instance.isIncorporeal());    
		        tag.setString("lastDeath", instance.getLastDeathMessage() == null || instance.getLastDeathMessage().isEmpty() ? "This player has no recorded death" : instance.getLastDeathMessage());
		        return tag;
		    }

		    @Override
		    public void readNBT (Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side, NBTBase nbt) {
		        
		        final NBTTagCompound tag = (NBTTagCompound) nbt;
		        instance.setIncorporeal(tag.getBoolean("incorporeal"));
		        instance.setLastDeathMessage(tag.getString("lastDeath"));
		    }
		}

}
