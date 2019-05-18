/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.requiem.common.util;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;

public class InventoryHelper {
    public static void transferEquipment(LivingEntity source, LivingEntity dest) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stuff = source.getEquippedStack(slot);
            if (stuff.isEmpty()) {
                continue;
            }
            if (!dest.getEquippedStack(slot).isEmpty()) {
                dest.dropStack(stuff, 0.5f);
            } else {
                dest.setEquippedStack(slot, stuff);
                if (dest instanceof MobEntity) {
                    ((MobEntity) dest).setEquipmentDropChance(slot, 2.0F);
                }
            }
            source.setEquippedStack(slot, ItemStack.EMPTY);
        }
    }
}
