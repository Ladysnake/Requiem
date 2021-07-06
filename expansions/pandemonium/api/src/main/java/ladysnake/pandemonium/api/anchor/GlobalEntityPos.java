/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.pandemonium.api.anchor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public record GlobalEntityPos(RegistryKey<World> world, double x, double y, double z) {
    public static final GlobalEntityPos ORIGIN = new GlobalEntityPos(World.OVERWORLD, 0, 0, 0);
    public static final Codec<GlobalEntityPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        World.CODEC.fieldOf("world").forGetter(GlobalEntityPos::world),
        Codec.DOUBLE.fieldOf("x").forGetter(GlobalEntityPos::x),
        Codec.DOUBLE.fieldOf("y").forGetter(GlobalEntityPos::y),
        Codec.DOUBLE.fieldOf("z").forGetter(GlobalEntityPos::z)
    ).apply(instance, GlobalEntityPos::new));

    public GlobalEntityPos(Entity entity) {
        this(entity.world.getRegistryKey(), entity.getX(), entity.getY(), entity.getZ());
    }
}
