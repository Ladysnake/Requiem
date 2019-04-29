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
package ladysnake.requiem.common.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DebugItem extends Item {
    private int debugMode;

    public DebugItem(Settings item$Settings_1) {
        super(item$Settings_1);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (player.isSneaking() && !world.isClient) {
            debugMode = (debugMode + 1) % 2;
            player.addChatMessage(new TranslatableTextComponent("Switched mode to %s", debugMode), true);
        } else {
            switch (debugMode) {
                case 0:
                    if (world.isClient) {
                        player.addChatMessage(new StringTextComponent("Hi"), true);
                    }
                    break;
                case 1:
                    break;
            }
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    @Override
    public boolean interactWithEntity(ItemStack itemStack_1, PlayerEntity playerEntity_1, LivingEntity livingEntity_1, Hand hand_1) {
        return true;
    }

    @Override
    public boolean beforeBlockBreak(BlockState blockState_1, World world_1, BlockPos blockPos_1, PlayerEntity playerEntity_1) {
        return super.beforeBlockBreak(blockState_1, world_1, blockPos_1, playerEntity_1);
    }
}
