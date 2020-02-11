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
 */
package ladysnake.requiem.common.item;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.RequiemComponents;
import ladysnake.requiem.common.impl.remnant.dialogue.PlayerDialogueTracker;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
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
        if (player.isSneaking()) {
            if (!world.isClient) {
                debugMode = (debugMode + 1) % 3;
                player.addChatMessage(new TranslatableText("Switched mode to %s", debugMode), true);
            }
        } else {
            switch (debugMode) {
                case 0:
                    if (world.isClient) {
                        if (((RequiemPlayer) player).getDeathSuspender().isLifeTransient()) {
                            RequiemNetworking.sendToServer(RequiemNetworking.createDialogueActionMessage(PlayerDialogueTracker.BECOME_REMNANT));
                            ((RequiemPlayer) player).getDeathSuspender().setLifeTransient(false);
                        } else {
                            ((RequiemPlayer) player).getDeathSuspender().setLifeTransient(true);
                        }
                    }
                    break;
                case 1:
                    if (!world.isClient) {
                        RequiemComponents.HOROLOGIST_MANAGER.get(world.getLevelProperties())
                            .trySpawnHorologistAround((ServerWorld) player.world, new BlockPos.Mutable(player));
                    }
                    break;
                case 2:
                    if (!world.isClient) {
                        RequiemPlayer.from(player).become(RemnantTypes.MORTAL);
                        ((RequiemPlayer) player).getDeathSuspender().suspendDeath(DamageSource.CACTUS);
                    }
                    break;
            }
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

}
