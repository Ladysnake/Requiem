package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.EctoplasmStats;
import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.DissolutionConfigManager;
import ladysnake.dissolution.common.DissolutionConfigManager.FlightModes;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * This set of classes handles the Incorporeal capability.
 * It is used to store and read all the additional information (related to the ghost state) on players. <br>
 * The IncorporealDataHandler class itself is used to register the capability and query the right handler
 * @author Pyrofab
 *
 */
@Mod.EventBusSubscriber(modid=Reference.MOD_ID)
public class CapabilityIncorporealHandler {

	@CapabilityInject(IIncorporealHandler.class)
	static Capability<IIncorporealHandler> CAPABILITY_INCORPOREAL;

	private static MethodHandle entity$setSize;

	static {
		try {
			Method m = ReflectionHelper.findMethod(Entity.class, "setSize", "func_70105_a", float.class, float.class);
			entity$setSize = MethodHandles.lookup().unreflect(m);
		} catch (UnableToFindFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

    public static void register() {
        CapabilityManager.INSTANCE.register(IIncorporealHandler.class, new Storage(), DefaultIncorporealHandler::new);
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

	@Nonnull
    public static IIncorporealHandler getHandler(EntityPlayer entityPlayer) {
    	IIncorporealHandler handler = entityPlayer.getCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN);
    	if(handler == null)
    		return new DefaultIncorporealHandler();
    	return handler;
	}
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(PlayerTickEvent event) {
    	if(event.phase != TickEvent.Phase.END) return;
    	IIncorporealHandler handler = getHandler(event.player);
    	handler.tick();
		if(handler.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL) {
			try {
				entity$setSize.invoke(event.player, event.player.width, 1f);
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}
	}

    /**
     * This is the class that does most of the work, and the one other classes interact with
     * @author Pyrofab
     *
     */
	public static class DefaultIncorporealHandler implements IIncorporealHandler {

		@Nonnull
		private CorporealityStatus corporealityStatus = CorporealityStatus.CORPOREAL;
		private EctoplasmStats ectoplasmHealth = new EctoplasmStats();
		private int lastFood = -1;
		private String lastDeathMessage;
		private boolean synced = false;
		private int prevGamemode = 0;
		/**Not used currently, allows the player to wear a different skin*/
		private UUID disguise = null;

		private EntityPlayer owner;

		/**Only there for internal instantiation by forge*/
		DefaultIncorporealHandler() {}

		DefaultIncorporealHandler(EntityPlayer owner) {
			this.owner = owner;
		}

		@Override
		public void setCorporealityStatus(CorporealityStatus newStatus) {
			if(owner == null || MinecraftForge.EVENT_BUS.post(new PlayerIncorporealEvent(owner, newStatus))) return;
			corporealityStatus = newStatus;
			owner.setEntityInvulnerable(newStatus == CorporealityStatus.SOUL);

			if(newStatus == CorporealityStatus.SOUL) {
				owner.eyeHeight = 0.8f;
			} else {
				owner.eyeHeight = owner.getDefaultEyeHeight();
			}

			try {
				ObfuscationReflectionHelper.setPrivateValue(Entity.class, owner, newStatus.isIncorporeal(), "isImmuneToFire", "field_70178_ae");
			} catch (UnableToFindFieldException | UnableToAccessFieldException e) {
				e.printStackTrace();
			}

			owner.setInvisible(newStatus.isIncorporeal() && DissolutionConfig.ghost.invisibleGhosts);

			if(!owner.isCreative()) {
				boolean enableFlight = (!DissolutionConfigManager.isFlightEnabled(FlightModes.NO_FLIGHT)) && (!DissolutionConfigManager.isFlightEnabled(FlightModes.CUSTOM_FLIGHT));
				owner.capabilities.disableDamage = newStatus == CorporealityStatus.SOUL;
				owner.capabilities.allowFlying = (newStatus.isIncorporeal() && (owner.experienceLevel > 0) && enableFlight);
				owner.capabilities.isFlying = (newStatus.isIncorporeal() && owner.capabilities.isFlying && owner.experienceLevel > 0 && enableFlight);
			}
			if(!owner.world.isRemote) {
				PacketHandler.net.sendToAll(new IncorporealMessage(owner.getUniqueID().getMostSignificantBits(),
					owner.getUniqueID().getLeastSignificantBits(), newStatus));
			}
			setSynced(true);
		}

		@Nonnull
		@Override
		public CorporealityStatus getCorporealityStatus() {
			return this.corporealityStatus;
		}

		@Override
		public EctoplasmStats getEctoplasmStats() {
			return this.ectoplasmHealth;
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
			if(getCorporealityStatus().isIncorporeal())
				if(this.lastFood < 0)
					lastFood = owner.getFoodStats().getFoodLevel();
				else
					owner.getFoodStats().setFoodLevel(lastFood);
			else
				lastFood = -1;
		}

		@Override
		public void setDisguise(UUID usurpedId) {
			disguise = usurpedId;
		}

		@Override
		public Optional<UUID> getDisguise() {
			return Optional.ofNullable(disguise);
		}
	}

	 public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

	        IIncorporealHandler instance;

	        public Provider(EntityPlayer owner) {
	        	this.instance = new DefaultIncorporealHandler(owner);
			}

	        @Override
	        public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
	            return capability == CAPABILITY_INCORPOREAL;
	        }

	        @Override
	        public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {

	            return hasCapability(capability, facing) ? CAPABILITY_INCORPOREAL.cast(instance) : null;
	        }

	        @Override
	        public NBTTagCompound serializeNBT() {
	            return (NBTTagCompound) CAPABILITY_INCORPOREAL.getStorage().writeNBT(CAPABILITY_INCORPOREAL, instance, null);
	        }

	        @Override
	        public void deserializeNBT(NBTTagCompound nbt) {
	        	try {
					CAPABILITY_INCORPOREAL.getStorage().readNBT(CAPABILITY_INCORPOREAL, instance, EnumFacing.DOWN, nbt);
				} catch (IllegalArgumentException e) {
					LogManager.getLogger().error("Could not load the state of a player", e);
				}
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
		        tag.setString("corporealityStatus", instance.getCorporealityStatus().name());
		        if(instance instanceof DefaultIncorporealHandler) {
		        	tag.setInteger("prevGamemode", ((DefaultIncorporealHandler)instance).prevGamemode);
		        }
		        tag.setString("lastDeath", instance.getLastDeathMessage() == null || instance.getLastDeathMessage().isEmpty() ? "This player has no recorded death" : instance.getLastDeathMessage());
		        return tag;
		    }

		    @Override
		    public void readNBT (Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side, NBTBase nbt) {
		        final NBTTagCompound tag = (NBTTagCompound) nbt;
		        instance.setCorporealityStatus(IIncorporealHandler.CorporealityStatus.valueOf(
		        		tag.getString("corporealityStatus")));
		        if(instance instanceof DefaultIncorporealHandler) {
		        	((DefaultIncorporealHandler)instance).prevGamemode = tag.getInteger("prevGamemode");
		        }
		        instance.setLastDeathMessage(tag.getString("lastDeath"));
		    }
		}

}
