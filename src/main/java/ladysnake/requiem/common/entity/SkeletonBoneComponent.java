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

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SkeletonBoneComponent implements Component {
    public static final ComponentKey<SkeletonBoneComponent> KEY = ComponentRegistry.getOrCreate(Requiem.id("skeleton_bones"), SkeletonBoneComponent.class);

    private final MobEntity owner;
    private int replacedBones;

    public SkeletonBoneComponent(MobEntity owner) {
        this.owner = owner;
    }

    public boolean replaceBone() {
        if (this.owner.isAlive() && this.owner.getHealth() < this.owner.getMaxHealth()) {
            this.replacedBones++;
            this.owner.heal(4.0f);
            this.owner.playAmbientSound();

            if (this.shouldBeReplaced()) {
                this.replaceSkeleton();
            }

            return true;
        }
        return false;
    }

    private void replaceSkeleton() {
        PlayerEntity possessor = ((Possessable) this.owner).getPossessor();
        SkeletonEntity replacement = this.owner.method_29243(EntityType.SKELETON, true);
        if (replacement != null) {
            replacement.setHealth(this.owner.getHealth());
            if (possessor instanceof ServerPlayerEntity) {
                RequiemCriteria.TRANSFORMED_POSSESSED_ENTITY.handle(((ServerPlayerEntity) possessor), this.owner, replacement, false);
            }
        }
    }

    private boolean shouldBeReplaced() {
        if (RequiemEntityTypeTags.REPLACEABLE_SKELETONS.contains(this.owner.getType())) {
            switch (this.owner.world.getDifficulty()) {
                case PEACEFUL: return false; // what is this skeleton doing in peaceful anyway....?
                case EASY: return this.replacedBones > 8;
                case NORMAL: return this.replacedBones > 4;
                default: return this.replacedBones > 2;
            }
        }
        return false;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("replaced_bones")) {
            this.replacedBones = tag.getInt("replaced_bones");
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        if (this.replacedBones > 0) {
            tag.putInt("replaced_bones", this.replacedBones);
        }
    }
}
