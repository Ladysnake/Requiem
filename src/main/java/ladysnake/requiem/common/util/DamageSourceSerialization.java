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
 */
package ladysnake.requiem.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static net.minecraft.entity.damage.DamageSource.*;

public final class DamageSourceSerialization {
    public static final Map<String, BiFunction<@Nullable Entity, @Nullable Entity, DamageSource>> DAMAGE_FACTORIES = new HashMap<>();

    static {
        // God I wish we had a registry for this
        for (DamageSource damage : new DamageSource[]{IN_FIRE, LIGHTNING_BOLT, ON_FIRE, LAVA, HOT_FLOOR, IN_WALL,
                CRAMMING, DROWN, STARVE, CACTUS, FALL, FLY_INTO_WALL, OUT_OF_WORLD, GENERIC, MAGIC, WITHER, ANVIL,
                FALLING_BLOCK, DRAGON_BREATH, FIREWORKS, DRYOUT, SWEET_BERRY_BUSH}) {
            DAMAGE_FACTORIES.put(damage.name, (e1, e2) -> damage);
        }
        for (String name : new String[] {"mob", "arrow", "trident", "fireball", "thrown", "indirectMagic"}) {
            DAMAGE_FACTORIES.put(name, (e1, e2) -> new ProjectileDamageSource(name, e1, e2));
        }
    }

    public static CompoundTag toTag(DamageSource damage) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", damage.name);
        if (damage.getSource() != null) {
            tag.putUuid("sourceUuid", damage.getSource().getUuid());
        }
        if (damage.getAttacker() != null) {
            tag.putUuid("attackerUuid", damage.getAttacker().getUuid());
        }
        return tag;
    }

    public static DamageSource fromTag(CompoundTag tag, @Nullable ServerWorld world) {
        String name = tag.getString("name");
        final Entity source;
        final Entity attacker;
        if (world != null) {
            // If the tag does not have those keys, the result should be null
            source = world.getEntity(tag.getUuid("sourceUuid"));
            attacker = world.getEntity(tag.getUuid("attackerUuid"));
        } else {
            source = null;
            attacker = null;
        }
        return Optional.ofNullable(DAMAGE_FACTORIES.get(name)).map(factory -> factory.apply(source, attacker)).orElse(GENERIC);
    }
}
