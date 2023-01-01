/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.api.v1.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.api.v1.util.MoreCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * @param pos the position of the structure's origin
 */
public record ObeliskDescriptor(RegistryKey<World> dimension, BlockPos pos, int width, int height, Optional<Text> name) {
    public static final Codec<ObeliskDescriptor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        World.CODEC.fieldOf("dimension").forGetter(ObeliskDescriptor::dimension),
        BlockPos.CODEC.fieldOf("pos").forGetter(ObeliskDescriptor::pos),
        Codec.INT.optionalFieldOf("width", 1).forGetter(ObeliskDescriptor::width),
        Codec.INT.optionalFieldOf("height", 1).forGetter(ObeliskDescriptor::height),
        MoreCodecs.text(MoreCodecs.STRING_JSON).optionalFieldOf("name").forGetter(ObeliskDescriptor::name)
    ).apply(instance, ObeliskDescriptor::new));

    public ObeliskDescriptor(RegistryKey<World> dimension, BlockPos pos, int width, int height) {
        this(dimension, pos, width, height, Optional.empty());
    }

    public Vec3d center() {
        return new Vec3d(
            (this.pos().getX() * 2 + this.width - 1) * 0.5,
            (this.pos().getY() * 2 + this.height - 1) * 0.5,
            (this.pos().getZ() * 2 + this.width - 1) * 0.5
        );
    }

    public Text resolveName() {
        return this.name().orElseGet(() -> Text.of(this.pos.toShortString()));
    }
}
