package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.*;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.config.DissolutionConfigManager.FlightModes;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PossessionMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;

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
	 * Attaches a {@link CapabilityIncorporealHandler} to players.
	 */
	@SubscribeEvent
	public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if ((event.getObject() instanceof EntityPlayer)) {
			Provider provider = new Provider((EntityPlayer) event.getObject());
			event.addCapability(new ResourceLocation(Reference.MOD_ID, "incorporeal"), provider);
		}
	}

    /**
     * This is a utility method to get the handler attached to an entity
     * @param entity an entity that has the capability attached (in this case, a player)
     * @return the IncorporealHandler attached or null if there is none
     */
    public static IIncorporealHandler getHandler(@Nullable Entity entity) {
		if(!(entity instanceof EntityPlayer)) return null;
        if (entity.hasCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN))
            return entity.getCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN);

        return null;
    }

	@Nonnull
    public static IIncorporealHandler getHandler(@Nonnull EntityPlayer entityPlayer) {
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
		if(handler.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL || handler.getPossessed() != null) {
			float size = event.player.isRiding() ? 0f : 0.8f;
			try {
				entity$setSize.invoke(event.player, event.player.width, size);
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

		private boolean strongSoul;
		@Nonnull
		private CorporealityStatus corporealityStatus = CorporealityStatus.BODY;
		private EctoplasmStats ectoplasmStats = new EctoplasmStats();
		private DialogueStats dialogueStats = new DialogueStats(this);
		private int lastFood = -1;
		private String lastDeathMessage;
		private boolean synced = false;
		/**Not used currently, allows the player to wear a different skin*/
		private UUID disguise = null;
		private UUID hostUUID;
		private int hostID;

		private EntityPlayer owner;

		/**Only there for internal instantiation by forge*/
		DefaultIncorporealHandler() {}

		DefaultIncorporealHandler(EntityPlayer owner) {
			this.owner = owner;
		}

		@Override
		public boolean isStrongSoul() {
			return DissolutionConfig.enforcedSoulStrength.getValue(strongSoul);
		}

		@Override
		public void setStrongSoul(boolean strongSoul) {
			if(owner == null || MinecraftForge.EVENT_BUS.post(new SoulStrengthModifiedEvent(owner, strongSoul))) return;
			this.strongSoul = strongSoul;
			if(!isStrongSoul()) {
				//noinspection ConstantConditions
				owner.setSpawnPoint(null, true);
			}
			if(!owner.world.isRemote)
				PacketHandler.NET.sendToAll(new IncorporealMessage(owner.getUniqueID().getMostSignificantBits(),
						owner.getUniqueID().getLeastSignificantBits(), strongSoul, corporealityStatus));
		}

		@Override
		public void setCorporealityStatus(CorporealityStatus newStatus) {
			if(!this.isStrongSoul()) return;
			if(owner == null || MinecraftForge.EVENT_BUS.post(new PlayerIncorporealEvent(owner, newStatus))) return;

			corporealityStatus = newStatus;

			if(!newStatus.isIncorporeal() && this.getPossessed() != null) this.setPossessed(null);

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
				boolean enableFlight = (!DissolutionConfigManager.isFlightSetTo(FlightModes.NO_FLIGHT)) && (!DissolutionConfigManager.isFlightSetTo(FlightModes.CUSTOM_FLIGHT));
				owner.capabilities.disableDamage = newStatus == CorporealityStatus.SOUL;
				owner.capabilities.allowFlying = (newStatus.isIncorporeal() && (owner.experienceLevel > 0) && enableFlight);
				owner.capabilities.isFlying = (newStatus.isIncorporeal() && owner.capabilities.isFlying && owner.experienceLevel > 0 && enableFlight);
			}
			if(!owner.world.isRemote) {
				PacketHandler.NET.sendToAll(new IncorporealMessage(owner.getUniqueID().getMostSignificantBits(),
					owner.getUniqueID().getLeastSignificantBits(), strongSoul, newStatus));
			}
			setSynced(true);
		}

		@Nonnull
		@Override
		public CorporealityStatus getCorporealityStatus() {
			return this.isStrongSoul() ? this.corporealityStatus : CorporealityStatus.BODY;
		}

		@Override
		public boolean setPossessed(IPossessable possessable) {
			if(!this.isStrongSoul()) return false;
			if(possessable != null && !(possessable instanceof Entity))
				throw new IllegalArgumentException("A player can only possess an entity.");
			owner.clearActivePotions();
			if(possessable == null) {
				IPossessable currentHost = getPossessed();
				if (currentHost != null && !currentHost.onPossessionStop(owner)) return false;
				hostID = 0;
				hostUUID = null;
				owner.setInvisible(DissolutionConfig.ghost.invisibleGhosts);
				owner.dismountRidingEntity();
			} else {
				if(!possessable.onEntityPossessed(owner)) return false;
				hostID = ((Entity)possessable).getEntityId();
				hostUUID = ((Entity)possessable).getUniqueID();
				owner.setInvisible(true);
			}
			if(!owner.world.isRemote) {
				((EntityPlayerMP) owner).connection.sendPacket(new SPacketCamera(possessable == null ? owner : (Entity) possessable));
				PacketHandler.NET.sendToAllAround(new PossessionMessage(owner.getUniqueID(), hostID),
						new NetworkRegistry.TargetPoint(owner.dimension, owner.posX, owner.posY, owner.posZ, 100));
			}
			return true;
		}

		@Override
		public IPossessable getPossessed() {
			if(hostUUID == null || !this.isStrongSoul())
				return null;
			Entity host = this.owner.world.getEntityByID(hostID);
			if(host == null) {
				List<Entity> list = owner.getEntityWorld().getEntitiesWithinAABB(Entity.class,
						new AxisAlignedBB(new BlockPos(owner)),
						e -> e != null && e.getUniqueID().equals(hostUUID));
				if(!list.isEmpty()) {
					host = list.get(0);
					this.setPossessed((IPossessable) host);
				} else {
					LogManager.getLogger().debug(String.format("%s: this player's possessed entity is nowhere to be found", owner));
				}
			}
			return (IPossessable) host;
		}

		@Nonnull
		@Override
		public IEctoplasmStats getEctoplasmStats() {
			return this.ectoplasmStats;
		}

		@Nonnull
		@Override
		public IDialogueStats getDialogueStats() {
			return this.dialogueStats;
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

		public EntityPlayer getOwner() {
			return owner;
		}
	}

	 public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

	        IIncorporealHandler instance;

	        Provider(EntityPlayer owner) {
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
				tag.setBoolean("strongSoul", instance.isStrongSoul());
		        tag.setString("corporealityStatus", instance.getCorporealityStatus().name());
		        tag.setString("lastDeath", instance.getLastDeathMessage() == null || instance.getLastDeathMessage().isEmpty() ? "This player has no recorded death" : instance.getLastDeathMessage());
		        if(instance.getPossessed() instanceof Entity)
		        	tag.setUniqueId("possessedEntity", ((Entity) instance.getPossessed()).getUniqueID());
		        tag.setTag("dialogueStats", instance.getDialogueStats().serializeNBT());
		        return tag;
		    }

		    @Override
		    public void readNBT (Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side, NBTBase nbt) {
		        final NBTTagCompound tag = (NBTTagCompound) nbt;
		        instance.setStrongSoul(((NBTTagCompound) nbt).getBoolean("strongSoul"));
		        instance.setCorporealityStatus(IIncorporealHandler.CorporealityStatus.valueOf(tag.getString("corporealityStatus")));
		        if(instance instanceof DefaultIncorporealHandler) {
		        	UUID hostUUID = tag.getUniqueId("possessedEntity");
					((DefaultIncorporealHandler) instance).hostUUID = hostUUID == new UUID(0,0) ? null : hostUUID;
		        }
		        instance.setLastDeathMessage(tag.getString("lastDeath"));
		        instance.getDialogueStats().deserializeNBT(tag.getCompoundTag("dialogueStats"));
		    }
		}

}
