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
package ladysnake.requiem.core.resurrection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ladysnake.requiem.api.v1.event.requiem.ConsumableItemEvents;
import ladysnake.requiem.core.RequiemCoreNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
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
import java.util.function.Predicate;

public record ResurrectionData(
    int priority,
    @Nullable EntityPredicate playerPredicate,
    @Nullable EntityPredicate possessedPredicate,
    @Nullable ExtendedDamageSourcePredicate damageSourcePredicate,
    @Nullable ItemPredicate consumable,
    List<BiPredicate<ServerPlayerEntity, DamageSource>> specials,
    EntityType<?> entityType,
    @Nullable NbtCompound entityNbt
) implements Comparable<ResurrectionData> {
    private static final Map<String, BiPredicate<ServerPlayerEntity, DamageSource>> SPECIAL_PREDICATES = Util.make(new HashMap<>(), m -> {
        m.put("head_in_sand", (lazarus, killingBlow) -> {
            float eyeHeight = lazarus.getEyeHeight(lazarus.getPose());
            return lazarus.world.getBlockState(lazarus.getBlockPos().add(0, eyeHeight, 0)).isIn(BlockTags.SAND);
        });
    });

    public boolean matches(ServerPlayerEntity player, @Nullable LivingEntity possessed, DamageSource killingBlow) {
        if (killingBlow.isOutOfWorld()) return false;

        if (damageSourcePredicate != null && !damageSourcePredicate.test(player, killingBlow)) {
            return false;
        }

        if (playerPredicate != null && !playerPredicate.test(player, player)) {
            return false;
        }

        if (possessedPredicate == null && possessed != null || possessedPredicate != null && !possessedPredicate.test(player, possessed)) {
            return false;
        }

        for (BiPredicate<ServerPlayerEntity, DamageSource> specialCondition : specials) {
            if (!specialCondition.test(player, killingBlow)) {
                return false;
            }
        }

        return this.tryUseConsumable(player, possessed == null ? player : possessed);
    }

    private boolean tryUseConsumable(ServerPlayerEntity player, LivingEntity user) {
        if (this.consumable == null) return true;

        Predicate<ItemStack> action = new Predicate<>() {
            private boolean found;

            @Override
            public boolean test(ItemStack stack) {
                if (this.found) throw new IllegalStateException("Consumable already found!");

                if (ResurrectionData.this.consumable.test(stack)) {
                    ItemStack totem = stack.copy();
                    stack.decrement(1);
                    player.incrementStat(Stats.USED.getOrCreateStat(totem.getItem()));
                    // can't pass the actual user to the packet since it's dying
                    RequiemCoreNetworking.sendItemConsumptionPacket(player, totem);
                    this.found = true;
                    return true;
                }

                return false;
            }
        };

        for (Hand hand : Hand.values()) {
            if (action.test(user.getStackInHand(hand))) {
                return true;
            }
        }

        return ConsumableItemEvents.SEARCH.invoker().findConsumables(player, action);
    }

    @Nullable
    public Entity createEntity(World world) {
        Entity e = this.entityType.create(world);
        if (e != null && this.entityNbt != null) {
            e.readNbt(this.entityNbt.copy());   // some entities may keep direct references to the passed NBT
        }
        return e;
    }

    public static ResurrectionData deserialize(JsonObject json) {
        int schemaVersion = JsonHelper.getInt(json, "schema_version", 0);
        if (schemaVersion != 0) {
            throw new JsonParseException(String.format("Invalid/Unsupported schema version \"%s\" was found", schemaVersion));
        }

        // TODO make a V1 format with `player` and `possessed` merged into `entity` and `entity` renamed to `result`
        return deserializeV0(json);
    }

    @NotNull
    private static ResurrectionData deserializeV0(JsonObject json) {
        int priority = JsonHelper.getInt(json, "priority", 100);
        @Nullable ExtendedDamageSourcePredicate damagePredicate = ExtendedDamageSourcePredicate.deserialize(json.get("killing_blow"));
        @Nullable EntityPredicate playerPredicate = json.has("player") ? EntityPredicate.fromJson(json.get("player")) : null;
        @Nullable EntityPredicate possessedPredicate = json.has("possessed") ? EntityPredicate.fromJson(json.get("possessed")) : null;
        @Nullable ItemPredicate consumable = json.has("consumable") ? ItemPredicate.fromJson(json.get("consumable")) : null;

        if (damagePredicate == null && playerPredicate == null && possessedPredicate == null && consumable == null) {
            throw new JsonParseException("Resurrection data must have at least one of a damage source predicate (\"killingBlow\"), or an entity predicate (\"player\" and/or \"possessed\"), or an item predicate (\"consumable\")");
        }

        List<BiPredicate<ServerPlayerEntity, DamageSource>> specials = new ArrayList<>();
        JsonArray specialConditions = JsonHelper.getArray(json, "special_conditions", null);

        if (specialConditions != null) {
            for (JsonElement specialCondition : specialConditions) {
                specials.add(SPECIAL_PREDICATES.get(JsonHelper.asString(specialCondition, "special condition")));
            }
        }

        JsonObject entityData = JsonHelper.getObject(json, "entity");
        String typeId = JsonHelper.getString(entityData, "type");
        EntityType<?> type = EntityType.get(typeId).orElseThrow(() -> new JsonParseException("Invalid entity id " + typeId));
        @Nullable NbtCompound nbt;
        if (entityData.has("nbt")) {
            try {
                nbt = StringNbtReader.parse(JsonHelper.getString(entityData, "nbt"));
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Failed to read resurrection entity NBT: " + e.getMessage());
            }
        } else {
            nbt = null;
        }

        return new ResurrectionData(priority, playerPredicate, possessedPredicate, damagePredicate, consumable, specials, type, nbt);
    }

    @Override
    public int compareTo(@NotNull ResurrectionData o) {
        return o.priority - this.priority;
    }
}
