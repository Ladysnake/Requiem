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
import ladysnake.requiem.common.entity.RequiemEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class SoulVesselItem extends Item {
    public SoulVesselItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (RemnantComponent.get(user).canCaptureSouls()) {
            int targetSoulStrength = computeSoulDefense(entity);
            int playerSoulStrength = computeSoulOffense(user);
            NbtCompound activeData = stack.getOrCreateSubTag("requiem:soul_capture");
            activeData.putInt("use_time", computeCaptureTime(targetSoulStrength, playerSoulStrength));
            activeData.putUuid("target", entity.getUuid());
        }
        return super.useOnEntity(stack, user, entity, hand);
    }

    private int computeCaptureTime(int targetSoulStrength, int playerSoulStrength) {
        return 96 * targetSoulStrength / playerSoulStrength;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!(world instanceof ServerWorld serverWorld)) return stack;
        NbtCompound activeData = stack.getSubTag("requiem:soul_capture");
        if (activeData == null) return stack;

        Entity entity = serverWorld.getEntity(activeData.getUuid("target"));

        if (!(entity instanceof LivingEntity target)) return stack;
        if (!(user instanceof PlayerEntity remnant && RemnantComponent.get(remnant).canCaptureSouls())) return stack;

        int targetSoulStrength = computeSoulDefense(target);
        int playerSoulStrength = computeSoulOffense(remnant);
        if (!wins(remnant, playerSoulStrength, target, targetSoulStrength)) return stack;

        return new ItemStack(RequiemItems.OMINOUS_SOUL_VESSEL);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        NbtCompound tag = stack.getTag();
        return tag == null ? 0 : tag.getInt("soulStealingTime");
    }

    private boolean wins(PlayerEntity user, int playerSoulStrength, LivingEntity entity, int targetSoulStrength) {
        return playerSoulStrength > targetSoulStrength;
    }

    private int computeSoulOffense(PlayerEntity user) {
        return (int) user.getAttributeValue(RequiemEntities.SOUL_OFFENSE);
    }

    private int computeSoulDefense(LivingEntity entity) {
        return (int) entity.getAttributeValue(RequiemEntities.SOUL_DEFENSE);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }
}
