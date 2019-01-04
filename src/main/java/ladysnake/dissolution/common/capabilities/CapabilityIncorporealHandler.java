package ladysnake.dissolution.common.capabilities;

import ladylib.reflection.TypedReflection;
import ladylib.reflection.typed.TypedMethod2;
import ladysnake.dissolution.api.IDialogueStats;
import ladysnake.dissolution.api.SoulStrengthModifiedEvent;
import ladysnake.dissolution.api.corporeality.*;
import ladysnake.dissolution.api.possession.PossessionEvent;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PossessionMessage;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * This set of classes handles the Incorporeal capability.
 * It is used to store and read all the additional information (related to the ghost state) on players. <br>
 * The IncorporealDataHandler class itself is used to register the capability and query the right handler
 *
 * @author Pyrofab
 */
@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public class CapabilityIncorporealHandler {

    @CapabilityInject(IIncorporealHandler.class)
    private static Capability<IIncorporealHandler> CAPABILITY_INCORPOREAL;
    private static TypedMethod2<Entity, Float, Float, Void> entity$setSize =
            TypedReflection.findMethod(Entity.class, "func_70105_a", void.class, float.class, float.class);

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
            event.addCapability(new ResourceLocation(Ref.MOD_ID, "incorporeal"), provider);
        }
    }

    /**
     * This is a utility method to get the handler attached to an entity
     *
     * @param entity an entity that has the capability attached (in this case, a player)
     * @return the IncorporealHandler attached or null if there is none
     */
    public static Optional<IIncorporealHandler> getHandler(@Nullable Entity entity) {
//        if (!(entity instanceof EntityPlayer)) return Optional.empty();
        if (entity != null && entity.hasCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN)) {
            return Optional.ofNullable(entity.getCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN));
        }
        return Optional.empty();
    }

    @Nonnull
    public static IIncorporealHandler getHandler(@Nonnull EntityPlayer entityPlayer) {
        IIncorporealHandler handler = entityPlayer.getCapability(CAPABILITY_INCORPOREAL, EnumFacing.DOWN);
        if (handler == null) {
            return new DefaultIncorporealHandler();
        }
        return handler;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        IIncorporealHandler handler = getHandler(event.player);
        handler.tick();
    }

    /**
     * Synchronizes the corporeal status of newly tracked players
     */
    @SubscribeEvent
    public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayerMP) {
            EntityPlayerMP target = (EntityPlayerMP) event.getTarget();
            IIncorporealHandler handler = getHandler(target);
            PacketHandler.NET.sendTo(new IncorporealMessage(target.getEntityId(), handler.isStrongSoul(), handler.getCorporealityStatus()), (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    /**
     * This is the class that does most of the work, and the one other classes interact with
     *
     * @author Pyrofab
     */
    public static class DefaultIncorporealHandler implements IIncorporealHandler {

        private boolean strongSoul;
        private ICorporealityStatus corporealityStatus = SoulStates.BODY;
        private DialogueStats dialogueStats = new DialogueStats(this);
        private IDeathStats deathStats = new DeathStats();
        private int lastFood = -1;
        private boolean synced = false;
        private UUID hostUUID;
        private int hostID;
        private NBTTagCompound serializedPossessedEntity;

        private EntityPlayer owner;

        /**
         * Only there for internal instantiation by forge
         */
        DefaultIncorporealHandler() {}

        DefaultIncorporealHandler(EntityPlayer owner) {
            this.owner = owner;
        }

        @Override
        public boolean isStrongSoul() {
            return Dissolution.config.forceRemnant.getValue(strongSoul);
        }

        @Override
        public void setStrongSoul(boolean strongSoul) {
            if (owner == null || MinecraftForge.EVENT_BUS.post(new SoulStrengthModifiedEvent(owner, strongSoul))) {
                return;
            }
            if (!strongSoul && this.getCorporealityStatus().isIncorporeal()) {
                MinecraftForge.EVENT_BUS.post(new PlayerIncorporealEvent(owner, SoulStates.BODY, true));
                this.setCorporealityStatus0(SoulStates.BODY);
            }
            this.strongSoul = strongSoul;
            if (owner instanceof EntityPlayerMP && ((EntityPlayerMP) owner).connection != null) {
                IncorporealMessage message = new IncorporealMessage(owner.getEntityId(), strongSoul, this.corporealityStatus);
                PacketHandler.NET.sendTo(message, (EntityPlayerMP) owner);
                PacketHandler.NET.sendToAllTracking(message, owner);
            }
            setSynced(true);
        }

        @Override
        public void setCorporealityStatus(ICorporealityStatus newStatus) {
            if (!this.isStrongSoul() || newStatus == corporealityStatus) {
                return;
            }
            if (owner == null || MinecraftForge.EVENT_BUS.post(new PlayerIncorporealEvent(owner, newStatus, false))) {
                return;
            }

            if(newStatus == null) {
                newStatus = SoulStates.BODY;
            }

            setCorporealityStatus0(newStatus);

            if (owner instanceof EntityPlayerMP && ((EntityPlayerMP) owner).connection != null) {
                IncorporealMessage message = new IncorporealMessage(owner.getEntityId(), strongSoul, newStatus);
                PacketHandler.NET.sendTo(message, (EntityPlayerMP) owner);
                PacketHandler.NET.sendToAllTracking(message, owner);
            }
        }

        private void setCorporealityStatus0(ICorporealityStatus newStatus) {
            corporealityStatus.resetState(owner);

            corporealityStatus = newStatus;

            corporealityStatus.initState(owner);

            if (!newStatus.isIncorporeal() && this.getPossessed() != null) {
                this.setPossessed(null);
            }
        }

        @Nonnull
        @Override
        public ICorporealityStatus getCorporealityStatus() {
            return this.corporealityStatus;
        }

        /**
         * Sets the entity possessed by this player
         * @param possessable the entity to possess. If null, will end existing possession
         * @param force if true, the operation cannot fail under normal circumstances
         * @return true if the operation succeeded
         */
        @Override
        public <T extends EntityLivingBase & IPossessable> boolean setPossessed(@Nullable T possessable, boolean force) {
            if (!this.isStrongSoul()) {
                return false;
            }
            owner.clearActivePotions();
            if (possessable == null) {          // stop the current possession
                IPossessable currentHost = getPossessed();
                // cancel the operation if a) the event is canceled or b) the possessed entity denies it
                if (MinecraftForge.EVENT_BUS.post(new PossessionEvent.Stop(owner, (EntityLivingBase & IPossessable) currentHost, force)) ||
                        currentHost != null && !currentHost.onPossessionStop(owner, force || owner.isCreative())) {
                    return false;
                }
                hostID = 0;
                hostUUID = null;
                owner.setInvisible(Dissolution.config.ghost.invisibleGhosts);   // restore previous visibility
                owner.capabilities.allowFlying = true;
                serializedPossessedEntity = null;
            } else {                            // start possessing an entity
                // cancel the operation if a) the event is canceled or b) the possessed entity denies it
                if (MinecraftForge.EVENT_BUS.post(new PossessionEvent.Start(owner, possessable, force)) ||
                        !possessable.onEntityPossessed(owner)) {
                    return false;
                }
                hostID = possessable.getEntityId();
                hostUUID = possessable.getUniqueID();
                owner.setInvisible(true);   // prevent the soul from being seen at all
                owner.capabilities.allowFlying = owner.isCreative();
                owner.capabilities.isFlying = owner.isCreative() && owner.capabilities.isFlying;
            }
            syncWithClient(possessable);
            return true;
        }

        private <T extends EntityLivingBase & IPossessable> void syncWithClient(@Nullable T possessable) {
            if (owner instanceof EntityPlayerMP && ((EntityPlayerMP) owner).connection != null) {
                ((EntityPlayerMP) owner).connection.sendPacket(new SPacketCamera(possessable == null ? owner : possessable));
                PossessionMessage message = new PossessionMessage(owner.getUniqueID(), hostID);
                PacketHandler.NET.sendTo(message, (EntityPlayerMP) owner);
                PacketHandler.NET.sendToAllTracking(message, owner);
            }
        }

        @Override
        public UUID getPossessedUUID() {
            return hostUUID;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends EntityLivingBase & IPossessable> T getPossessed() {
            if (hostUUID == null || !this.isStrongSoul()) {
                return null;
            }
            Entity host = tryFindPossessedEntity();
            if (!(host instanceof EntityMob && host instanceof IPossessable)) {
                if (host != null) {
                    Dissolution.LOGGER.warn("{}'s possessed entity is supposed to be \"{}\" but it cannot be possessed", owner, host);
                }
                host = null;
                hostID = 0;
                hostUUID = null;
                owner.capabilities.allowFlying = true;
                owner.setInvisible(Dissolution.config.ghost.invisibleGhosts);
                syncWithClient(null);
            }
            return (T) host;
        }

        public Entity tryFindPossessedEntity() {
            // First attempt: use the network id (client & server)
            Entity host = this.owner.world.getEntityByID(hostID);
            if (host == null) {
                if (owner.world instanceof WorldServer) {
                    // Second attempt: use the UUID (server)
                    host = ((WorldServer) owner.world).getEntityFromUuid(hostUUID);
                }
                if (host == null) {
                    Dissolution.LOGGER.warn("{}: this player's possessed entity is nowhere to be found", owner);
                } else if (host instanceof EntityLivingBase && host instanceof IPossessable) {
                    this.setPossessed((EntityLivingBase & IPossessable) host);
                }
            }
            return host;
        }

        @Nonnull
        @Override
        public IDialogueStats getDialogueStats() {
            return this.dialogueStats;
        }

        @Override
        public IDeathStats getDeathStats() {
            return this.deathStats;
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
        public NBTTagCompound getSerializedPossessedEntity() {
            return serializedPossessedEntity;
        }

        @Override
        public void setSerializedPossessedEntity(NBTTagCompound serializedPossessedEntity) {
            this.serializedPossessedEntity = serializedPossessedEntity;
        }

        @Override
        public void tick() {
            if (getCorporealityStatus().isIncorporeal()) {
                if (this.lastFood < 0) {
                    lastFood = owner.getFoodStats().getFoodLevel();
                } else {
                    owner.getFoodStats().setFoodLevel(lastFood);
                }
                Entity possessed = getPossessed();
                if (possessed != null) {
                    float width = possessed.width;
                    float height = possessed.height;
                    AxisAlignedBB aabb = owner.getEntityBoundingBox();
                    double radius = (double)owner.width / 2.0D;
                    entity$setSize.invoke(owner, width, height);
                    // We need to set the bounding box ourselves otherwise the player will start sliding in Entity#resetPositionToBB
                    // Fix taken from Resize Potion (https://github.com/CammiePone/Resize-Potion/blob/master/src/main/java/com/camellias/resizer/handlers/PotionHandler.java#L204)
                    owner.setEntityBoundingBox(new AxisAlignedBB(owner.posX - radius, aabb.minY, owner.posZ - radius,
                            owner.posX + radius, aabb.minY + (double)owner.height, owner.posZ + radius));
                }
            } else {
                lastFood = -1;
            }
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
     *
     * @author Pyrofab
     */
    public static class Storage implements Capability.IStorage<IIncorporealHandler> {

        @Override
        public NBTBase writeNBT(Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side) {
            final NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("strongSoul", instance.isStrongSoul());
            tag.setString("corporealityStatus", Objects.requireNonNull(instance.getCorporealityStatus().getRegistryName()).toString());
            Entity possessed = instance.getPossessed();
            if (possessed != null) {
                tag.setUniqueId("possessedEntity", possessed.getUniqueID());
                tag.setTag("serializedPossessedEntity", possessed.serializeNBT());
            }
            tag.setTag("dialogueStats", instance.getDialogueStats().serializeNBT());
            tag.setTag("deathStats", instance.getDeathStats().serializeNBT());
            return tag;
        }

        @Override
        public void readNBT(Capability<IIncorporealHandler> capability, IIncorporealHandler instance, EnumFacing side, NBTBase nbt) {
            final NBTTagCompound tag = (NBTTagCompound) nbt;
            instance.setStrongSoul(((NBTTagCompound) nbt).getBoolean("strongSoul"));
            instance.setCorporealityStatus(SoulStates.REGISTRY.getValue(new ResourceLocation(tag.getString("corporealityStatus"))));
            if (instance instanceof DefaultIncorporealHandler) {
                DefaultIncorporealHandler defaultInstance = (DefaultIncorporealHandler) instance;
                if (tag.hasKey("possessedEntity")) {
                    defaultInstance.hostUUID = tag.getUniqueId("possessedEntity");
                }
            }
            if (tag.hasKey("serializedPossessedEntity")) {
                instance.setSerializedPossessedEntity(tag.getCompoundTag("serializedPossessedEntity"));
            }
            instance.getDialogueStats().deserializeNBT(tag.getCompoundTag("dialogueStats"));
            instance.getDeathStats().deserializeNBT(tag.getCompoundTag("deathStats"));
        }
    }

}
