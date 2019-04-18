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
package ladysnake.requiem.mixin.entity.attribute;

import ladysnake.requiem.common.entity.ai.attribute.DelegatingAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityAttributes.class)
public abstract class EntityAttributesMixin {
    /**
     * Prevents saving the wrong attributes to disk
     */
    @ModifyVariable(
            method = "toTag(Lnet/minecraft/entity/attribute/EntityAttributeInstance;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At(value = "INVOKE", ordinal = 0),
            ordinal = 0
    )
    private static EntityAttributeInstance toTag(EntityAttributeInstance attribute) {
        while (attribute instanceof DelegatingAttribute) {
            attribute = ((DelegatingAttribute) attribute).getOriginal();
        }
        return attribute;
    }
}
