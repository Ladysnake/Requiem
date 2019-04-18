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
package ladysnake.requiem.mixin.client.network;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements RequiemPlayer {
    public ClientPlayerEntityMixin(World world_1, GameProfile gameProfile_1) {
        super(world_1, gameProfile_1);
    }

    @Inject(method = "cannotFitAt", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void stopPushingOutOfBlocks(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            Entity possessed = (Entity) this.getPossessionComponent().getPossessedEntity();
            if (possessed != null && possessed.getHeight() < 1F) {
                cir.setReturnValue(false);
            }
        }
    }

}
