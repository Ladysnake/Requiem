/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.api.v1.entity.CurableEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements CurableEntity {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    @Nullable
    public abstract <T extends MobEntity> T method_29243(EntityType<T> entityType, boolean bl);

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow
    protected abstract float getDropChance(EquipmentSlot slot);

    @Override
    public @Nullable MobEntity cureAsPossessed() {
        MobEntity cured = this.createCuredEntity();
        if (cured != null) {
            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                ItemStack itemStack = this.getEquippedStack(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    if (EnchantmentHelper.hasBindingCurse(itemStack)) {
                        cured.equip(equipmentSlot.getEntitySlotId() + 300, itemStack);
                    } else {
                        if (this.getDropChance(equipmentSlot) > 1.0D) {
                            this.dropStack(itemStack);
                        }
                    }
                }
            }
            cured.initialize(((ServerWorld) world), world.getLocalDifficulty(cured.getBlockPos()), SpawnReason.CONVERSION, null, null);
            cured.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));

            if (!this.isSilent()) {
                world.syncWorldEvent(null, 1027, this.getBlockPos(), 0);
            }
        }
        return cured;
    }

    @Unique
    protected @Nullable MobEntity createCuredEntity() {
        return this.method_29243(EntityType.VILLAGER, false);
    }
}
