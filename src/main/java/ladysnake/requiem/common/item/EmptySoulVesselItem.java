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
package ladysnake.requiem.common.item;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.entity.RequiemEntityAttributes;
import ladysnake.requiem.common.entity.effect.AttritionStatusEffect;
import ladysnake.requiem.common.impl.remnant.WandererRemnantState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class EmptySoulVesselItem extends Item {

    public static final String ACTIVE_DATA_TAG = "requiem:soul_capture";

    public EmptySoulVesselItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (RemnantComponent.get(user).canCaptureSouls()) {
            int targetSoulStrength = computeSoulDefense(entity);
            int playerSoulStrength = computeSoulOffense(user);
            NbtCompound activeData = stack.getOrCreateSubTag(ACTIVE_DATA_TAG);
            activeData.putInt("use_time", computeCaptureTime(targetSoulStrength, playerSoulStrength));
            activeData.putUuid("target", entity.getUuid());
            // will be a copy in creative mode, so need to copy changes too
            user.getStackInHand(hand).getOrCreateSubTag(ACTIVE_DATA_TAG).copyFrom(activeData);
            user.setCurrentHand(hand);
            return ActionResult.CONSUME;
        }
        return super.useOnEntity(stack, user, entity, hand);
    }

    private int computeCaptureTime(int targetSoulStrength, int playerSoulStrength) {
        return Math.round(96.0f * Math.max(1.0f, (float) targetSoulStrength / playerSoulStrength));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!(world instanceof ServerWorld serverWorld)) return stack;
        NbtCompound activeData = stack.getSubTag(ACTIVE_DATA_TAG);
        if (activeData == null) return stack;

        stack.removeSubTag(ACTIVE_DATA_TAG);
        Entity entity = serverWorld.getEntity(activeData.getUuid("target"));

        if (!(entity instanceof LivingEntity target)) return stack;
        if (!(user instanceof ServerPlayerEntity remnant && RemnantComponent.get(remnant).canCaptureSouls())) return stack;

        int targetSoulStrength = computeSoulDefense(target);
        int playerSoulStrength = computeSoulOffense(remnant);
        ItemStack result;
        remnant.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!wins(remnant, playerSoulStrength, target, targetSoulStrength)) {
            AttritionStatusEffect.apply(remnant, 1, 20*60*5);
            WandererRemnantState.spawnAttritionParticles(remnant, remnant);
            remnant.incrementStat(Stats.BROKEN.getOrCreateStat(this));
            result = new ItemStack(RequiemItems.SHATTERED_SOUL_VESSEL);
        } else {
            result = new ItemStack(RequiemItems.FILLED_SOUL_VESSEL);
            result.getOrCreateSubTag(FilledSoulVesselItem.SOUL_FRAGMENT_NBT).putString("type", EntityType.getId(entity.getType()).toString());
        }
        return ItemUsage.exchangeStack(stack, remnant, result, false);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        NbtCompound tag = stack.getSubTag(ACTIVE_DATA_TAG);
        return tag == null ? 0 : tag.getInt("use_time");
    }

    private boolean wins(PlayerEntity user, int playerSoulStrength, LivingEntity entity, int targetSoulStrength) {
        if (playerSoulStrength > targetSoulStrength) {
            return true;
        }
        float strengthRatio = (float) playerSoulStrength / targetSoulStrength;
        return user.getRandom().nextFloat() > (strengthRatio * strengthRatio);
    }

    private static int computeSoulOffense(PlayerEntity user) {
        return (int) Math.round(user.getAttributeValue(RequiemEntityAttributes.SOUL_OFFENSE));
    }

    public static int computeSoulDefense(LivingEntity entity) {
        double base = entity.getAttributeValue(RequiemEntityAttributes.SOUL_DEFENSE);
        double maxHealth = entity.getMaxHealth();
        double intrinsicArmor = getAttributeBaseValue(entity, EntityAttributes.GENERIC_ARMOR);
        double intrinsicArmorToughness = getAttributeBaseValue(entity, EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        double intrinsicStrength = getAttributeBaseValue(entity, EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double invertedHealthRatio = entity.getMaxHealth() / entity.getHealth();
        double woundedModifier = 1 - (invertedHealthRatio * invertedHealthRatio * invertedHealthRatio);
        double physicalModifier = woundedModifier * 0.75 + 0.25;
        return (int) Math.round(base + physicalModifier * (maxHealth + intrinsicStrength * 2 + intrinsicArmor * 2 + intrinsicArmorToughness * 3));
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
