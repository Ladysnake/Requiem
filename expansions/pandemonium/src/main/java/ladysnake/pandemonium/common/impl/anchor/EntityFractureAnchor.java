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

import ladysnake.pandemonium.api.anchor.GlobalEntityPos;
import ladysnake.pandemonium.api.anchor.GlobalEntityTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Optional;
import java.util.UUID;

public class EntityFractureAnchor extends InertFractureAnchor {

    public EntityFractureAnchor(GlobalEntityTracker manager, UUID uuid, int id, GlobalEntityPos pos, boolean synced) {
        super(checkSide(manager), uuid, id, pos, synced);
    }

    private static GlobalEntityTracker checkSide(GlobalEntityTracker manager) {
        if (!(manager instanceof ServerAnchorManager)) {
            throw new IllegalArgumentException("EntityFractureAnchor is only supported on ServerWorld!");
        }
        return manager;
    }

    @Override
    public void update() {
        super.update();
        this.getEntity().ifPresentOrElse(
            this::syncWithEntity,
            () -> this.getWorld().ifPresentOrElse(
                this::checkEntityAlive,
                this::invalidate
            ));
    }

    public Optional<Entity> getEntity() {
        return this.getWorld().map(world -> ((ServerWorld) world).getEntity(this.getUuid()));
    }

    @Override
    public Identifier getType() {
        return ENTITY_TYPE;
    }

    private void checkEntityAlive(World world) {
        WorldChunk chunk = (WorldChunk) world.getChunk(((int) this.getPos().x()) >> 4, ((int) this.getPos().z()) >> 4, ChunkStatus.FULL, false);
        // In some circumstances, it seems that a chunk can be loaded without the entity being found in the world
        if (chunk != null && chunk.getLevelType().isAfter(ChunkHolder.LevelType.ENTITY_TICKING)) {
            // chunk is loaded but entity not in it -- assume dead
            this.invalidate();
        }
    }

    private void syncWithEntity(Entity entity) {
        if (entity instanceof LivingEntity living && living.getHealth() <= 0.0F) {
            this.invalidate();
        } else if (entity.getX() != this.getPos().x() || entity.getY() != this.getPos().y() || entity.getZ() != this.getPos().z()) {
            this.setPos(new GlobalEntityPos(entity));
        }
    }
}
