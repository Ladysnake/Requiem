package ladysnake.dissolution.common.entity;

import com.google.common.base.Optional;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Ref;
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
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.Mod;
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
@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public class EntityPossessableImpl extends EntityMob implements IPossessable {
    private static final DataParameter<Optional<UUID>> POSSESSING_ENTITY_ID =
            EntityDataManager.createKey(EntityPossessableImpl.class, DataSerializers.OPTIONAL_UNIQUE_ID);

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
        if (this.getPossessingEntity() == player)
            return true;
        if (this.getPossessingEntity() != null) {
            Dissolution.LOGGER.warn("A player attempted to possess an entity that was already possessed");
            return false;
        }
        this.setPossessingEntity(player.getUniqueID());
        player.capabilities.isFlying = false;
        player.capabilities.allowFlying = false;
        player.eyeHeight = this.getEyeHeight();
        player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.getAIMoveSpeed());
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
//            if (!world.isRemote) {
//                ((EntityPlayerMP) player).connection.sendPacket(new SPacketCamera(player));
//            }
            this.aiDontDoShit.setShouldExecute(false);
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void possessTickClient() {
        EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        Vector2f move = new Vector2f(playerSP.movementInput.moveStrafe, playerSP.movementInput.moveForward);
//        move.scale((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
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
    public boolean canBePossessedBy(EntityPlayer player) {
        EntityPlayer possessingEntity = this.getPossessingEntity();
        return possessingEntity == null || possessingEntity == player;
    }

    private boolean isBeingPossessed() {
        return this.getPossessingEntityId() != null;
    }

    @Override
    public void updatePossessing() {
        EntityPlayer possessing = getPossessingEntity();
        if (possessing != null) {
            this.setPosition(possessing.posX, possessing.posY, possessing.posZ);
//            possessing.setPosition(this.posX, this.posY, this.posZ);
            //                for (PotionEffect potionEffect : ((EntityPlayer) possessing).getActivePotionMap().values())
//                    this.addPotionEffect(new PotionEffect(potionEffect));
            possessing.clearActivePotions();
            for (PotionEffect potionEffect : this.getActivePotionMap().values()) {
                possessing.addPotionEffect(new PotionEffect(potionEffect));
            }
        }
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
    public Entity getLowestRidingEntity() {
        /*
        Prevents the player from being targeted in EntityRenderer#getMouseOver
        Specifically passes this check:
        if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity() && !entity1.canRiderInteract())
        */
        if (this.isBeingPossessed()) {
            return this.getPossessingEntity();
        }
        return super.getLowestRidingEntity();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(POSSESSING_ENTITY_ID, Optional.absent());
    }

    @Override
    protected void onInsideBlock(IBlockState block) {
        super.onInsideBlock(block);
        // Allows possessed entities to use portals
        // TODO check if this is useful without riding
        if (block.getBlock() == Blocks.PORTAL && !this.isRiding() && this.isBeingRidden() && this.getPossessingEntity() != null) {
            this.setPortal(new BlockPos(this));
        }
    }

    @Override
    public void setPortal(@Nonnull BlockPos pos) {
        super.setPortal(pos);
        // Allows possessed entities to use portals
        // TODO check if this is useful without riding
        EntityPlayer passenger = getPossessingEntity();
        if (passenger != null) {
            passenger.setPortal(pos);
        }
    }

    @Nullable
    @Override
    public Entity changeDimension(int dimensionIn, @Nonnull ITeleporter teleporter) {
        // Teleports the player along with the entity
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
    protected void updateFallState(double y, boolean onGroundIn, @Nonnull IBlockState state, @Nonnull BlockPos pos) {
        // Prevent fall damage on ladders
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
//            strafe = player.moveStrafing;
//            forward = player.moveForward;

            if (player.isUser()) {
                this.setPosition(player.posX, player.posY, player.posZ);
//                this.setAIMoveSpeed((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
//                super.travel(strafe, vertical, forward);
            }
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
            if (!world.isRemote) {
                player.inventory.dropAllItems();
                // Hardcore players die for good when their body is killed
                if (world.getMinecraftServer().isHardcore()) {
                    player.setHealth(0f);
                    player.onDeath(cause);
                    CapabilityIncorporealHandler.getHandler(player).setStrongSoul(false);
                }
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
