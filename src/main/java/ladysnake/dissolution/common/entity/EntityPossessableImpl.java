package ladysnake.dissolution.common.entity;

import com.google.common.base.Optional;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.ai.EntityAIInert;
import ladysnake.dissolution.common.entity.ai.attribute.AttributeHelper;
import ladysnake.dissolution.common.entity.ai.attribute.CooldownStrengthAttribute;
import ladysnake.dissolution.common.util.DelayedTaskRunner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

/**
 * The template class for possessable entities. <br>
 * Used in {@link PossessableEntityFactory} to generate possessable versions of any mob. <br>
 * Note: do <b>NOT</b> check whether entities are instances of this class, it will always return false.
 */
@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public class EntityPossessableImpl extends EntityMob implements IPossessable {
    private static final DataParameter<Optional<UUID>> POSSESSING_ENTITY_ID =
            EntityDataManager.createKey(EntityPossessableImpl.class, DataSerializers.OPTIONAL_UNIQUE_ID);

    private EntityAIInert aiDontDoShit = new EntityAIInert(false);

    private boolean sleeping;
    // Fields used to track external changes to this entity's motion
    private double prevMotionX;
    private double prevMotionY;
    private double prevMotionZ;
    private Entity dummyRidingEntity;
    private int ridingEntityQueries;

    public EntityPossessableImpl(World worldIn) {
        super(worldIn);
        if (worldIn != null && !worldIn.isRemote) {
            this.tasks.addTask(99, aiDontDoShit);
        }
    }

    @Override
    public boolean onEntityPossessed(EntityPlayer player) {
        if (this.getPossessingEntity() == player)
            return true;
        if (this.getPossessingEntity() != null) {
            Dissolution.LOGGER.warn("A player attempted to possess an entity that was already possessed");
            return false;
        }
        this.setPossessingEntity(player.getUniqueID());
        player.eyeHeight = this.getEyeHeight();
        this.aiDontDoShit.setShouldExecute(true);
        return true;
    }

    @Override
    public boolean onPossessionStop(EntityPlayer player, boolean force) {
        if (!player.getUniqueID().equals(this.getPossessingEntityId())) {
            return true;
        }
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
        if (!handler.getCorporealityStatus().isIncorporeal() || this.dead || force) {
            this.setPossessingEntity(null);
            player.eyeHeight = player.getDefaultEyeHeight();
            this.aiDontDoShit.setShouldExecute(false);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public UUID getPossessingEntityId() {
        return this.getDataManager().get(POSSESSING_ENTITY_ID).orNull();
    }

    @Nullable
    @Override
    public EntityPlayer getPossessingEntity() {
        UUID possessingId = getPossessingEntityId();
        return possessingId == null ? null : world.getPlayerEntityByUUID(possessingId);
    }

    private void setPossessingEntity(@Nullable UUID possessingEntity) {
        if (!world.isRemote) {
            this.aiDontDoShit.setShouldExecute(possessingEntity != null);
        }
        this.getDataManager().set(POSSESSING_ENTITY_ID, Optional.fromNullable(possessingEntity));
    }

    @Override
    public boolean canBePossessedBy(EntityPlayer player) {
        EntityPlayer possessingEntity = this.getPossessingEntity();
        return possessingEntity == null || possessingEntity == player;
    }

    @Override
    public void markForLogOut() {
        this.world.removeEntity(this);
    }

    @Override
    public void updatePossessing() {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            this.setPosition(possessing.posX, possessing.posY, possessing.posZ);
        }
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (this.isBeingPossessed()) {
                EntityPlayer possessing = Objects.requireNonNull(this.getPossessingEntity());
                if (this.prevMotionX != this.motionX || this.prevMotionY != this.motionY || this.prevMotionZ != this.motionZ) {
                    possessing.motionX += this.motionX - this.prevMotionX;
                    possessing.motionY += this.motionY - this.prevMotionY;
                    possessing.motionZ += this.motionZ - this.prevMotionZ;
                    possessing.velocityChanged = true;
                }
                if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
                    possessing.sendStatusMessage(new TextComponentTranslation("dissolution.message.peaceful_despawn"), true);
                    ((WorldServer)world).spawnParticle((EntityPlayerMP)possessing, EnumParticleTypes.SMOKE_NORMAL, false, posX, posY, posZ, 30, 0, 0.5,0, 0.5);
                }
            }
        }
        super.onUpdate();
        this.prevMotionX = this.motionX;
        this.prevMotionY = this.motionY;
        this.prevMotionZ = this.motionZ;
    }

    @Override
    public boolean proxyAttack(EntityLivingBase entity, DamageSource source, float amount) {
        DamageSource newSource = null;
        if (source instanceof EntityDamageSourceIndirect)
            //noinspection ConstantConditions
            newSource = new EntityDamageSourceIndirect(source.getDamageType(), source.getImmediateSource(), this);
        else if (source instanceof EntityDamageSource)
            newSource = new EntityDamageSource(source.getDamageType(), this);
        if (newSource != null) {
            entity.attackEntityFrom(newSource, amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean isEntityInvulnerable(@Nonnull DamageSource source) {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null && possessing.isCreative() || possessing == source.getTrueSource()) {
            return true;
        }
        return super.isEntityInvulnerable(source);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(POSSESSING_ENTITY_ID, Optional.absent());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AttributeHelper.substituteAttributeInstance(this.getAttributeMap(), new CooldownStrengthAttribute(this));
    }

    @Nullable
    @Override
    public Entity changeDimension(int dimensionIn, @Nonnull ITeleporter teleporter) {
        // Teleports the player along with the entity
        final EntityPlayer passenger = this.getPossessingEntity();
        if (passenger != null && !world.isRemote) {
            CapabilityIncorporealHandler.getHandler(passenger).setPossessed(null, true);
            ((EntityPlayerMP)passenger).connection.sendPacket(new SPacketCamera(this));
            passenger.setPosition(posX, posY, posZ);
            passenger.timeUntilPortal = passenger.getPortalCooldown();
        }
        final Entity clone = super.changeDimension(dimensionIn, teleporter);
        if (passenger != null && clone != null && !world.isRemote) {
            // We need to delay the player's dimension change slightly, otherwise it can cause a concurrent modification crash
            DelayedTaskRunner.INSTANCE.addDelayedTask(dimensionIn, 0, () -> {
                passenger.changeDimension(dimensionIn, teleporter);
                CapabilityIncorporealHandler.getHandler(passenger).setPossessed((EntityLivingBase & IPossessable) clone, true);
            });
            // Sometimes the client doesn't get notified, so we update again half a second later to be sure
            DelayedTaskRunner.INSTANCE.addDelayedTask(dimensionIn, 10, () -> CapabilityIncorporealHandler.getHandler(passenger).setPossessed((EntityLivingBase & IPossessable) clone, true));
        }

        return clone;
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.sleeping = sleeping;
    }

    @Override
    public boolean isPlayerSleeping() {
        return sleeping;
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, @Nonnull IBlockState state, @Nonnull BlockPos pos) {
        // Prevent fall damage on ladders
        if (this.isOnLadder() && this.getPossessingEntity() != null) {
            this.fallDistance = 0;
        }
        super.updateFallState(y, onGroundIn, state, pos);
    }

    @Override
    public void playLivingSound() {
        if (!Dissolution.config.cancelPossessingAmbientSounds) {
            super.playLivingSound();
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.getPossessingEntityId() != null)
            compound.setUniqueId("possessingEntity", this.getPossessingEntityId());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("possessingEntity")) {
            this.setPossessingEntity(compound.getUniqueId("possessingEntity"));
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        // Stolen from AbstractHorse#travel
        if (this.isBeingPossessed() && this.getPossessingEntity() != null) {
            EntityPlayer player = this.getPossessingEntity();
            assert player != null;
            this.rotationYaw = player.rotationYaw;
            this.prevRotationYaw = this.rotationYaw;
            this.rotationPitch = player.rotationPitch;
            this.setRotation(this.rotationYaw, this.rotationPitch);
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = this.renderYawOffset;
            this.fallDistance = player.fallDistance;
//            strafe = player.moveStrafing;
//            forward = player.moveForward;
//            vertical = player.moveVertical;

            super.travel(strafe, vertical, forward);
            this.setPosition(player.posX, player.posY, player.posZ);
//                this.setAIMoveSpeed((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
            this.limbSwing = player.limbSwing;
            this.limbSwingAmount = player.limbSwingAmount;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Nullable
    public Entity getControllingPassenger() {
        // Allows this entity to move client-side, in conjunction with #canBeSteered
        return this.getPossessingEntity();
    }

    @Override
    public boolean canBeSteered() {
        // Allows this entity to move client-side
        return this.getPossessingEntity() != null;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        // Prevent infinite propulsion through self collision
        if (!entityIn.getUniqueID().equals(getPossessingEntityId())) {
            super.applyEntityCollision(entityIn);
        }
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        // Prevent infinite propulsion through self collision
        if (!entityIn.getUniqueID().equals(getPossessingEntityId())) {
            super.collideWithEntity(entityIn);
        }
    }

    @Override
    public void onDeath(@Nonnull DamageSource cause) {
        super.onDeath(cause);
        if (this.getPossessingEntity() != null) {
            EntityPlayer player = this.getPossessingEntity();
            CapabilityIncorporealHandler.getHandler(player).setPossessed(null);
            if (!world.isRemote && !world.getGameRules().getBoolean("keepInventory")) {
                player.captureDrops = true;
                player.capturedDrops.clear();
                player.inventory.dropAllItems();
                player.captureDrops = false;
                ForgeEventFactory.onPlayerDrops(player, cause, player.capturedDrops, recentlyHit > 0);
            }
        }
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        // Player and possessed are in the same team because duh
        Entity possessing = this.getPossessingEntity();
        return entityIn.equals(possessing)
                || (possessing != null && possessing.isOnSameTeam(entityIn))
                || super.isOnSameTeam(entityIn);
    }

    /* * * * * * * * * * *
      Plenty of delegation
     * * * * * * * * * * * */

    @Override
    public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            possessing.knockBack(entityIn, strength, xRatio, zRatio);
            possessing.velocityChanged = true;
            return;
        }
        super.knockBack(entityIn, strength, xRatio, zRatio);
    }

    @Override
    public boolean isRiding() {
        return this.dummyRidingEntity != null || super.isRiding();
    }

    public void setDummyRidingEntity(@Nullable Entity dummyRidingEntity) {
        this.dummyRidingEntity = dummyRidingEntity;
        this.ridingEntityQueries = 2;
    }

    @Nullable
    @Override
    public Entity getRidingEntity() {
        // Stupid hack to render sat. Let's pray for no NullPointerException with modded renders
        // I am really sorry for any coder reading this.
        if (dummyRidingEntity != null) {
            Entity ret = this.dummyRidingEntity;
            if (--this.ridingEntityQueries <= 0) {
                this.dummyRidingEntity = null;
            }
            return ret;
        }
        return super.getRidingEntity();
    }

    @Override
    public boolean startRiding(Entity entityIn) {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            return possessing.startRiding(entityIn);
        }
        return super.startRiding(entityIn);
    }

    @Override
    public int getPortalCooldown() {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            return possessing.getPortalCooldown();
        }
        return super.getPortalCooldown();
    }

    @Override
    public boolean isActiveItemStackBlocking() {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            return possessing.isActiveItemStackBlocking();
        }
        return super.isActiveItemStackBlocking();
    }

    @Nonnull
    @Override
    public Iterable<ItemStack> getHeldEquipment() {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            return possessing.getHeldEquipment();
        }
        return super.getHeldEquipment();
    }

    @Nonnull
    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            return possessing.getItemStackFromSlot(slotIn);
        }
        return super.getItemStackFromSlot(slotIn);
    }

    @Nonnull
    @Override
    public ItemStack getActiveItemStack() {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            return possessing.getActiveItemStack();
        }
        return super.getActiveItemStack();
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, @Nonnull ItemStack stack) {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null && !world.isRemote) {
            possessing.setItemStackToSlot(slotIn, stack);
            return;
        }
        super.setItemStackToSlot(slotIn, stack);
    }

    @Override
    public boolean hasItemInSlot(@Nonnull EntityEquipmentSlot slot) {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            return possessing.hasItemInSlot(slot);
        }
        return super.hasItemInSlot(slot);
    }

    @Override
    public boolean isHandActive() {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            return possessing.isHandActive();
        }
        return super.isHandActive();
    }

    @Override
    protected void updateActiveHand() {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing == null) {
            super.updateActiveHand();
        }
    }

}
