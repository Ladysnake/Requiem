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

import ladysnake.requiem.common.entity.internal.WardenExtension;
import ladysnake.requiem.common.entity.warden.WardenSensedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.warden.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(WardenEntity.class)
public abstract class WardenMixin extends HostileEntity implements WardenExtension {

    @Shadow
    public abstract boolean isEnemy(@Nullable Entity entity);

    protected WardenMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }


    @Unique
    @Override
    public Set<Entity> requiem$getVisibleEntities() {
        HashSet<Entity> entities = new java.util.HashSet<>();
        ((WardenEntity)(Object) this).getAngerManager().getSuspects().forEach(entry -> {
            if (entry.getSecond() > 0) {
                entities.add(((ServerWorld) this.getWorld()).getEntity(entry.getFirst()));
            }
        });

        return entities;
    }

    @Unique
    @Override
    public Set<PlayerEntity> requiem$getVisiblePlayers() {
        HashSet<PlayerEntity> visiblePlayers = new HashSet<>();
        requiem$getVisibleEntities().forEach(entity->{
            if (entity instanceof PlayerEntity) {
                visiblePlayers.add((PlayerEntity) entity);
            }
        });
        return visiblePlayers;

    }

    @Inject(
        method = "mobTick",
        at=@At(
            value= "TAIL"
        )
    )
    public void tickEverySecond(CallbackInfo ci) {
        //TODO: by:Redfan2: replace by Mixin
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,-1,5,false,false,false),this);
        if (age % 200 == 0 ) {
            this.requiem$syncSensedEntities();
        }
    }

    @Unique
    public void requiem$syncSensedEntities() {
        if (!this.getWorld().isClient()) {
            this.getWorld().getPlayers().forEach(player -> {
                if (player.getComponent(WardenSensedComponent.KEY).shouldSyncWith((ServerPlayerEntity) player)){
                    player.getComponent(WardenSensedComponent.KEY).setVisibile(requiem$getVisibleEntities());
                    WardenSensedComponent.KEY.sync(player);
                }
            });
        }
    }

    @Inject(at=@At("TAIL"),method = "increaseAngerAt(Lnet/minecraft/entity/Entity;IZ)V")
    public void syncWhenAngerIncreased(Entity entity, int angerLevel, boolean playSound, CallbackInfo ci) {
        if (this.isEnemy(entity)) {
            this.requiem$syncSensedEntities();
        }
    }
}
