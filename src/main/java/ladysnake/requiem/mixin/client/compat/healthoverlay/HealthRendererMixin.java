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
package ladysnake.requiem.mixin.client.compat.healthoverlay;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "terrails.healthoverlay.HealthRenderer")
public class HealthRendererMixin {
    @Shadow
    private MinecraftClient client;

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"), ordinal = 1)
    private int substituteHealth(int health) {
        assert client.player != null;
        LivingEntity entity = ((RequiemPlayer)client.player).asPossessor().getPossessedEntity();
        if (entity != null) {
            return MathHelper.ceil(entity.getHealth());
        }
        return health;
    }
}
