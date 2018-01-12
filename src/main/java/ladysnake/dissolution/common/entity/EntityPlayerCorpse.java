package ladysnake.dissolution.common.entity;

import com.google.common.base.Optional;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.ISoulHandler;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.inventory.GuiProxy;
import ladysnake.dissolution.common.inventory.InventoryPlayerCorpse;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("Guava")
public class EntityPlayerCorpse extends AbstractMinion implements ISoulInteractable {

    private static DataParameter<Optional<UUID>> PLAYER = EntityDataManager.createKey(EntityPlayerCorpse.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> DECOMPOSITION_COUNTDOWN = EntityDataManager
            .createKey(EntityPlayerCorpse.class, DataSerializers.VARINT);
    protected InventoryPlayerCorpse inventory;

    public static final int MAX_DECAY_TIME = 6000;

    public EntityPlayerCorpse(World worldIn) {
        super(worldIn);
        inventory = new InventoryPlayerCorpse(this);
        this.setInert(true);
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!canEntityAccess(this, player)) return false;
        final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
        if (this.ticksExisted > 100 && (!this.isDecaying() || Dissolution.config.respawn.wowLikeRespawn)) {
            if (!world.isRemote) {
                DissolutionInventoryHelper.transferEquipment(this, player);
                this.onDeath(DamageSource.GENERIC);
                this.setDead();
                handler.setCorporealityStatus(SoulStates.BODY);
            }
            player.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            player.cameraPitch = 90;
            player.prevCameraPitch = 90;
            player.setHealth(4f);
        } else if (!world.isRemote
                && (!handler.getCorporealityStatus().isIncorporeal() || handler.getPossessed() != null)) {
            player.openGui(Dissolution.instance, GuiProxy.PLAYER_CORPSE, this.world, (int) this.posX, (int) this.posY, (int) this.posZ);
        }
        return true;
    }

    /**
     * Checks if a given entity can access this corpse
     *
     * @param entityPlayerCorpse the player corpse entity that's being interacted with
     * @param entity an entity trying to access this corpse
     * @return true if the entity meets all the requirement, or the config option is disabled
     */
    private static boolean canEntityAccess(EntityPlayerCorpse entityPlayerCorpse, Entity entity) {
        if (Dissolution.config.entities.lockPlayerCorpses) {
            if (entity == null) return false;
            if (!Objects.equals(entity.getUniqueID(), entityPlayerCorpse.getOwnerId())) {
                if (!(entity instanceof EntityPlayer)) return false;
                World world = entityPlayerCorpse.getEntityWorld();
//                if (world.isRemote && ((EntityPlayerSP) entity).getPermissionLevel() < 2)
//                    return false;
                MinecraftServer server = world.getMinecraftServer();
                if (server != null) {
                    UserListOpsEntry opsEntry = world.getMinecraftServer().getPlayerList().getOppedPlayers().getEntry(((EntityPlayer) entity).getGameProfile());
                    //noinspection ConstantConditions
                    int permissionLevel = opsEntry == null ? server.getOpPermissionLevel() : opsEntry.getPermissionLevel();
                    return permissionLevel >= 2;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isEntityInvulnerable(@Nonnull DamageSource source) {
        if (Dissolution.config.entities.lockPlayerCorpses && this.isInert()) {
            return source.canHarmInCreative() || canEntityAccess(this, source.getTrueSource());
        }
        return super.isEntityInvulnerable(source);
    }

    @Override
    protected boolean isMobEligibleForAttention(EntityCreature other) {
        return !Dissolution.config.entities.lockPlayerCorpses && other instanceof EntityZombie && super.isMobEligibleForAttention(other);
    }

    @Override
    public boolean canBePossessedBy(EntityPlayer player) {
        return this.hasLifeStone() && canEntityAccess(this, player) && super.canBePossessedBy(player);
    }

    @Override
    public boolean onEntityPossessed(EntityPlayer player) {
        return this.hasLifeStone() && super.onEntityPossessed(player);
    }

    @Override
    public void setLifeStone(int gem) {
        super.setLifeStone(gem);
    }

    public InventoryPlayerCorpse getInventory() {
        return inventory;
    }

    public void setInventory(InventoryPlayerCorpse inventory) {
        this.inventory = inventory;
    }

    public boolean isDecaying() {
        return getRemainingTicks() >= 0 && !this.hasLifeStone() && Dissolution.config.entities.bodiesDespawn;
    }

    @Override
    public void setCustomNameTag(@Nonnull String name) {
        super.setCustomNameTag(name);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIMinionAttack(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.applyEntityAI();
    }


    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isDecaying()) {
            countdown();
            if (getRemainingTicks() == 0 && !this.world.isRemote) {
                this.setDead();
            }
        }
    }

    /**
     * @return the time (in ticks) that this entity has before disappearing
     */
    public int getRemainingTicks() {
        return this.getDataManager().get(DECOMPOSITION_COUNTDOWN);
    }

    /**
     * Sets the time in ticks this entity has left
     */
    public void setDecompositionCountdown(int countdown) {
        this.getDataManager().set(DECOMPOSITION_COUNTDOWN, countdown);
    }

    private void countdown() {
        setDecompositionCountdown(getRemainingTicks() - 1);
    }

    public int getMaxTimeRemaining() {
        return MAX_DECAY_TIME;
    }

    @Override
    protected void handleSunExposure() {
    }

    public UUID getPlayer() {
        return this.getDataManager().get(PLAYER).orNull();
    }

    public void setPlayer(UUID id) {
        this.getDataManager().set(PLAYER, Optional.of(id));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(PLAYER, Optional.absent());
        this.getDataManager().register(DECOMPOSITION_COUNTDOWN, MAX_DECAY_TIME);
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, @Nonnull DamageSource cause) {
        super.dropLoot(wasRecentlyHit, lootingModifier, cause);
        if (this.inventory != null)
            this.inventory.dropAllItems(this);
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        this.getEquipmentAndArmor().forEach(stack -> this.entityDropItem(stack, 0.5f));
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.inventory.readFromNBT(compound.getTagList("Inventory", 10));
        this.soulInventoryProvider.deserializeNBT((NBTTagCompound) compound.getTag("SoulInventory"));
        this.setPlayer(compound.getUniqueId("player"));
        this.setDecompositionCountdown(compound.getInteger("remainingTicks"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("SoulInventory", this.soulInventoryProvider.serializeNBT());
        compound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        compound.setUniqueId("player", this.getPlayer());
        compound.setInteger("remainingTicks", getRemainingTicks());
    }

    @Nonnull
    @Override
    public String toString() {
        return "EntityPlayerCorpse [inventory=" + inventory + ",\n player=" + this.getPlayer() + ",\n soulInventory=" + this.getSoulHandlerCapability() + "]";
    }

    private CapabilitySoulHandler.Provider soulInventoryProvider = new CapabilitySoulHandler.Provider();

    private ISoulHandler getSoulHandlerCapability() {
        return getCapability(CapabilitySoulHandler.CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilitySoulHandler.CAPABILITY_SOUL_INVENTORY)
            return soulInventoryProvider.hasCapability(capability, facing);
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilitySoulHandler.CAPABILITY_SOUL_INVENTORY)
            return soulInventoryProvider.getCapability(capability, facing);
        return super.getCapability(capability, facing);
    }

}
