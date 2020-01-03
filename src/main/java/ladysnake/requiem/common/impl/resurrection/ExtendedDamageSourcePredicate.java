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
package ladysnake.requiem.common.impl.resurrection;

import com.google.gson.JsonElement;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Objects;

public final class ExtendedDamageSourcePredicate {
    public static final ExtendedDamageSourcePredicate EMPTY = new ExtendedDamageSourcePredicate(DamageSourcePredicate.EMPTY, null);
    private final DamageSourcePredicate base;
    private final String damageName;

    private ExtendedDamageSourcePredicate(DamageSourcePredicate base, String damageName) {
        this.base = base;
        this.damageName = damageName;
    }

    public boolean test(ServerPlayerEntity player, DamageSource damage) {
        return (damageName == null || damageName.equals(damage.name)) && base.test(player, damage);
    }

    public static ExtendedDamageSourcePredicate deserialize(@Nullable JsonElement element) {
        DamageSourcePredicate base = DamageSourcePredicate.deserialize(element);
        if (base == DamageSourcePredicate.EMPTY) {
            return EMPTY;
        }
        String damageName = JsonHelper.getString(Objects.requireNonNull(element).getAsJsonObject(), "name", null);
        return new ExtendedDamageSourcePredicate(base, damageName);
    }
}
