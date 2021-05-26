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
package ladysnake.requiem.common.impl.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class LazyEntityPredicate extends LazyDataPredicate<EntityPredicate> {
    public static final LazyEntityPredicate ANY = new LazyEntityPredicate(null);

    public static Codec<LazyEntityPredicate> codec(Codec<JsonElement> jsonCodec) {
        return jsonCodec.xmap(LazyEntityPredicate::new, LazyDataPredicate::getJson);
    }

    public LazyEntityPredicate(@Nullable JsonElement json) {
        super(json);
    }

    public boolean test(Entity entity) {
        if (!entity.world.isClient) {
            return this.get(entity.world).test((ServerWorld) entity.world, null, entity);
        } else {
            // We still need to have some idea of whether this test can succeed clientside
            // Thankfully, most tests will never use the server world, so we can just pass null and pray
            try {
                return this.get(entity.world).test(null/*Possible NPE*/, null, entity);
            } catch (NullPointerException npe) {
                // We will have to check this serverside
                return true;
            }
        }
    }

    @Override
    protected EntityPredicate deserialize(@Nullable JsonElement json) {
        return EntityPredicate.fromJson(json);
    }
}
