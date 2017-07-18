package ladysnake.dissolution.common.capabilities;

import java.util.ArrayList;

import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.GameType;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;

/**
 * This set of classes handles the Incorporeal capability. 
 * It is used to store and read all the additional information (related to the ghost state) on players. <br>
 * The IncorporealDataHandler class itself is used to register the capability and query the right handler
 * @author Pyrofab
 * 
 */
public class CapabilityIncorporealHandler {
	
	/**this is a list of hardcoded vanilla blocks that players can interact with*/
	public static ArrayList<Block> soulInteractableBlocks = new ArrayList<Block>();
	
	static {
		soulInteractableBlocks.add(Blocks.LEVER);
		soulInteractableBlocks.add(Blocks.GLASS_PANE);
	}
	
	@CapabilityInject(IIncorporealHandler.class)
    public static final Capability<IIncorporealHandler> CAPABILITY_INCORPOREAL = null;
	
    public static void register() {
        CapabilityManager.INSTANCE.register(IIncorporealHandler.class, new Storage(), DefaultIncorporealHandler.class);
        MinecraftForge.EVENT_BUS.register(new CapabilityIncorporealHandler());
    }
    
    /**
     * This is a utility method to get the handler attached to an entity
     * @param entity an entity that has the capability attached (in this case, a player)
     * @return the IncorporealHandler attached or null if there is none
     */
    public static IIncorporealHandler getHandler(Entity entity) {

        if (entity.hasCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN))
            return entity.getCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN);
        
        return null;
    }
	
    /**
     * This is the class that does most of the work, and the one other classes interact with
     * @author Pyrofab
     *
     */
	public static class DefaultIncorporealHandler implements IIncorporealHandler {
		
		private boolean incorporeal = false;
		/**How much time this entity will be intangible*/
		private int intangibleTimer = -1;
		private int lastFood = -1;
		private int mercuryCandleNearby = -1;
		private int sulfurCandleNearby = -1;
		private String lastDeathMessage;
		private boolean synced = false;
		private int prevGamemode = 0;
		
		private EntityPlayer owner;
		
		/**Only there in case of reflection by forge*/
		public DefaultIncorporealHandler () {}
		
		public DefaultIncorporealHandler (EntityPlayer owner) {
			this.owner = owner;
		}
	
		@Override
		public void setIncorporeal(boolean enable) {
			incorporeal = enable;
			owner.setEntityInvulnerable(enable);
			if(DissolutionConfig.flightMode == DissolutionConfig.CUSTOM_FLIGHT && owner.world.isRemote)
				owner.capabilities.setFlySpeed(enable ? 0.025f : 0.05f);
			
			try {
				ObfuscationReflectionHelper.setPrivateValue(Entity.class, owner, enable, "isImmuneToFire", "field_70178_ae");
			} catch (UnableToFindFieldException | UnableToAccessFieldException e) {
				e.printStackTrace();
			}
			
			owner.setInvisible(enable && DissolutionConfig.invisibleGhosts);

			if(!owner.isCreative()) {
				boolean enableFlight = (DissolutionConfig.flightMode != DissolutionConfig.NO_FLIGHT) && (DissolutionConfig.flightMode != DissolutionConfig.CUSTOM_FLIGHT);
				owner.capabilities.disableDamage = enable;
				owner.capabilities.allowFlying = (enable && (owner.experienceLevel > 0) && enableFlight);
				owner.capabilities.isFlying = (enable && owner.capabilities.isFlying && owner.experienceLevel > 0 && enableFlight);
			}
			if(!owner.world.isRemote) {
				PacketHandler.net.sendToAll(new IncorporealMessage(owner.getUniqueID().getMostSignificantBits(),
					owner.getUniqueID().getLeastSignificantBits(), enable));
			} else {
				GuiIngameForge.renderHotbar = !enable;
				GuiIngameForge.renderHealth = !enable;
				GuiIngameForge.renderFood = !enable;
				GuiIngameForge.renderArmor = !enable;
				GuiIngameForge.renderAir = !enable;
			}
			setSynced(true);
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
		public void tick() {
			if(this.isSoulCandleNearby(1)) this.mercuryCandleNearby--;
			if(this.isSoulCandleNearby(2)) this.sulfurCandleNearby--;
			if(isIncorporeal())
				if(this.lastFood < 0)
					lastFood = owner.getFoodStats().getFoodLevel();
				else
					owner.getFoodStats().setFoodLevel(lastFood);
			if(intangibleTimer > -1000) {
				final boolean prevIntangible = isIntangible();
				intangibleTimer--;
				if(prevIntangible && !isIntangible()) {
					setIntangible(false);
				}
			}
		}

		@Override
		public boolean setIntangible(boolean intangible) {
			if(intangible && this.isIncorporeal() && intangibleTimer <= -1000) {
				this.intangibleTimer = 100;
				if(owner != null && !owner.world.isRemote) {
					this.prevGamemode = ((EntityPlayerMP)owner).interactionManager.getGameType().getID();
					owner.setGameType(GameType.SPECTATOR);
				}
			} else if (!intangible) {
				this.intangibleTimer = -1;
				if(owner != null && !owner.world.isRemote)
					owner.setGameType(GameType.getByID(this.prevGamemode));
			}
			return true;
		}
		
		@Override
		public boolean isIntangible() {
			return this.intangibleTimer >= 0;
		}
	}
	
	 public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
	        
	        IIncorporealHandler instance;
	        
	        public Provider(EntityPlayer owner) {
	        	this.instance = new DefaultIncorporealHandler(owner);
			}

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
	            CAPABILITY_INCORPOREAL.getStorage().readNBT(CAPABILITY_INCORPOREAL, instance, EnumFacing.DOWN, nbt);
	        }
	    }
	 
	 /**
	  * This is what stores to and reads from the disk
	  * @author Pyrofab
	  *
	  */
	 public static class Storage implements Capability.IStorage<IIncorporealHandler> {

		    @Override
		    public NBTBase writeNBT (Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side) {
		        final NBTTagCompound tag = new NBTTagCompound();           
		        tag.setBoolean("incorporeal", instance.isIncorporeal());    
		        if(instance instanceof DefaultIncorporealHandler) {
		        	tag.setInteger("intangible", ((DefaultIncorporealHandler)instance).intangibleTimer);
		        	tag.setInteger("prevGamemode", ((DefaultIncorporealHandler)instance).prevGamemode);
		        } else {
		        	tag.setBoolean("intangible", instance.isIntangible());
		        }
		        tag.setString("lastDeath", instance.getLastDeathMessage() == null || instance.getLastDeathMessage().isEmpty() ? "This player has no recorded death" : instance.getLastDeathMessage());
		        return tag;
		    }

		    @Override
		    public void readNBT (Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side, NBTBase nbt) {
		        final NBTTagCompound tag = (NBTTagCompound) nbt;
		        instance.setIncorporeal(tag.getBoolean("incorporeal"));
		        if(instance instanceof DefaultIncorporealHandler) {
		        	((DefaultIncorporealHandler)instance).intangibleTimer = tag.getInteger("intangible");
		        	((DefaultIncorporealHandler)instance).prevGamemode = tag.getInteger("prevGamemode");
		        } else {
		        	instance.setIntangible(tag.getBoolean("intangible"));
		        }
		        instance.setLastDeathMessage(tag.getString("lastDeath"));
		    }
		}

}
