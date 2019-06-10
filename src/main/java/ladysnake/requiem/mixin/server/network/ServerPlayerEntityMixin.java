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
package ladysnake.requiem.mixin.server.network;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements RequiemPlayer {

    @Shadow public abstract ServerWorld getServerWorld();

    @Shadow public abstract PlayerAdvancementTracker getAdvancementManager();

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "method_14226", at = @At("HEAD"), cancellable = true)
    private void stopTicking(CallbackInfo ci) {
        if (this.getDeathSuspender().isLifeTransient()) {
            ci.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void suspendDeath(DamageSource killingBlow, CallbackInfo ci) {
        Identifier advancementId = Requiem.id("adventure/the_choice");
        Advancement theChoice = this.getServerWorld().getServer().getAdvancementManager().get(advancementId);
        AdvancementProgress progress = this.getAdvancementManager().getProgress(theChoice);
        if (progress == null) {
            Requiem.LOGGER.error("Advancement '{}' is missing", advancementId);
        } else if (!progress.isDone()) {
            this.getDeathSuspender().suspendDeath(killingBlow);
            ci.cancel();
        }
    }

    @Inject(method = "onStartedTracking", at = @At("HEAD"))
    private void onStartedTracking(Entity tracked, CallbackInfo info) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (tracked instanceof PlayerEntity) {
            // Synchronize soul players with other players
            sendTo(self, createCorporealityMessage((PlayerEntity) tracked));
            Entity possessed = ((RequiemPlayer)tracked).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                // in case the possessed entity gets tracked before its possessor
                sendTo(self, createPossessionMessage(possessed.getUuid(), tracked.getEntityId()));
            }
        } else if (tracked instanceof Possessable) {
            // Synchronize possessed entities with their possessor / other players
            ((Possessable) tracked).getPossessorUuid()
                    .ifPresent(uuid -> sendTo(self, createPossessionMessage(uuid, tracked.getEntityId())));
        }
    }
}
