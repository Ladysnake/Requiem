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
package ladysnake.pandemonium.common.impl.anchor;

import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityFractureAnchor extends TrackedFractureAnchor {
    private final UUID entityUuid;

    public EntityFractureAnchor(UUID entityUuid, FractureAnchorManager manager, UUID uuid, int id) {
        super(manager, uuid, id);
        this.entityUuid = entityUuid;
    }

    protected EntityFractureAnchor(FractureAnchorManager manager, CompoundTag tag, int id) {
        super(manager, tag, id);
        this.entityUuid = tag.getUuid("AnchorEntity");
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
        } else if (this.manager.getWorld().isChunkLoaded(((int)this.x) >> 4, ((int)this.z) >> 4)) {
            // chunk is loaded but entity not found -- assume dead
            this.invalidate();
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
