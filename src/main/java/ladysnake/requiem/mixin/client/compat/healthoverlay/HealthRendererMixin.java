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
package ladysnake.requiem.mixin.client.compat.healthoverlay;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "terrails.healthoverlay.HealthRenderer", remap = false)
public class HealthRendererMixin {
    @SuppressWarnings({"UnresolvedMixinReference", "InvalidMemberReference"})
    @Redirect(method = {"render", "renderHearts"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getHealth()F"))
    private float substituteHealth(PlayerEntity player) {
        LivingEntity possessed = ((RequiemPlayer)player).asPossessor().getPossessedEntity();
        if (possessed != null) {
            return possessed.getHealth();
        }
        return player.getHealth();
    }
}
