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
package ladysnake.requiem.common.item.dispensing;

import ladysnake.requiem.common.entity.ReleasedSoulEntity;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.item.FilledSoulVesselItem;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class FilledVesselItemDispenserBehavior implements DispenserBehavior {
    private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

    public FilledVesselItemDispenserBehavior() {
    }

    public ItemStack tryPutStack(BlockPointer pointer, ItemStack inputStack, ItemStack outputStack) {
        inputStack.decrement(1);
        if (inputStack.isEmpty()) {
            return outputStack.copy();
        } else {
            if (pointer.<DispenserBlockEntity>getBlockEntity().addToFirstFreeSlot(outputStack.copy()) < 0) {
                fallbackBehavior.dispense(pointer, outputStack.copy());
            }

            return inputStack;
        }
    }

    @Override
    public ItemStack dispense(BlockPointer pointer, ItemStack stack) {
        Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
        Vec3d targetPos = Vec3d.ofCenter(pointer.getPos().offset(direction));
        @Nullable UUID ownerRecord = Optional.ofNullable(stack.getSubNbt(FilledSoulVesselItem.SOUL_FRAGMENT_NBT))
            .filter(data -> data.containsUuid("uuid"))
            .map(data -> data.getUuid("uuid"))
            .orElse(null);
        ReleasedSoulEntity releasedSoul = new ReleasedSoulEntity(RequiemEntities.RELEASED_SOUL, pointer.getWorld(), ownerRecord);
        releasedSoul.setPosition(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        releasedSoul.setVelocity(new Vec3d(direction.getUnitVector()).multiply(0.15f));
        releasedSoul.setYaw(pointer.getWorld().random.nextFloat());
        releasedSoul.setPitch(pointer.getWorld().random.nextFloat());
        pointer.getWorld().spawnEntity(releasedSoul);
        ItemStack result = ((FilledSoulVesselItem) stack.getItem()).getEmptiedStack();
        return this.tryPutStack(pointer, stack, result);
    }
}
