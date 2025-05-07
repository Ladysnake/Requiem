/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
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
package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.common.particle.RequiemSoundParticleEffect;
import net.minecraft.entity.mob.warden.WardenEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.GameEventTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow
    public abstract boolean sendToPlayerIfNearby(ServerPlayerEntity player, boolean force, double x, double y, double z, Packet<?> packet);

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();

    @Inject(method = "emitGameEvent(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/event/GameEvent$Context;)V",at=@At("HEAD"))
    public void emitGameEvent(GameEvent event, Vec3d pos, GameEvent.Context context, CallbackInfo ci) {
        if (event.isIn(GameEventTags.WARDEN_CAN_SENSE)) {
            //TODO: by:Redfan2: maybe ask Tags for Color

            //Fallback
            Color color = Color.white;

            if (event.isIn(TagKey.of(RegistryKeys.GAME_EVENT, new Identifier(Requiem.MOD_ID,"blocks")))) {
                color = Color.green;
            }
            if (event.isIn(TagKey.of(RegistryKeys.GAME_EVENT,new Identifier(Requiem.MOD_ID,"entities")))) {
                color = Color.cyan;
            }
            Packet<?> packet = new ParticleS2CPacket(
                new RequiemSoundParticleEffect(RequiemParticleTypes.SOUND, color.getRGB()),
                true,
                pos.getX() + .5f,
                pos.getY() + .5f,
                pos.getZ() + .5f,
                0,
                0,
                0,
                0,
                1
            );
            for (ServerPlayerEntity player : this.getPlayers()) {
                if (PossessionComponent.get(player).isPossessionOngoing() && PossessionComponent.getHost(player) instanceof WardenEntity && !(context.sourceEntity() instanceof WardenEntity)) {
                    this.sendToPlayerIfNearby(player, true, pos.getX(), pos.getY(), pos.getZ(), packet);
                }
            }
        }
    }
}
