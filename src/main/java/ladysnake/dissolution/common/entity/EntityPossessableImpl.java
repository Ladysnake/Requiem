package ladysnake.dissolution.common.entity;

import com.google.common.base.Optional;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.ai.EntityAIInert;
import ladysnake.dissolution.unused.common.blocks.BlockSepulchre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector2f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The template class for possessable entities. <br>
 * Used in {@link PossessableEntityFactory} to generate possessable versions of any mob. <br>
 * Note: do <b>NOT</b> check whether entities are instances of this class, it will always return false.
 */
public class EntityPossessableImpl extends EntityMob implements IPossessable {
    private static final DataParameter<Optional<UUID>> POSSESSING_ENTITY_ID =
            EntityDataManager.createKey(EntityPossessableImpl.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> PURIFIED_HEALTH =
            EntityDataManager.createKey(EntityPossessableImpl.class, DataSerializers.VARINT);

    private EntityAIInert aiDontDoShit = new EntityAIInert(false);

    private boolean sleeping;

    public EntityPossessableImpl(World worldIn) {
        super(worldIn);
        if (worldIn != null && !worldIn.isRemote) {
            this.tasks.addTask(99, aiDontDoShit);
        }
    }

    @Override
    public boolean onEntityPossessed(EntityPlayer player) {
        if (this.getControllingPassenger() == player)
            return true;
        if (this.getControllingPassenger() != null) {
            Dissolution.LOGGER.warn("A player attempted to possess an entity that was already possessed");
            return false;
        }
        this.setPossessingEntity(player.getUniqueID());
        player.startRiding(this);
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
        if (!handler.getCorporealityStatus().isIncorporeal() || this.isDead || force) {
            this.setPossessingEntity(null);
            player.eyeHeight = player.getDefaultEyeHeight();
            this.aiDontDoShit.setShouldExecute(false);
            return true;
        }
        return false;
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
        if (possessing != null && possessing.isCreative()) {
            return true;
        }
        return super.isEntityInvulnerable(source);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void possessTickClient() {
        EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        Vector2f move = new Vector2f(playerSP.movementInput.moveStrafe, playerSP.movementInput.moveForward);
        move.scale((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
        playerSP.moveStrafing = move.x;
        playerSP.moveForward = move.y;
        this.setJumping(playerSP.movementInput.jump);
    }

    @Nullable
    public UUID getPossessingEntityId() {
        return this.getDataManager().get(POSSESSING_ENTITY_ID).orNull();
    }

    @Nullable
    private EntityPlayer getPossessingEntity() {
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
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(POSSESSING_ENTITY_ID, Optional.absent());
        this.getDataManager().register(PURIFIED_HEALTH, 0);
    }

    @Override
    protected void onInsideBlock(IBlockState block) {
        super.onInsideBlock(block);
        if (block.getBlock() == Blocks.PORTAL && !this.isRiding() && this.isBeingRidden() && this.getPossessingEntity() != null) {
            this.setPortal(new BlockPos(this));
        }
    }

    @Override
    public void setPortal(@Nonnull BlockPos pos) {
        super.setPortal(pos);
        EntityPlayer passenger = getPossessingEntity();
        if (passenger != null) {
            passenger.setPortal(pos);
        }
    }

    @Nullable
    @Override
    public Entity changeDimension(int dimensionIn, @Nonnull ITeleporter teleporter) {
        EntityPlayer passenger = this.getPossessingEntity();
        if (passenger != null && !world.isRemote) {
            CapabilityIncorporealHandler.getHandler(passenger).setPossessed(null, true);
            passenger.setPosition(posX, posY, posZ);
            passenger.changeDimension(dimensionIn, teleporter);
        }
        Entity clone = super.changeDimension(dimensionIn, teleporter);
        if (passenger != null && clone != null && !world.isRemote) {
            CapabilityIncorporealHandler.getHandler(passenger).setPossessed((EntityLivingBase & IPossessable) clone, true);
        }
        return clone;
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.sleeping = sleeping;
        BlockSepulchre.updateAllZombiesSleepingFlag();
    }

    @Override
    public boolean isPlayerSleeping() {
        return sleeping;
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

    @Override
    protected void updateFallState(double y, boolean onGroundIn, @Nonnull IBlockState state, @Nonnull BlockPos pos) {
        if (this.isOnLadder() && this.getPossessingEntity() != null) {
            this.fallDistance = 0;
        }
        super.updateFallState(y, onGroundIn, state, pos);
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
        this.setPossessingEntity(compound.getUniqueId("possessingEntity"));
    }

    @Override
    public boolean canBePossessedBy(EntityPlayer player) {
        return this.getControllingPassenger() == player || this.getControllingPassenger() == null;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        CapabilityIncorporealHandler.getHandler(passenger).ifPresent(handler -> {
            if (!world.isRemote) {
                ((EntityPlayerMP) passenger).connection.sendPacket(new SPacketCamera(passenger));
            }
        });
        super.removePassenger(passenger);
    }
    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isBeingRidden() && this.canBeSteered()) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) this.getControllingPassenger();
            assert entityLivingBase != null;
            this.rotationYaw = entityLivingBase.rotationYaw;
            this.prevRotationYaw = this.rotationYaw;
            this.rotationPitch = entityLivingBase.rotationPitch;
            this.setRotation(this.rotationYaw, this.rotationPitch);
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = this.renderYawOffset;
            strafe = entityLivingBase.moveStrafing;
            forward = entityLivingBase.moveForward;

            if (this.canPassengerSteer()) {
                this.setAIMoveSpeed((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                super.travel(strafe, vertical, forward);
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Nullable
    public Entity getControllingPassenger() {
        return this.getPassengers().stream().filter(e -> e.getUniqueID().equals(getPossessingEntityId())).findAny().orElse(null);
    }

    @Override
    public boolean canBeSteered() {
        return this.getControllingPassenger() instanceof EntityLivingBase;
    }

    @Override
    public void updatePassenger(@Nonnull Entity passenger) {
        super.updatePassenger(passenger);
        if (passenger.getUniqueID().equals(this.getPossessingEntityId())) {
            passenger.setPosition(this.posX, this.posY, this.posZ);
            if (passenger instanceof EntityPlayer) {
//                for (PotionEffect potionEffect : ((EntityPlayer) passenger).getActivePotionMap().values())
//                    this.addPotionEffect(new PotionEffect(potionEffect));
                ((EntityPlayer) passenger).clearActivePotions();
                for (PotionEffect potionEffect : this.getActivePotionMap().values()) {
                    ((EntityPlayer) passenger).addPotionEffect(new PotionEffect(potionEffect));
                }
            }
        }
    }

    @Override
    public void onDeath(@Nonnull DamageSource cause) {
        if (this.getPossessingEntity() != null) {
            EntityPlayer player = this.getPossessingEntity();
            if (!world.isRemote) {
                player.inventory.dropAllItems();
            }
            CapabilityIncorporealHandler.getHandler(player).setPossessed(null);
        }
        super.onDeath(cause);
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        return entityIn.equals(this.getControllingPassenger())
                || (this.getControllingPassenger() != null && this.getControllingPassenger().isOnSameTeam(entityIn))
                || super.isOnSameTeam(entityIn);
    }

}
