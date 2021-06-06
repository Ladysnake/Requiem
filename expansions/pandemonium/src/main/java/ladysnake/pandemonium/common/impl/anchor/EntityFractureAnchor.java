/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.pandemonium.common.impl.anchor;

import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class EntityFractureAnchor extends TrackedFractureAnchor {
    private final UUID entityUuid;

    public EntityFractureAnchor(UUID entityUuid, FractureAnchorManager manager, UUID uuid, int id) {
        super(checkSide(manager), uuid, id);
        this.entityUuid = entityUuid;
    }

    protected EntityFractureAnchor(FractureAnchorManager manager, CompoundTag tag, int id) {
        super(checkSide(manager), tag, id);
        this.entityUuid = tag.getUuid("AnchorEntity");
    }

    private static FractureAnchorManager checkSide(FractureAnchorManager manager) {
        if (!(manager.getWorld() instanceof ServerWorld)) {
            throw new IllegalArgumentException("EntityFractureAnchor is only supported on ServerWorld!");
        }
        return manager;
    }

    @Override
    public void update() {
        super.update();
        Entity entity = this.getEntity();
        if (entity != null) {
            if (entity instanceof LivingEntity && ((LivingEntity)entity).getHealth() <= 0.0F) {
                this.invalidate();
            } else if (entity.getX() != this.x || entity.getY() != this.y || entity.getZ() != this.z) {
                this.setPosition(entity.getX(), entity.getY(), entity.getZ());
            }
        } else {
            WorldChunk chunk = (WorldChunk) this.manager.getWorld().getChunk(((int) this.x) >> 4, ((int) this.z) >> 4, ChunkStatus.FULL, false);
            // In some circumstances, it seems that a chunk can be loaded without the entity being found in the world
            if (chunk != null && chunk.getLevelType().isAfter(ChunkHolder.LevelType.ENTITY_TICKING) && Arrays.stream(chunk.getEntitySectionArray()).flatMap(Collection::stream).map(Entity::getUuid).noneMatch(this.entityUuid::equals)) {
                // chunk is loaded but entity not in it -- assume dead
                this.invalidate();
            }
        }
    }

    @Nullable
    public Entity getEntity() {
        return ((ServerWorld)this.manager.getWorld()).getEntity(this.entityUuid);
    }

    @Override
    public CompoundTag toTag(CompoundTag anchorTag) {
        super.toTag(anchorTag);
        anchorTag.putString("AnchorType", "requiem:entity");
        anchorTag.putUuid("AnchorEntity", this.entityUuid);
        return anchorTag;
    }
}
