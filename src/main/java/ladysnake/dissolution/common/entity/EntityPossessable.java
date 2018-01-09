package ladysnake.dissolution.common.entity;

import com.google.common.base.Optional;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.ai.EntityAIInert;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.registries.CorporealityStatus;
import ladysnake.dissolution.common.registries.SoulCorporealityStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector2f;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class EntityPossessable extends EntityMob implements IPossessable {
    protected static final DataParameter<Optional<UUID>> POSSESSING_ENTITY_ID =
            EntityDataManager.createKey(EntityPossessable.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    protected static final DataParameter<Integer> PURIFIED_HEALTH =
            EntityDataManager.createKey(EntityPossessable.class, DataSerializers.VARINT);
    private static MethodHandle entityAINearestAttackableTarget$targetClass;

    protected List<Entity> triggeredMobs = new LinkedList<>();
    protected List<Class<? extends EntityLivingBase>> equivalents = new LinkedList<>();
    protected EntityAIInert aiDontDoShit;

    static {
        try {
            Field f = ReflectionHelper.findField(EntityAINearestAttackableTarget.class, "targetClass", "field_75307_b");
            entityAINearestAttackableTarget$targetClass = MethodHandles.lookup().unreflectGetter(f);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public EntityPossessable(World worldIn) {
        super(worldIn);
        Collections.addAll(this.equivalents, EntityPlayer.class, EntityPlayerMP.class);
    }

    protected void applyEntityAI() {
        this.tasks.addTask(99, aiDontDoShit = new EntityAIInert(false));
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
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
            if (player.getItemStackFromSlot(slot).isEmpty())
                player.setItemStackToSlot(slot, this.getItemStackFromSlot(slot));
            else player.addItemStackToInventory(this.getItemStackFromSlot(slot));
        player.startRiding(this);
        return true;
    }

    @Override
    public boolean onPossessionStop(EntityPlayer player, boolean force) {
        if (!player.getUniqueID().equals(this.getPossessingEntity()))
            return true;
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
        if (!handler.getCorporealityStatus().isIncorporeal() || this.isDead || force) {
            this.setPossessingEntity(null);
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
    public boolean proxyRangedAttack(int charge) {
        return true;
    }

    @Override
    public int getPurifiedHealth() {
        return getDataManager().get(PURIFIED_HEALTH);
    }

    @Override
    public void purifyHealth(int purified) {
        this.setPurifiedHealth(getPurifiedHealth()+1);
    }

    @Override
    public void setPurifiedHealth(int health) {
        if(health >= this.getHealth() && !this.world.isRemote) {
            Entity passenger = this.getControllingPassenger();
            if (passenger instanceof EntityPlayer && passenger.getUniqueID().equals(getPossessingEntity())) {
                EntityPlayer soul = (EntityPlayer) passenger;
                CapabilityIncorporealHandler.getHandler(soul).setCorporealityStatus(CorporealityStatus.BODY);
                this.world.removeEntity(this);
            }
        } else this.getDataManager().set(PURIFIED_HEALTH, health);
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
    public UUID getPossessingEntity() {
        return this.getDataManager().get(POSSESSING_ENTITY_ID).orNull();
    }

    public void setPossessingEntity(@Nullable UUID possessingEntity) {
        if (!world.isRemote)
            this.aiDontDoShit.setShouldExecute(possessingEntity != null);
        this.getDataManager().set(POSSESSING_ENTITY_ID, Optional.fromNullable(possessingEntity));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(POSSESSING_ENTITY_ID, Optional.absent());
        this.getDataManager().register(PURIFIED_HEALTH, 0);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.rand.nextFloat() < 0.01f)
            this.attractAttention();
    }

    protected void attractAttention() {
        List<EntityCreature> nearby = this.world.getEntitiesWithinAABB(EntityCreature.class,
                new AxisAlignedBB(new BlockPos(this)).grow(30), this::isMobEligibleForAttention);
        Collections.shuffle(nearby);
        int max = Math.min(rand.nextInt() % 5, nearby.size());
        for (int i = 0; i < max; i++) {
            for (EntityAITasks.EntityAITaskEntry taskEntry : nearby.get(i).targetTasks.taskEntries) {
                if (shouldBeTargetedBy(nearby.get(i), taskEntry)) {
                    nearby.get(i).targetTasks.addTask(taskEntry.priority - 1,
                            new EntityAINearestAttackableTarget<>(nearby.get(i), this.getClass(), true));
                    break;
                }
            }
        }
    }

    protected boolean isMobEligibleForAttention(EntityCreature other) {
        return !this.triggeredMobs.contains(other) && (!other.isEntityUndead() || !other.isNonBoss());
    }

    protected boolean shouldBeTargetedBy(EntityCreature other, EntityAITasks.EntityAITaskEntry taskEntry) {
        if (taskEntry.action instanceof EntityAINearestAttackableTarget) {
            try {
                Class<?> clazz = (Class<?>) entityAINearestAttackableTarget$targetClass.invoke(taskEntry.action);
                return this.equivalents.contains(clazz);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("purifiedHealth", getPurifiedHealth());
        if (this.getPossessingEntity() != null)
            compound.setUniqueId("possessingEntity", this.getPossessingEntity());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.setPurifiedHealth(compound.getInteger("purifiedHealth"));
        this.setPossessingEntity(compound.getUniqueId("possessingEntity"));
    }
}
