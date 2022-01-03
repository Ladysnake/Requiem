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
package ladysnake.requiem.common.entity.ai.brain.tasks;

import baritone.api.fakeplayer.FakeServerPlayerEntity;
import com.google.common.collect.ImmutableMap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import ladysnake.requiem.core.util.RayHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

public class PlayerMeleeTask extends Task<FakeServerPlayerEntity> {
    public PlayerMeleeTask() {
        super(ImmutableMap.of(
            MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
            MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT
        ));
    }

    public static double getAttackRange(LivingEntity attacker) {
        return ReachEntityAttributes.getAttackRange(attacker, 3.);
    }

    @Override
    protected boolean shouldRun(ServerWorld world, FakeServerPlayerEntity executor) {
        LivingEntity target = this.getAttackTarget(executor);
        return !this.isHoldingRangedWeapon(executor)
            && LookTargetUtil.isVisibleInMemory(executor, target)
            && this.isLookingAt(executor, target)
            && this.isAttackReady(executor, target);
    }

    private boolean isLookingAt(FakeServerPlayerEntity executor, LivingEntity target) {
        return RayHelper.getTargetedEntity(executor) == target;
    }

    private boolean isAttackReady(PlayerEntity executor, LivingEntity target) {
        return target.timeUntilRegen <= 10 && (executor.getAttackCooldownProgress(0.5F) >= 1 || estimateDamage(executor, target) > target.getHealth());
    }

    @Override
    protected void run(ServerWorld serverWorld, FakeServerPlayerEntity mobEntity, long l) {
        LivingEntity livingEntity = this.getAttackTarget(mobEntity);
        LookTargetUtil.lookAt(mobEntity, livingEntity);
        mobEntity.tryAttack(livingEntity);
        // Note the hand swinging MUST be after the attack, as it resets the cooldown
        mobEntity.swingHand(Hand.MAIN_HAND);
    }

    private boolean isHoldingRangedWeapon(PlayerEntity player) {
        return player.isHolding((item) -> item.getItem() instanceof RangedWeaponItem);
    }

    private LivingEntity getAttackTarget(LivingEntity executor) {
        return executor.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).orElseThrow(IllegalStateException::new);
    }

    /**
     * Copied from {@link PlayerEntity#attack(Entity)}
     */
    public static float estimateDamage(PlayerEntity player, Entity target) {
        float baseDamage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float enchantmentBonus;
        if (target instanceof LivingEntity) {
            enchantmentBonus = EnchantmentHelper.getAttackDamage(player.getMainHandStack(), ((LivingEntity) target).getGroup());
        } else {
            enchantmentBonus = EnchantmentHelper.getAttackDamage(player.getMainHandStack(), EntityGroup.DEFAULT);
        }

        float i = player.getAttackCooldownProgress(0.5F);
        baseDamage *= 0.2F + i * i * 0.8F;
        enchantmentBonus *= i;
        return baseDamage + enchantmentBonus;
    }
}
