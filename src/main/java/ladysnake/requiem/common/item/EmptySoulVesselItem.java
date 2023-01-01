/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.common.item;

import ladysnake.requiem.api.v1.event.requiem.SoulCaptureEvents;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.block.obelisk.InertRunestoneBlock;
import ladysnake.requiem.common.block.obelisk.RunestoneBlock;
import ladysnake.requiem.common.entity.RequiemEntityAttributes;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import ladysnake.requiem.common.particle.RequiemEntityParticleEffect;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.common.remnant.WandererRemnantState;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.core.entity.SoulHolderComponent;
import ladysnake.requiem.core.record.EntityPositionClerk;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;
import java.util.UUID;

public class EmptySoulVesselItem extends Item {

    public static final String ACTIVE_DATA_TAG = "requiem:soul_capture";
    /**
     * The damage inflicted to soul aggregating creatures when stealing one of their souls
     */
    public static final int SOUL_AGGREGATE_STEALING_DAMAGE = 20;
    /**
     * The delay before a newly filled vessel can be used
     */
    public static final int POST_CAPTURE_COOLDOWN = 100;

    public EmptySoulVesselItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();

        TypedActionResult<ItemStack> result = useOnBlock(world, pos, stack);
        if (result.getResult().isAccepted()) {
            if (player != null) {
                Item item = stack.getItem();
                player.setStackInHand(context.getHand(), ItemUsage.exchangeStack(stack, player, result.getValue()));
                player.incrementStat(Stats.USED.getOrCreateStat(item));
            }
        }
        return result.getResult();
    }

    public static TypedActionResult<ItemStack> useOnBlock(World world, BlockPos pos, ItemStack stack) {
        if (world.getBlockState(pos).getBlock() instanceof RunestoneBlock runestone) {
            if (!world.isClient) {
                IchorVesselItem filledVessel = RequiemItems.vesselsByEffect.get(runestone.getEffect());
                if (filledVessel == null) return TypedActionResult.fail(stack);

                world.setBlockState(pos, RequiemBlocks.TACHYLITE_RUNESTONE.getDefaultState(), Block.NOTIFY_LISTENERS | Block.NOTIFY_NEIGHBORS);
                InertRunestoneBlock.tryActivateObelisk((ServerWorld) world, pos, false);

                world.playSound(null, pos, RequiemSoundEvents.BLOCK_RUNESTONE_CLEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                return TypedActionResult.success(new ItemStack(filledVessel), false);
            }

            return TypedActionResult.success(stack, true);
        }

        return TypedActionResult.pass(stack);
    }

    /**
     * Requires {@link ladysnake.requiem.mixin.common.vessel.MobEntityMixin} to work because ugh mojang
     */
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        Optional<SoulCaptureEvents.CaptureType> captureType = checkCapturePreconditions(user, entity);
        if (captureType.isPresent()) {
            NbtCompound activeData = stack.getOrCreateSubNbt(ACTIVE_DATA_TAG);
            activeData.putInt("use_time", computeCaptureTime(entity, user, captureType.get()));
            activeData.putUuid("target", entity.getUuid());
            // will be a copy in creative mode, so need to copy changes too
            user.getStackInHand(hand).getOrCreateSubNbt(ACTIVE_DATA_TAG).copyFrom(activeData);
            user.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }

        return super.useOnEntity(stack, user, entity, hand);
    }

    public static Optional<SoulCaptureEvents.CaptureType> checkCapturePreconditions(LivingEntity user, LivingEntity entity) {
        return Optional.of(entity)
            .filter(e -> e instanceof MobEntity)
            .flatMap(EmptySoulVesselItem::getCaptureType)
            .filter(captureType -> SoulCaptureEvents.BEFORE_ATTEMPT.invoker().canAttemptCapturing(user, entity, captureType));
    }

    private static Optional<SoulCaptureEvents.CaptureType> getCaptureType(LivingEntity entity) {
        if (entity.getType().isIn(RequiemEntityTypeTags.SOUL_AGGREGATES)) {
            return Optional.of(SoulCaptureEvents.CaptureType.AGGREGATE);
        } else if (!entity.getType().isIn(RequiemEntityTypeTags.SOUL_CAPTURE_BLACKLIST)) {
            return Optional.of(SoulCaptureEvents.CaptureType.NORMAL);
        }

        return Optional.empty();
    }

    private int computeCaptureTime(LivingEntity target, LivingEntity user, SoulCaptureEvents.CaptureType captureType) {
        int targetSoulStrength = computeSoulDefense(target, captureType);
        int playerSoulStrength = computeSoulOffense(user);
        float max = switch (captureType) {
            case NORMAL -> 3.0f;
            case AGGREGATE -> 1.5f;
        };
        return Math.round(96.0f * MathHelper.clamp((float) targetSoulStrength / playerSoulStrength, 1.0f, max));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!(world instanceof ServerWorld serverWorld)) return stack;
        NbtCompound activeData = stack.getSubNbt(ACTIVE_DATA_TAG);
        if (activeData == null) return stack;

        stack.removeSubNbt(ACTIVE_DATA_TAG);
        Entity entity = serverWorld.getEntity(activeData.getUuid("target"));

        if (!(entity instanceof LivingEntity target)) return stack;
        if (!(user instanceof ServerPlayerEntity remnant)) return stack;
        Optional<SoulCaptureEvents.CaptureType> captureType = checkCapturePreconditions(remnant, target);
        if (captureType.isEmpty()) return stack;

        ItemStack result;
        remnant.incrementStat(Stats.USED.getOrCreateStat(this));

        if (wins(remnant, target, captureType.get())) {
            result = FilledSoulVesselItem.forEntityType(entity.getType());
            UUID recordUuid = switch(captureType.get()) {
                case NORMAL -> setupRecord(target).getUuid();
                case AGGREGATE -> UUID.randomUUID(); // if we are stealing a soul from an aggregate, there is no linked entity
            };
            result.getOrCreateSubNbt(FilledSoulVesselItem.SOUL_FRAGMENT_NBT).putUuid("uuid", recordUuid);
            switch (captureType.get()) {
                case NORMAL -> SoulHolderComponent.get(target).removeSoul();
                case AGGREGATE -> target.damage(DamageSource.MAGIC, SOUL_AGGREGATE_STEALING_DAMAGE);
            }
            remnant.getItemCooldownManager().set(RequiemItems.FILLED_SOUL_VESSEL, POST_CAPTURE_COOLDOWN);
        } else {
            AttritionStatusEffect.apply(remnant, 1, 20 * 60 * 5);
            WandererRemnantState.spawnAttritionParticles(remnant, remnant);
            remnant.incrementStat(Stats.BROKEN.getOrCreateStat(this));
            remnant.sendToolBreakStatus(user.getActiveHand());
            result = new ItemStack(RequiemItems.SHATTERED_SOUL_VESSEL);
        }

        return ItemUsage.exchangeStack(stack, remnant, result, false);
    }

    public static GlobalRecord setupRecord(LivingEntity target) {
        return EntityPositionClerk.get(target).getOrCreateRecord();
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        NbtCompound tag = stack.getSubNbt(ACTIVE_DATA_TAG);
        return tag == null ? 0 : tag.getInt("use_time");
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world instanceof ServerWorld serverWorld) {
            NbtCompound useData = stack.getSubNbt(ACTIVE_DATA_TAG);
            if (useData != null) {
                Entity target = serverWorld.getEntity(useData.getUuid("target"));
                if (target instanceof LivingEntity) {
                    playSoulCaptureEffects(user, target);
                }
            }
        }
    }

    public static void playSoulCaptureEffects(LivingEntity user, Entity target) {
        if (!(user.world instanceof ServerWorld world)) throw new IllegalStateException("Must be called serverside");
        if (world.getRandom().nextFloat() < 0.75f) {
            world.spawnParticles(
                new RequiemEntityParticleEffect(RequiemParticleTypes.ENTITY_DUST, target.getId(), user.getId()),
                target.getX(), target.getBodyY(0.5), target.getZ(),
                world.random.nextInt(6) + 4,
                target.getWidth() * 0.2,
                target.getHeight() * 0.2,
                target.getWidth() * 0.2,
                1.0
            );
        }
        user.playSound(RequiemSoundEvents.ITEM_EMPTY_VESSEL_USE, 1, 1);
    }

    public static boolean wins(LivingEntity user, LivingEntity target, SoulCaptureEvents.CaptureType captureType) {
        int soulOffense = computeSoulOffense(user);
        int soulDefense = computeSoulDefense(target, captureType);

        if (soulOffense > soulDefense) {
            return true;
        }

        float strengthRatio = (float) soulOffense / soulDefense;
        return user.getRandom().nextFloat() < (strengthRatio * strengthRatio);
    }

    static int computeSoulOffense(LivingEntity user) {
        return (int) Math.round(user.getAttributeValue(RequiemEntityAttributes.SOUL_OFFENSE));
    }

    static int computeSoulDefense(LivingEntity entity, SoulCaptureEvents.CaptureType captureType) {
        double base = entity.getAttributeValue(RequiemEntityAttributes.SOUL_DEFENSE);
        double maxHealth = switch (captureType) {
            case NORMAL -> entity.getMaxHealth();
            case AGGREGATE -> 1;
        };
        double intrinsicArmor = getAttributeBaseValue(entity, EntityAttributes.GENERIC_ARMOR);
        double intrinsicArmorToughness = getAttributeBaseValue(entity, EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        double intrinsicStrength = getAttributeBaseValue(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double invertedHealthRatio = 1 - entity.getHealth() / entity.getMaxHealth();
        double woundedModifier = 1 - (invertedHealthRatio * invertedHealthRatio);
        double healthModifier = woundedModifier * 0.8 + 0.2;
        double strengthModifier = computeStrengthModifier(intrinsicStrength);
        double physicalModifier = strengthModifier * healthModifier;
        return (int) Math.round(base + physicalModifier * (maxHealth + intrinsicArmor * 2 + intrinsicArmorToughness * 3));
    }

    private static double computeStrengthModifier(double intrinsicStrength) {
        if (intrinsicStrength == 0) return 0.5;
        else if (intrinsicStrength < 2) return 0.75;
        else return Math.sqrt(intrinsicStrength * 0.5);
    }

    private static double getAttributeBaseValue(LivingEntity entity, EntityAttribute attribute) {
        if (!entity.getAttributes().hasAttribute(attribute)) return 0;
        return entity.getAttributeBaseValue(attribute);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
