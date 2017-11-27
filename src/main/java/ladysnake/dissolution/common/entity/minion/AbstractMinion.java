package ladysnake.dissolution.common.entity.minion;

import com.google.common.base.Optional;
import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.IPossessable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.entity.ai.EntityAIInert;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionRangedAttack;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector2f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"Guava", "WeakerAccess"})
public abstract class AbstractMinion extends EntityMob implements IRangedAttackMob, IEntityOwnable, IPossessable {

    protected static final float SIZE_X = 0.6F, SIZE_Y = 1.95F;

    private static final DataParameter<Boolean> INERT = EntityDataManager
            .createKey(AbstractMinion.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> LIFE_STONE = EntityDataManager
            .createKey(AbstractMinion.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager
            .createKey(AbstractMinion.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager
            .createKey(AbstractMinion.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Optional<UUID>> POSSESSING_ENTITY_ID = EntityDataManager
            .createKey(AbstractMinion.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static MethodHandle entityAINearestAttackableTarget$targetClass;
    private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, 1);

    private final EntityAIMinionRangedAttack aiArrowAttack = new EntityAIMinionRangedAttack(this, 1.0D, 20, 15.0F);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false);
    protected EntityAIInert aiDontDoShit;
    private List<Entity> triggeredMobs = new LinkedList<>();
    protected List<Class<? extends EntityLivingBase>> equivalents = new LinkedList<>();

    static {
        try {
            Field f = ReflectionHelper.findField(EntityAINearestAttackableTarget.class, "targetClass", "field_75307_b");
            entityAINearestAttackableTarget$targetClass = MethodHandles.lookup().unreflectGetter(f);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static IPossessable createMinion(EntityLivingBase deadGuy) {
        if (deadGuy instanceof IPossessable)
            return (IPossessable) deadGuy;
        if (!deadGuy.isEntityUndead())
            return null;

        AbstractMinion corpse = null;
        if (deadGuy instanceof EntityPigZombie) {
            corpse = new EntityMinionPigZombie(deadGuy.world, deadGuy.isChild());
        } else if (deadGuy instanceof EntityZombie) {
            corpse = new EntityMinionZombie(deadGuy.world, deadGuy instanceof EntityHusk, deadGuy.isChild());
        } else if (deadGuy instanceof EntitySkeleton) {
            corpse = new EntityMinionSkeleton(deadGuy.world);
        } else if (deadGuy instanceof EntityStray) {
            corpse = new EntityMinionStray(deadGuy.world);
        } else if (deadGuy instanceof EntityWitherSkeleton) {
            corpse = new EntityMinionWitherSkeleton(deadGuy.world);
        } else if (deadGuy.isNonBoss() && deadGuy instanceof EntityMob)
            corpse = new EntityGenericMinion(deadGuy.world, (EntityMob) deadGuy);

        if (corpse != null) {
            for (IAttributeInstance attribute : deadGuy.getAttributeMap().getAllAttributes()) {
                IAttributeInstance corpseAttribute = corpse.getAttributeMap().getAttributeInstance(attribute.getAttribute());
                //noinspection ConstantConditions
                if (corpseAttribute == null) {
                    corpseAttribute = corpse.getAttributeMap().registerAttribute(attribute.getAttribute());
                }
                corpseAttribute.setBaseValue(attribute.getBaseValue());
                for (AttributeModifier modifier : attribute.getModifiers()) {
                    corpseAttribute.removeModifier(modifier.getID());
                    corpseAttribute.applyModifier(modifier);
                }
            }
            for (PotionEffect potionEffect : deadGuy.getActivePotionEffects())
                corpse.addPotionEffect(new PotionEffect(potionEffect));
            corpse.setPositionAndRotation(deadGuy.posX, deadGuy.posY, deadGuy.posZ, deadGuy.rotationYaw, deadGuy.rotationPitch);
            corpse.onUpdate();
        }

        return corpse;
    }

    public AbstractMinion(World worldIn) {
        this(worldIn, false);
    }

    public AbstractMinion(World worldIn, boolean isChild) {
        super(worldIn);
        this.setSize(SIZE_X, SIZE_Y);
        this.setChild(isChild);
        Collections.addAll(this.equivalents, EntityPlayer.class, EntityPlayerMP.class);
    }

    @Override
    protected abstract void initEntityAI();

    protected void applyEntityAI() {
        this.tasks.addTask(99, aiDontDoShit = new EntityAIInert(false));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true) {
            @Override
            protected boolean isSuitableTarget(EntityLivingBase target, boolean includeInvincibles) {
                return super.isSuitableTarget(target, includeInvincibles) && target != getOwner();
            }
        });
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityMob.class, 10, true, false,
                e -> !DissolutionConfigManager.isEntityBlacklistedFromMinionAttacks(e)));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        this.isAIDisabled();
    }

    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(INERT, false);
        this.getDataManager().register(LIFE_STONE, (byte) 0b0);
        this.getDataManager().register(IS_CHILD, false);
        this.getDataManager().register(OWNER_UNIQUE_ID, Optional.absent());
        this.getDataManager().register(POSSESSING_ENTITY_ID, Optional.absent());
    }

