/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.core.util.serde;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.nbt.NbtCompound;
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
                FALLING_BLOCK, DRAGON_BREATH, DRYOUT, SWEET_BERRY_BUSH}) {
            DAMAGE_FACTORIES.put(damage.name, (e1, e2) -> damage);
        }
        for (String name : new String[] {"mob", "arrow", "trident", "fireball", "thrown", "indirectMagic", "fireworks"}) {
            DAMAGE_FACTORIES.put(name, (e1, e2) -> new ProjectileDamageSource(name, e1, e2));
        }
    }

    public static NbtCompound toTag(DamageSource damage) {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", damage.name);
        if (damage.getSource() != null) {
            tag.putUuid("sourceUuid", damage.getSource().getUuid());
        }
        if (damage.getAttacker() != null) {
            tag.putUuid("attackerUuid", damage.getAttacker().getUuid());
        }
        return tag;
    }

    public static DamageSource fromTag(NbtCompound tag, @Nullable ServerWorld world) {
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
