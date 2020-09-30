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
package ladysnake.requiem.common.impl.resurrection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ladysnake.requiem.mixin.common.access.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
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
        m.put("head_in_sand", (lazarus, killingBlow) -> {
            EntityPose pose = lazarus.getPose();
            float eyeHeight = ((EntityAccessor) lazarus).invokeGetEyeHeight(pose, lazarus.getDimensions(pose));
            return BlockTags.SAND.contains(lazarus.world.getBlockState(lazarus.getBlockPos().add(0, eyeHeight, 0)).getBlock());
        });
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
