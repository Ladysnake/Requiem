/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.util.RequiemTargetPredicate;
import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import ladysnake.requiem.common.item.FilledSoulVesselItem;
import ladysnake.requiem.common.item.RequiemItems;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopAndLookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MorticianEntity extends MerchantEntity {
    public static final TradeOffers.Factory[] TRADES = new TradeOffers.Factory[]{
                (entity, random) -> {
                    // TODO create reverse offer
                    if (entity instanceof PlayerEntity player && !RemnantComponent.get(player).getRemnantType().isDemon()) {
                        return new TradeOffer(new ItemStack(RequiemItems.EMPTY_SOUL_VESSEL), new ItemStack(Items.NETHERITE_INGOT), new ItemStack(RequiemItems.SEALED_REMNANT_VESSEL), 1, 1, 0.05F);
                    }
                    return new TradeOffer(new ItemStack(Items.GOLD_INGOT, 32), new ItemStack(Items.NETHERITE_INGOT), new ItemStack(RequiemItems.SEALED_REMNANT_VESSEL), 1, 1, 0.05f);
                },
                (entity, random) -> new TradeOffer(new ItemStack(RequiemItems.SHATTERED_SOUL_VESSEL), new ItemStack(Items.GOLD_INGOT), new ItemStack(RequiemItems.EMPTY_SOUL_VESSEL), 10, 1, 0.05F),
                (entity, random) -> new TradeOffer(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(RequiemEnchantments.HUMANITY, 1)), new ItemStack(Items.GOLD_INGOT, 20), EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(RequiemEnchantments.HUMANITY, 2)), 5, 1, 0.05F),
                (entity, random) -> new TradeOffer(FilledSoulVesselItem.forEntityType(entity.getEntityWorld().getDimension().isUltrawarm() ? EntityType.PIGLIN : EntityType.VILLAGER), new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(RequiemItems.ICHOR_VESSEL_EMANCIPATION), 10, 1, 0.05F),
                (entity, random) -> new TradeOffer(FilledSoulVesselItem.forEntityType(EntityType.AXOLOTL), new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(RequiemItems.ICHOR_VESSEL_RECLAMATION), 10, 1, 0.05F),
                (entity, random) -> new TradeOffer(FilledSoulVesselItem.forEntityType(EntityType.CAVE_SPIDER), new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(RequiemItems.ICHOR_VESSEL_ATTRITION), 10, 1, 0.05F),
                (entity, random) -> new TradeOffer(FilledSoulVesselItem.forEntityType(EntityType.PILLAGER), new ItemStack(Items.GOLD_INGOT, 5), new ItemStack(RequiemItems.ICHOR_VESSEL_PENANCE), 10, 1, 0.05F)
            };

    public MorticianEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new StopFollowingCustomerGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 0.5D));
        this.goalSelector.add(1, new LookAtCustomerGoal(this));
        this.goalSelector.add(4, new GoToWalkTargetGoal(this, 0.35D));
        this.goalSelector.add(8, new WanderAroundFarGoal(this, 0.35D));
        this.goalSelector.add(9, new StopAndLookAtEntityGoal(this, PlayerEntity.class, 3.0F, 1.0F) {
            {
                RequiemTargetPredicate.includeIncorporeal(this.targetPredicate);
            }
        });
        this.goalSelector.add(10, new LookAtEntityGoal(this, MobEntity.class, 8.0F));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
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
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isOf(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.hasCustomer() && !this.isBaby()) {
            if (hand == Hand.MAIN_HAND) {
                player.incrementStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.world.isClient && !this.getOffers().isEmpty()) {
                this.setCurrentCustomer(player);
                this.sendOffers(player, this.getDisplayName(), 1);
            }

            return ActionResult.success(this.world.isClient);
        } else {
            return super.interactMob(player, hand);
        }
    }

    @Override
    protected void fillRecipes() {
        this.fillRecipesFromPool(this.getOffers(), TRADES, 7);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setBreedingAge(Math.max(0, this.getBreedingAge()));
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        if (offer.shouldRewardPlayerExperience()) {
            int i = 3 + this.random.nextInt(4);
            this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.getX(), this.getY() + 0.5D, this.getZ(), i));
        }

    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasCustomer() ? SoundEvents.ENTITY_PIGLIN_ADMIRING_ITEM : SoundEvents.ENTITY_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    protected SoundEvent getDrinkSound(ItemStack stack) {
        return stack.isOf(Items.MILK_BUCKET) ? SoundEvents.ENTITY_WANDERING_TRADER_DRINK_MILK : SoundEvents.ENTITY_WANDERING_TRADER_DRINK_POTION;
    }

    @Override
    protected SoundEvent getTradingSound(boolean sold) {
        return sold ? SoundEvents.ENTITY_PIGLIN_CELEBRATE : SoundEvents.ENTITY_PIGLIN_RETREAT;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_PIGLIN_ADMIRING_ITEM;
    }
}
