/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

/**
 * A pointer containing all the necessary information to locate an entity.
 *
 * @param uuid  {@linkplain Entity#getUuid() the entity's unique serverside identifier}
 * @param world the {@link RegistryKey} describing {@linkplain Entity#getEntityWorld() the entity's current dimension}
 * @param pos   the {@link Vec3d} describing {@linkplain Entity#getPos() the entity's last known position}
 */
public record EntityPointer(UUID uuid, RegistryKey<World> world, Vec3d pos) {
    public static final Codec<EntityPointer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        DynamicSerializableUuid.CODEC.fieldOf("uuid").forGetter(EntityPointer::uuid),
        World.CODEC.fieldOf("world").forGetter(EntityPointer::world),
        RecordCodecBuilder.<Vec3d>create(instance2 -> instance2.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3d::getX),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3d::getY),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3d::getZ)
        ).apply(instance2, Vec3d::new)).fieldOf("pos").forGetter(EntityPointer::pos)
    ).apply(instance, EntityPointer::new));

    public EntityPointer(Entity entity) {
        this(entity.getUuid(), entity.world.getRegistryKey(), entity.getPos());
    }

    /**
     * Attempts to find the entity this pointer is referencing using the given {@link MinecraftServer}.
     *
     * <p>The entity will only be found if it is already loaded into a world.
     *
     * @param server the server object to use to find the entity
     * @return an {@code Optional} describing the referenced entity, or {@code Optional.empty()} if no corresponding loaded entity was found
     */
    public Optional<Entity> resolve(MinecraftServer server) {
        return Optional.ofNullable(server.getWorld(this.world())).map(world -> world.getEntity(this.uuid()));
    }
}
