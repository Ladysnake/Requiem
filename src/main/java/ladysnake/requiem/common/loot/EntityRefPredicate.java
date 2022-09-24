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
package ladysnake.requiem.common.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import ladysnake.requiem.api.v1.record.EntityPointer;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.DistancePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record EntityRefPredicate(DistancePredicate distance, LocationPredicate location, EntityPredicate entity) {
    public static final EntityRefPredicate ANY = new EntityRefPredicate(
        DistancePredicate.ANY,
        LocationPredicate.ANY,
        EntityPredicate.ANY
    );

    public boolean test(ServerWorld world, @Nullable Vec3d origin, @Nullable EntityPointer entityPointer) {
        if (this == ANY) {
            return true;
        } else if (entityPointer == null) {
            return false;
        } else {
            Vec3d location = entityPointer.pos();

            if (origin == null) {
                if (this.distance != DistancePredicate.ANY) {
                    return false;
                }
            } else if (!this.distance.test(origin.getX(), origin.getY(), origin.getZ(), location.x, location.y, location.z)) {
                return false;
            }

            if (!this.location.test(world, location.x, location.y, location.z)) {
                return false;
            }

            @Nullable Entity entity = entityPointer.resolve(world.getServer()).orElse(null);
            return this.entity.test(world, origin, entity);
        }
    }

    public JsonElement toJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject json = new JsonObject();
            json.add("distance", this.distance.toJson());
            json.add("location", this.location.toJson());
            json.add("entity", this.entity.toJson());
            return json;
        }
    }

    public static EntityRefPredicate fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return ANY;
        } else {
            JsonObject jsonObject = JsonHelper.asObject(json, "entity_ref");
            DistancePredicate distance = DistancePredicate.fromJson(jsonObject.get("distance"));
            LocationPredicate location = LocationPredicate.fromJson(jsonObject.get("location"));
            EntityPredicate entity = EntityPredicate.fromJson(jsonObject.get("entity"));
            return new EntityRefPredicate(distance, location, entity);
        }
    }
}
