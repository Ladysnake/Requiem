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
package ladysnake.requiem.mixin.common.remnant;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements RequiemPlayer {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow
    public abstract PlayerAdvancementTracker getAdvancementTracker();

    @Override
    @Shadow
    public abstract ServerWorld getWorld();

    @Inject(method = "playerTick", at = @At("HEAD"), cancellable = true)
    private void stopTicking(CallbackInfo ci) {
        if (DeathSuspender.get(this).isLifeTransient()) {
            ci.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void suspendDeath(DamageSource killingBlow, CallbackInfo ci) {
        Identifier advancementId = Requiem.id("adventure/the_choice");
        Advancement theChoice = this.getWorld().getServer().getAdvancementLoader().get(advancementId);
        AdvancementProgress progress = this.getAdvancementTracker().getProgress(theChoice);
        if (progress == null) {
            Requiem.LOGGER.error("Advancement '{}' is missing", advancementId);
        } else if (!progress.isDone() && !world.getLevelProperties().isHardcore()) {
            RemnantType startingRemnantType = world.getGameRules().get(RequiemGamerules.STARTING_SOUL_MODE).get().getRemnantType();
            if (startingRemnantType == null) {
                DeathSuspender.get(this).suspendDeath(killingBlow);
                ci.cancel();
            } else {
                RemnantComponent.get(this).become(startingRemnantType, true);
            }
        }
    }

    /**
     * When you die in hardcore, you are indeed dead
     */
    @Inject(method = "onDeath", at = @At(value = "FIELD", target = "Lnet/minecraft/world/GameRules;SHOW_DEATH_MESSAGES:Lnet/minecraft/world/GameRules$Key;"))
    private void revokeLifeRights(DamageSource source, CallbackInfo ci) {
        if (world.getLevelProperties().isHardcore()) {
            RemnantComponent.get(this).become(RemnantTypes.MORTAL);
        }
    }
}
