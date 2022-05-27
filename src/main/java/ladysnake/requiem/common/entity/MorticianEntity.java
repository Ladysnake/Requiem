/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.entity;

import com.google.common.base.Preconditions;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.util.RequiemTargetPredicate;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import ladysnake.requiem.common.entity.ai.FreeFromMortailCoilGoal;
import ladysnake.requiem.common.entity.ai.MorticianLookAtTargetGoal;
import ladysnake.requiem.common.entity.ai.MoveBackToObeliskGoal;
import ladysnake.requiem.common.entity.ai.StealSoulGoal;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.item.FilledSoulVesselItem;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.util.ObeliskDescriptor;
import ladysnake.requiem.core.record.EntityPositionClerk;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.StopAndLookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MorticianEntity extends MerchantEntity implements Angerable {
    public static final int MAX_LINK_DISTANCE = 20;
    public static final TradeOffers.Factory[] TRADES = new TradeOffers.Factory[]{
        (entity, random) -> new RemnantTradeOffer(
            new TradeOffer(new ItemStack(Items.GOLD_INGOT, 32), new ItemStack(Items.NETHERITE_INGOT), new ItemStack(RequiemItems.SEALED_REMNANT_VESSEL), 1, 1, 0.05F),
            new TradeOffer(new ItemStack(RequiemItems.EMPTY_SOUL_VESSEL), new ItemStack(Items.NETHERITE_INGOT, 1), new ItemStack(RequiemItems.SEALED_REMNANT_VESSEL), 1, 1, 0.05F), true),
        (entity, random) -> new TradeOffer(new ItemStack(RequiemItems.SHATTERED_SOUL_VESSEL), new ItemStack(Items.GOLD_INGOT), new ItemStack(RequiemItems.EMPTY_SOUL_VESSEL), 10, 1, 0.05F),
        (entity, random) -> new TradeOffer(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(RequiemEnchantments.HUMANITY, 1)), new ItemStack(Items.GOLD_INGOT, 20), EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(RequiemEnchantments.HUMANITY, 2)), 5, 1, 0.05F),
        (entity, random) -> new TradeOffer(FilledSoulVesselItem.forEntityType(entity.getWorld().getDimension().isUltrawarm() ? EntityType.PIGLIN : EntityType.VILLAGER), new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(RequiemItems.ICHOR_VESSEL_EMANCIPATION), 10, 1, 0.05F),
        (entity, random) -> new TradeOffer(FilledSoulVesselItem.forEntityType(EntityType.AXOLOTL), new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(RequiemItems.ICHOR_VESSEL_RECLAMATION), 10, 1, 0.05F),
        (entity, random) -> new TradeOffer(FilledSoulVesselItem.forEntityType(EntityType.GHAST), new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(RequiemItems.ICHOR_VESSEL_ATTRITION), 10, 1, 0.05F),
        (entity, random) -> new TradeOffer(FilledSoulVesselItem.forEntityType(EntityType.PILLAGER), new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(RequiemItems.ICHOR_VESSEL_PENANCE), 10, 1, 0.05F)
    };
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    public static final TrackedData<Boolean> OBELISK_PROJECTION = DataTracker.registerData(MorticianEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Integer> FADING_TICKS = DataTracker.registerData(MorticianEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Byte> SPELL = DataTracker.registerData(MorticianEntity.class, TrackedDataHandlerRegistry.BYTE);
    public static final int DESPAWN_DELAY = 20 * 30;

    public static DefaultAttributeContainer.Builder createMorticianAttributes() {
        return MobEntity.createMobAttributes().add(RequiemEntityAttributes.SOUL_OFFENSE, 30);
    }

    private @Nullable UUID linkedObelisk;
    private int angerTime;
    private @Nullable UUID angryAt;
    private RevengeGoal revengeGoal;
    private final List<UUID> capturedSouls = new ArrayList<>();

    public MorticianEntity(EntityType<? extends MorticianEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(OBELISK_PROJECTION, false);
        this.getDataTracker().startTracking(FADING_TICKS, 0);
        this.getDataTracker().startTracking(SPELL, (byte) 0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(1, new MorticianLookAtTargetGoal(this));
        this.goalSelector.add(2, new LookAtCustomerGoal(this));
        this.goalSelector.add(3, new StealSoulGoal(this));
        this.goalSelector.add(4, new FreeFromMortailCoilGoal(this));
        this.goalSelector.add(5, new GoToWalkTargetGoal(this, 0.35D));
        this.goalSelector.add(6, new MoveBackToObeliskGoal(this, 0.35D, false));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.35D));
        this.goalSelector.add(9, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F) {
            {
                RequiemTargetPredicate.includeIncorporeal(this.targetPredicate);
            }
        });
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F) {
            {
                RequiemTargetPredicate.includeIncorporeal(this.targetPredicate);
            }
        });
        this.revengeGoal = new RevengeGoal(this);
        this.targetSelector.add(1, revengeGoal);
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
    }

    public void addCapturedSoul(UUID recordId) {
        this.capturedSouls.add(recordId);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.world.isClient && this.isSpellcasting()) {
            int color = RequiemStatusEffects.PENANCE.getColor();
            double r = (float) (color >> 16 & 0xFF) / 255.0F;
            double g = (float) (color >> 8 & 0xFF) / 255.0F;
            double b = (float) (color & 0xFF) / 255.0F;
            float theta = this.bodyYaw * (float) (Math.PI / 180.0) + MathHelper.cos((float) this.age * 0.6662F) * 0.25F;
            float x = MathHelper.cos(theta);
            float z = MathHelper.sin(theta);
            this.world.addParticle(RequiemParticleTypes.PENANCE, this.getX() + (double) x * 0.6, this.getY() + this.getHeight() * 0.92, this.getZ() + (double) z * 0.6, 1, 1, 1);
            this.world.addParticle(RequiemParticleTypes.PENANCE, this.getX() - (double) x * 0.6, this.getY() + this.getHeight() * 0.92, this.getZ() - (double) z * 0.6, 1, 1, 1);
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Possessable possessable && possessable.isBeingPossessed()) {
            super.setTarget(possessable.getPossessor());
        } else {
            super.setTarget(target);
        }
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (this.world.getDifficulty() != Difficulty.PEACEFUL
            && target instanceof ServerPlayerEntity player
            && player.isPartOfGame()
            && player.interactionManager.isSurvivalLike()) {
            return true;
        }
        return super.canTarget(target);
    }

    @Override
    public boolean shouldDisplaySoulSpeedEffects() {
        return super.shouldDisplaySoulSpeedEffects()
            || this.age % 5 == 0 && this.getVelocity().x != 0.0D && this.getVelocity().z != 0.0D && this.isOnSoulSpeedBlock();
    }

    @Override
    protected float getVelocityMultiplier() {
        return this.isOnSoulSpeedBlock() ? 1.2F : super.getVelocityMultiplier();
    }

    @Override
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS) ? 4 : super.getPathfindingFavor(pos, world);
    }

    @Override
    public int getAngerTime() {
        return angerTime;
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angerTime = angerTime;
    }

    @Override
    public @Nullable UUID getAngryAt() {
        return angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    public void setSpellcasting(boolean spellcasting) {
        this.getDataTracker().set(SPELL, (byte) (spellcasting ? 1 : 0));
    }

    public boolean isSpellcasting() {
        return this.getDataTracker().get(SPELL) != 0;
    }

    private void setFadingTicks(int fadingTicks) {
        this.getDataTracker().set(FADING_TICKS, fadingTicks);
    }

    private int getFadingTicks() {
        return this.getDataTracker().get(FADING_TICKS);
    }

    public float getFadingAmount() {
        return (float) this.getFadingTicks() / DESPAWN_DELAY;
    }

    private void setObeliskProjection(boolean projection) {
        this.getDataTracker().set(OBELISK_PROJECTION, projection);
    }

    public boolean isObeliskProjection() {
        return this.getDataTracker().get(OBELISK_PROJECTION);
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.world instanceof ServerWorld sw && this.hasInvalidLinkedObelisk()) {
            // Attempt to link to a new obelisk, otherwise despawn
            MorticianSpawner.streamSpawnableObelisks(sw.getServer())
                .filter(r -> r.get(RequiemRecordTypes.OBELISK_REF)
                    .filter(obelisk -> obelisk.dimension() == this.world.getRegistryKey())
                    .filter(obelisk -> obelisk.pos().isWithinDistance(this.getBlockPos(), MAX_LINK_DISTANCE))
                    .isPresent())
                .min(Comparator.comparing(r -> r.get(RequiemRecordTypes.OBELISK_REF).orElseThrow().pos().getSquaredDistance(this.getBlockPos())))
                .ifPresentOrElse(
                    r -> {
                        this.setFadingTicks(0);
                        this.linkWith(r);
                    },
                    () -> {
                        this.setFadingTicks(this.getFadingTicks() + 1);
                        if (this.getFadingTicks() <= 0) {
                            world.sendEntityStatus(this, EntityStatuses.ADD_PORTAL_PARTICLES);
                            this.discard();
                        }
                    }
                );

            if (!this.isRemoved()) {
                this.tickAngerLogic((ServerWorld) this.world, true);
            }
        }
    }

    @Override
    public void stopAnger() {
        Angerable.super.stopAnger();
        this.revengeGoal.stop();
    }

    @Override
    public void forgive(PlayerEntity player) {
        // No, I don't think so
    }

    private boolean hasInvalidLinkedObelisk() {
        return this.linkedObelisk != null && GlobalRecordKeeper.get(this.world).getRecord(this.linkedObelisk).flatMap(r -> r.get(RequiemRecordTypes.RIFT_OBELISK)).isEmpty();
    }

    @Override
    public ActionResult interactMob(PlayerEntity customer, Hand hand) {
        ItemStack itemStack = customer.getStackInHand(hand);
        if (!itemStack.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) {
                customer.incrementStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.world.isClient && !this.getOffers().isEmpty()) {
                this.prepareOffersFor(customer);
                this.setCustomer(customer);
                this.sendOffers(customer, this.getDisplayName(), 1);
            }

            return ActionResult.success(this.world.isClient);
        } else {
            return super.interactMob(customer, hand);
        }
    }

    private void prepareOffersFor(PlayerEntity customer) {
        for (TradeOffer offer : this.getOffers()) {
            if (offer instanceof RemnantTradeOffer demonTradeOffer) {
                demonTradeOffer.setRemnant(RemnantComponent.get(customer).getRemnantType().isDemon());
            }
        }
    }

    public void linkWith(GlobalRecord r) {
        Optional<ObeliskDescriptor> obelisk = r.get(RequiemRecordTypes.OBELISK_REF);
        Preconditions.checkState(obelisk.isPresent());
        EntityPositionClerk.get(this).linkWith(r, RequiemRecordTypes.MORTICIAN_REF);
        this.linkedObelisk = r.getUuid();
        this.setObeliskProjection(true);
    }

    @Override
    protected void fillRecipes() {
        this.fillRecipesFromPool(this.getOffers(), TRADES, 7);
    }

    public @Nullable ObeliskDescriptor getHome() {
        if (this.linkedObelisk == null) return null;
        return GlobalRecordKeeper.get(this.world).getRecord(this.linkedObelisk).flatMap(r -> r.get(RequiemRecordTypes.OBELISK_REF)).orElse(null);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.containsUuid("linked_obelisk")) {
            GlobalRecordKeeper.get(this.world).getRecord(nbt.getUuid("linked_obelisk")).ifPresent(this::linkWith);
            this.setObeliskProjection(true);
        } else {
            this.setObeliskProjection(false);
        }

        if (nbt.contains("fading_ticks")) {
            this.setFadingTicks(nbt.getInt("fading_ticks"));
        }

        if (nbt.contains("captured_souls", NbtElement.LIST_TYPE)) {
            this.capturedSouls.clear();
            for (NbtElement capturedSoul : nbt.getList("captured_souls", NbtElement.INT_ARRAY_TYPE)) {
                this.capturedSouls.add(NbtHelper.toUuid(capturedSoul));
            }
        }

        this.readAngerFromNbt(this.world, nbt);

        this.setBreedingAge(Math.max(0, this.getBreedingAge()));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.linkedObelisk != null) {
            nbt.putUuid("linked_obelisk", this.linkedObelisk);
        }

        nbt.putInt("fading_ticks", this.getFadingTicks());

        NbtList capturedSoulsNbt = new NbtList();
        for (UUID capturedSoul : this.capturedSouls) {
            capturedSoulsNbt.add(NbtHelper.fromUuid(capturedSoul));
        }
        nbt.put("captured_souls", capturedSoulsNbt);

        this.writeAngerToNbt(nbt);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        if (offer instanceof RemnantTradeOffer demonTradeOffer && demonTradeOffer.demonCustomer && this.getCustomer() instanceof ServerPlayerEntity player) {
            RemnantComponent.get(player).become(RemnantTypes.MORTAL, true);
            player.world.playSound(null, player.getX(), player.getY(), player.getZ(), RequiemSoundEvents.ITEM_OPUS_USE, player.getSoundCategory(), 1.4F, 0.1F);
            RequiemNetworking.sendTo(player, RequiemNetworking.createOpusUsePacket(RemnantTypes.MORTAL, false));
        }
        if (offer.shouldRewardPlayerExperience()) {
            int i = 3 + this.random.nextInt(4);
            this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY() + 0.5D, this.getZ(), i));
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.world.isClient && this.isDead()) {
            for (UUID capturedSoul : this.capturedSouls) {
                FilledSoulVesselItem.releaseSoul(this, capturedSoul);
            }
        }
        super.remove(reason);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasCustomer() ? RequiemSoundEvents.ENTITY_MORTICIAN_TRADE : RequiemSoundEvents.ENTITY_MORTICIAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return RequiemSoundEvents.ENTITY_MORTICIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return RequiemSoundEvents.ENTITY_MORTICIAN_DEATH;
    }

    @Override
    protected SoundEvent getTradingSound(boolean sold) {
        return sold ? RequiemSoundEvents.ENTITY_MORTICIAN_YES : RequiemSoundEvents.ENTITY_MORTICIAN_NO;
    }

    @Override
    public SoundEvent getYesSound() {
        return RequiemSoundEvents.ENTITY_MORTICIAN_YES;
    }

    public SoundEvent getCastSpellSound() {
        return RequiemSoundEvents.ENTITY_MORTICIAN_CAST_SPELL;
    }
}
