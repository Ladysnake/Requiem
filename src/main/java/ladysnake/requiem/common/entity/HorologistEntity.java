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
package ladysnake.requiem.common.entity;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.RequiemComponents;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class HorologistEntity extends PassiveEntity implements Npc {
    public HorologistEntity(EntityType<? extends HorologistEntity> type, World world) {
        super(type, world);
    }

    @Override
    public net.minecraft.entity.EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType spawnType, @Nullable net.minecraft.entity.EntityData entityData, @Nullable CompoundTag entityTag) {
        if (entityData == null) {
            entityData = new PassiveEntity.EntityData();
            ((PassiveEntity.EntityData)entityData).setBabyAllowed(false);
        }

        return super.initialize(world, difficulty, spawnType, entityData, entityTag);
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return this.isBaby() ? 0.81F : 1.62F;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(PassiveEntity mate) {
        return null;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 32.0F));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        ParticleEffect particleType = null;
        if (source.getAttacker() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getAttacker();
            this.swapPosition(attacker);
            attacker.damage(source, amount);
            particleType = ParticleTypes.SMOKE;
        } else if (source.isSourceCreativePlayer() || source.isOutOfWorld() || amount > 3F){
            RequiemComponents.HOROLOGIST_MANAGER.get(this.world.getLevelProperties()).freeHorologist();
            this.remove();
            particleType = ParticleTypes.LARGE_SMOKE;
        }
        if (particleType != null) {
            ((ServerWorld) this.world).spawnParticles(
                particleType,
                this.offsetX(0.5D), this.getHeight() * 0.5, this.offsetZ(0.5D),
                15,
                this.getWidth() * 0.5, this.getHeight() * 0.5, this.getWidth() * 0.5,
                0.2
            );
        }
        return false;
    }

    private void swapPosition(LivingEntity attacker) {
        Vec3d pos = attacker.getPos();
        float yaw = attacker.yaw;
        float pitch = attacker.pitch;
        attacker.copyPositionAndRotation(this);
        this.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
    }

    @Override
    public boolean interactMob(PlayerEntity player, Hand hand) {
        if (world.isClient) {
            RequiemPlayer.from(player).getDialogueTracker().startDialogue(Requiem.id("remnant_choice"));
            return true;
        }
        return super.interactMob(player, hand);
    }
}