    @Override
    public boolean isAIDisabled() {
        return isInert() || super.isAIDisabled();
    }

    @Override
    public boolean isPreventingPlayerRest(EntityPlayer playerIn) {
        return false;
    }

    protected void setCombatTask() {
        if (this.world != null && !this.world.isRemote) {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            ItemStack itemstack = this.getHeldItemMainhand();

            if (itemstack.getItem() == Items.BOW) {
                int i = 20;

                if (this.world.getDifficulty() != EnumDifficulty.HARD) {
                    i = 40;
                }

                this.aiArrowAttack.setAttackCooldown(i);
                this.tasks.addTask(4, this.aiArrowAttack);
            } else {
                this.tasks.addTask(4, this.aiAttackOnCollide);
            }
        }
    }

    @Override
    public void attackEntityWithRangedAttack(@Nonnull EntityLivingBase target, float distanceFactor) {
        EntityArrow entityarrow = this.getArrow(new EntityTippedArrow(this.world, this), distanceFactor);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double) (target.height / 3.0F) - entityarrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F,
                (float) (14 - this.world.getDifficulty().getDifficultyId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(entityarrow);
    }

    protected EntityArrow getArrow(EntityTippedArrow baseArrow, float distanceFactor) {
        ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);
        baseArrow.setEnchantmentEffectsFromEntity(this, distanceFactor);
        if (itemstack.getItem() == Items.TIPPED_ARROW)
            baseArrow.setPotionEffect(itemstack);
        return baseArrow;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.rand.nextFloat() < 0.01f)
            this.attractAttention();
        this.handleSunExposure();
    }

    @Override
    public boolean isEntityInvulnerable(@Nonnull DamageSource source) {
        return (source.getTrueSource() == null && this.isInert() && !source.canHarmInCreative()) || super.isEntityInvulnerable(source);
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

    protected void handleSunExposure() {
        if (this.world.isDaytime() && !this.world.isRemote
                && this.world.canSeeSky(new BlockPos(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ))
                && !this.world.isRaining()) {
            boolean flag = true;
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

            if (!itemstack.isEmpty()) {
                if (itemstack.isItemStackDamageable()) {
                    itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));

                    if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
                        this.renderBrokenItemStack(itemstack);
                        this.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }

                flag = false;
            }

            if (flag && !this.isBurning()) {
                this.setFire(1);
            }
        }
    }

    @Override
    public void stopActiveHand() {
        if (this.getActiveItemStack().getItem() instanceof ItemBow) {
            this.fireBow();
            this.resetActiveHand();
        } else
            super.stopActiveHand();
    }

    protected void fireBow() {
        if (!(this.getControllingPassenger() instanceof EntityPlayer)) return;
        EntityPlayer entityPlayer = (EntityPlayer) this.getControllingPassenger();
        ItemStack bow = this.getActiveItemStack();
        boolean infiniteAmmo = entityPlayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, bow) > 0;
        ItemStack ammoStack = DissolutionInventoryHelper.findItemInstance(entityPlayer, ItemArrow.class);
        int timeLeft = this.getItemInUseCount();
        World worldIn = this.world;

        int i = Items.BOW.getMaxItemUseDuration(bow) - timeLeft;
        i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(bow, worldIn, entityPlayer, i, !ammoStack.isEmpty() || infiniteAmmo);
        if (i < 0) return;

        if (!ammoStack.isEmpty() || infiniteAmmo) {
            if (ammoStack.isEmpty()) {
                ammoStack = new ItemStack(Items.ARROW);
            }

            float f = ItemBow.getArrowVelocity(i);

            if ((double) f >= 0.1D) {
                boolean flag1 = entityPlayer.capabilities.isCreativeMode || (ammoStack.getItem() instanceof ItemArrow && ((ItemArrow) ammoStack.getItem()).isInfinite(ammoStack, bow, entityPlayer));

                if (!worldIn.isRemote) {
                    ItemArrow itemarrow = (ItemArrow) (ammoStack.getItem() instanceof ItemArrow ? ammoStack.getItem() : Items.ARROW);
                    EntityArrow entityarrow = this.getArrow((EntityTippedArrow) itemarrow.createArrow(worldIn, ammoStack, this), 0);
                    entityarrow.shoot(entityPlayer, entityPlayer.rotationPitch, entityPlayer.rotationYaw, 0.0F, f * 3.0F, 1.0F);

                    if (f == 1.0F) {
                        entityarrow.setIsCritical(true);
                    }

                    bow.damageItem(1, entityPlayer);

                    if (flag1 || entityPlayer.capabilities.isCreativeMode && (ammoStack.getItem() == Items.SPECTRAL_ARROW || ammoStack.getItem() == Items.TIPPED_ARROW)) {
                        entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                    }

                    worldIn.spawnEntity(entityarrow);
                }

                worldIn.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SKELETON_SHOOT, this.getSoundCategory(), 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

                if (!flag1 && !entityPlayer.capabilities.isCreativeMode) {
                    ammoStack.shrink(1);

                    if (ammoStack.isEmpty()) {
                        entityPlayer.inventory.deleteStack(ammoStack);
                    }
                }

                entityPlayer.addStat(StatList.getObjectUseStats(bow.getItem()));
            }
        }
    }

    @Override
    protected void damageEntity(@Nonnull DamageSource damageSrc, float damageAmount) {
        super.damageEntity(damageSrc, damageAmount);
    }

    protected boolean isSuitableForInteraction(EntityPlayer player) {
        return !CapabilityIncorporealHandler.getHandler(player).getCorporealityStatus().isIncorporeal()
                && (this.isInert() || player == this.getOwner()) || player.isCreative();
    }

    @Override
    public boolean canBePossessedBy(EntityPlayer player) {
        return this.getControllingPassenger() == player || this.getControllingPassenger() == null;
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
    @SideOnly(Side.CLIENT)
    public void possessTickClient() {
        EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        Vector2f move = new Vector2f(playerSP.movementInput.moveStrafe, playerSP.movementInput.moveForward);
        move.scale((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
        playerSP.moveStrafing = move.x;
        playerSP.moveForward = move.y;
        this.setJumping(playerSP.movementInput.jump);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(passenger);
        if (handler != null) {
            if (!world.isRemote)
                ((EntityPlayerMP) passenger).connection.sendPacket(new SPacketCamera(passenger));
        }
        super.removePassenger(passenger);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
//        Entity entity = source.getTrueSource();
        return /*this.isBeingRidden() && entity != null && this.isRidingOrBeingRiddenBy(entity) ? false : */super.attackEntityFrom(source, amount);
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
        return this.getPassengers().stream().filter(e -> e.getUniqueID().equals(getPossessingEntity())).findAny().orElse(null);
    }

    @Override
    public boolean canBeSteered() {
        return this.getControllingPassenger() instanceof EntityLivingBase;
    }

    @Override
    public void updatePassenger(@Nonnull Entity passenger) {
        super.updatePassenger(passenger);
        if (passenger.getUniqueID().equals(this.getPossessingEntity())) {
            passenger.setPosition(this.posX, this.posY, this.posZ);
            if (passenger instanceof EntityPlayer) {
                for (PotionEffect potionEffect : ((EntityPlayer) passenger).getActivePotionMap().values())
                    this.addPotionEffect(new PotionEffect(potionEffect));
                ((EntityPlayer) passenger).clearActivePotions();
                for (PotionEffect potionEffect : this.getActivePotionMap().values())
                    ((EntityPlayer) passenger).addPotionEffect(new PotionEffect(potionEffect));
                for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
                    this.setItemStackToSlot(slot, ((EntityPlayer) passenger).getItemStackFromSlot(slot));
            }
        }
    }

    @Override
    public void onDeath(@Nonnull DamageSource cause) {
        if (this.getControllingPassenger() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) this.getControllingPassenger();
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
                player.setItemStackToSlot(slot, ItemStack.EMPTY);
            if (!world.isRemote)
                player.inventory.dropAllItems();
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

    /**
     * Applies the given player interaction to this Entity.
     */
    @Nonnull
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (isSuitableForInteraction(player) && itemstack.getItem() != Items.NAME_TAG && itemstack.getItem() != ModItems.EYE_OF_THE_UNDEAD) {
            if (!this.world.isRemote && !player.isSpectator()) {
                EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);

                if (itemstack.isEmpty()) {
                    EntityEquipmentSlot entityEquipmentSlot2 = this.getClickedSlot(vec);

                    if (this.hasItemInSlot(entityEquipmentSlot2)) {
                        this.swapItem(player, entityEquipmentSlot2, itemstack, hand);
                    } else {
                        return EnumActionResult.PASS;
                    }
                } else {

                    this.swapItem(player, entityequipmentslot, itemstack, hand);
                }
                if (entityequipmentslot == EntityEquipmentSlot.MAINHAND)
                    this.setCombatTask();

                return EnumActionResult.SUCCESS;
            } else {
                return itemstack.isEmpty() && !this.hasItemInSlot(this.getClickedSlot(vec)) ? EnumActionResult.PASS
                        : EnumActionResult.SUCCESS;
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    /**
     * Vanilla code from the armor stand
     *
     * @param raytrace the look vector of the player
     * @return the targeted equipment slot
     */
    protected EntityEquipmentSlot getClickedSlot(Vec3d raytrace) {
        EntityEquipmentSlot entityEquipmentSlot = EntityEquipmentSlot.MAINHAND;
        boolean flag = this.isChild();
        double d0 = (this.isInert() ? raytrace.z + 1.2 : raytrace.y) * (flag ? 2.0D : 1.0D);
        EntityEquipmentSlot entityEquipmentSlot1 = EntityEquipmentSlot.FEET;

        if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasItemInSlot(entityEquipmentSlot1)) {
            entityEquipmentSlot = EntityEquipmentSlot.FEET;
        } else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D)
                && this.hasItemInSlot(EntityEquipmentSlot.CHEST)) {
            entityEquipmentSlot = EntityEquipmentSlot.CHEST;
        } else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasItemInSlot(EntityEquipmentSlot.LEGS)) {
            entityEquipmentSlot = EntityEquipmentSlot.LEGS;
        } else if (d0 >= 1.6D && this.hasItemInSlot(EntityEquipmentSlot.HEAD)) {
            entityEquipmentSlot = EntityEquipmentSlot.HEAD;
        }

        return entityEquipmentSlot;
    }

    protected void swapItem(EntityPlayer player, EntityEquipmentSlot targetedSlot, ItemStack playerItemStack,
                            EnumHand hand) {
        ItemStack itemstack = this.getItemStackFromSlot(targetedSlot);
        if (player.capabilities.isCreativeMode && itemstack.isEmpty() && !playerItemStack.isEmpty()) {
            ItemStack itemstack2 = playerItemStack.copy();
            itemstack2.setCount(1);
            this.setItemStackToSlot(targetedSlot, itemstack2);
        } else if (!playerItemStack.isEmpty() && playerItemStack.getCount() > 1) {
            if (itemstack.isEmpty()) {
                ItemStack itemstack1 = playerItemStack.copy();
                itemstack1.setCount(1);
                this.setItemStackToSlot(targetedSlot, itemstack1);
                playerItemStack.shrink(1);
            }
        } else {
            this.setItemStackToSlot(targetedSlot, playerItemStack);
            player.setHeldItem(hand, itemstack);
        }
    }

    public void setChild(boolean childMinion) {
        this.getDataManager().set(IS_CHILD, childMinion);
        if (this.world != null && !this.world.isRemote) {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            iattributeinstance.removeModifier(BABY_SPEED_BOOST);

            if (childMinion) {
                iattributeinstance.applyModifier(BABY_SPEED_BOOST);
            }
        }
        this.setChildSize(childMinion);
    }

    @Override
    public boolean isChild() {
        return this.getDataManager().get(IS_CHILD);
    }

    protected void setChildSize(boolean isChild) {
        float ratio = (isChild ? 0.5F : 1.0F);
        if (isInert())
            super.setSize(SIZE_Y * ratio, SIZE_X * ratio);
        else
            super.setSize(SIZE_X * ratio, SIZE_Y * ratio);
    }

    public void notifyDataManagerChange(@Nonnull DataParameter<?> key) {
        if (IS_CHILD.equals(key) || INERT.equals(key)) {
            this.setChildSize(this.isChild());
        }
        super.notifyDataManagerChange(key);
    }

    @Nonnull
    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }

    /**
     * @param gem 0 -> no gem
     *            Most significant bit indicates used gem
     */
    public void setLifeStone(int gem) {
        this.getDataManager().set(LIFE_STONE, (byte) gem);
    }

    public byte getLifeStone() {
        return this.getDataManager().get(LIFE_STONE);
    }

    public boolean hasLifeStone() {
        return getLifeStone() != 0;
    }

    public void setInert(boolean isCorpse) {
        this.getDataManager().set(INERT, isCorpse);

        if (isCorpse)
            //noinspection SuspiciousNameCombination
            this.setSize(SIZE_Y, SIZE_X);
        else
            this.setSize(SIZE_X, SIZE_Y);
    }

    /**
     * @return Whether this minion is lying on the ground
     */
    public boolean isInert() {
        return this.getDataManager().get(INERT);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("inert", this.isInert());
        if (this.isChild())
            compound.setBoolean("isBaby", true);
        compound.setByte("stoneHeart", this.getLifeStone());
        if (this.getPossessingEntity() != null)
            compound.setUniqueId("possessingEntity", this.getPossessingEntity());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.setInert(compound.getBoolean("inert"));
        this.setChild(compound.getBoolean("isBaby"));
        this.setLifeStone(compound.getByte("stoneHeart"));
        this.setPossessingEntity(compound.getUniqueId("possessingEntity"));
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
            ItemStack itemstack = this.getItemStackFromSlot(entityequipmentslot);
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
                if (itemstack.isItemStackDamageable()) {
                    itemstack.setItemDamage(this.rand.nextInt(Math.min(itemstack.getMaxDamage() / 10, 50)));
                }
                this.entityDropItem(itemstack, this.getEyeHeight());
            }
        }
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

    @Nullable
    @Override
    public UUID getOwnerId() {
        return this.getDataManager().get(OWNER_UNIQUE_ID).orNull();
    }

    public void setOwnerId(@Nullable UUID uuid) {
        this.getDataManager().set(OWNER_UNIQUE_ID, Optional.fromNullable(uuid));
    }

    @Nullable
    @Override
    public EntityLivingBase getOwner() {
        UUID uuid = this.getOwnerId();
        return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
    }
}
