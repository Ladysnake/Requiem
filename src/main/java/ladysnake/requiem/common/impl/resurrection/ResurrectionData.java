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
package ladysnake.requiem.common.impl.resurrection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public final class ResurrectionData implements Comparable<ResurrectionData> {
    private static final Map<String, BiPredicate<ServerPlayerEntity, DamageSource>> SPECIAL_PREDICATES = Util.make(new HashMap<>(), m -> {
        m.put("head_in_sand", (lazarus, killingBlow) -> BlockTags.SAND.contains(lazarus.world.getBlockState(lazarus.getBlockPos().add(0, lazarus.getEyeHeight(lazarus.getPose()), 0)).getBlock()));
    });

    private final int priority;
    @Nullable
    private final EntityPredicate playerPredicate;
    @Nullable
    private final ExtendedDamageSourcePredicate damageSourcePredicate;
    private final EntityType<?> entityType;
    @Nullable
    private final CompoundTag entityNbt;
    private final List<BiPredicate<ServerPlayerEntity, DamageSource>> specials;

    private ResurrectionData(int priority, @Nullable EntityPredicate playerPredicate, @Nullable ExtendedDamageSourcePredicate damageSourcePredicate, EntityType<?> entityType, @Nullable CompoundTag entityNbt, List<BiPredicate<ServerPlayerEntity, DamageSource>> specials) {
        this.priority = priority;
        this.playerPredicate = playerPredicate;
        this.damageSourcePredicate = damageSourcePredicate;
        this.entityType = entityType;
        this.entityNbt = entityNbt;
        this.specials = specials;
    }

    public boolean matches(ServerPlayerEntity player, DamageSource killingBlow) {
        if (damageSourcePredicate != null && !damageSourcePredicate.test(player, killingBlow)) {
            return false;
        }
        if (playerPredicate != null && !playerPredicate.test(player, player)) {
            return false;
        }
        for (BiPredicate<ServerPlayerEntity, DamageSource> specialCondition : specials) {
            if (!specialCondition.test(player, killingBlow)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public Entity createEntity(World world) {
        Entity e = this.entityType.create(world);
        if (e != null && this.entityNbt != null) {
            e.fromTag(this.entityNbt);
        }
        return e;
    }

    public static ResurrectionData deserialize(JsonObject json) {
        int priority = JsonHelper.getInt(json, "priority", 100);
        @Nullable ExtendedDamageSourcePredicate damagePredicate = ExtendedDamageSourcePredicate.deserialize(json.get("killing_blow"));
        @Nullable EntityPredicate playerPredicate = EntityPredicate.fromJson(json.get("player"));
        if (damagePredicate == null && playerPredicate == null) {
            throw new JsonParseException("Resurrection data must have either a damage source predicate (\"killingBlow\") or an entity predicate (\"player\")");
        }
        JsonObject entityData = JsonHelper.getObject(json, "entity");
        String typeId = JsonHelper.getString(entityData, "type");
        EntityType<?> type = EntityType.get(typeId).orElseThrow(() -> new JsonParseException("Invalid entity id " + typeId));
        @Nullable CompoundTag nbt;
        if (entityData.has("nbt")) {
            try {
                nbt = StringNbtReader.parse(JsonHelper.getString(entityData, "nbt"));
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Failed to read resurrection entity NBT: " + e.getMessage());
            }
        } else {
            nbt = null;
        }
        List<BiPredicate<ServerPlayerEntity, DamageSource>> specials = new ArrayList<>();
        JsonArray specialConditions = JsonHelper.getArray(json, "special_conditions", new JsonArray());
        assert specialConditions != null;
        for (JsonElement specialCondition : specialConditions) {
            specials.add(SPECIAL_PREDICATES.get(JsonHelper.asString(specialCondition, "special condition")));
        }
        return new ResurrectionData(priority, playerPredicate, damagePredicate, type, nbt, specials);
    }

    @Override
    public int compareTo(@NotNull ResurrectionData o) {
        return o.priority - this.priority;
    }
}
