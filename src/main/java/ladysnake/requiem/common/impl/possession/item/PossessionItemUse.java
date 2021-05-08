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
package ladysnake.requiem.common.impl.possession.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.api.v1.possession.item.PossessionItemAction;
import ladysnake.requiem.common.RequiemRegistries;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.NotNull;

public final class PossessionItemUse implements Comparable<PossessionItemUse> {
    private static final Codec<JsonElement> JSON_CODEC = Codec.PASSTHROUGH.xmap(d -> d.convert(JsonOps.INSTANCE).getValue(), json -> new Dynamic<>(JsonOps.INSTANCE));
    private static final Codec<PossessionItemUse> CODEC_V0 = RecordCodecBuilder.create((instance) -> instance.group(
        Codec.INT.optionalFieldOf("priority", 0).forGetter(PossessionItemUse::getPriority),
        Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PossessionItemUse::isEnabled),
        JSON_CODEC.optionalFieldOf("possessed", null).xmap(EntityPredicate::fromJson, EntityPredicate::toJson).forGetter(PossessionItemUse::getPossessed),
        JSON_CODEC.optionalFieldOf("used_item", null).xmap(ItemPredicate::fromJson, ItemPredicate::toJson).forGetter(PossessionItemUse::getUsedItem),
        Codec.INT.optionalFieldOf("use_time", 0).forGetter(PossessionItemUse::getUseTime),
        RequiemRegistries.MOB_ACTIONS.fieldOf("action").forGetter(PossessionItemUse::getAction)
    ).apply(instance, PossessionItemUse::new));

    private static final int CURRENT_SCHEMA_VERSION = 0;

    public static final Codec<PossessionItemUse> CODEC = Codec.PASSTHROUGH.flatXmap(
        d -> {
            int schemaVersion = d.get("schema_version").asInt(-1);
            if (schemaVersion == 0) {
                return CODEC_V0.parse(d);
            } else if (schemaVersion == -1) {
                return DataResult.error("Missing schema version");
            } else {
                return DataResult.error(String.format("Invalid/Unsupported schema version \"%s\" was found", schemaVersion));
            }
        },
        d -> CODEC_V0.encodeStart(JsonOps.INSTANCE, d).map(j -> {
            j.getAsJsonObject().addProperty("schema_version", CURRENT_SCHEMA_VERSION);
            return new Dynamic<>(JsonOps.INSTANCE, j);
        })
    );

    private final int priority;
    private final boolean enabled;
    private final EntityPredicate possessed;
    private final ItemPredicate usedItem;
    private final int useTime;
    private final PossessionItemAction action;

    public PossessionItemUse(int priority, boolean enabled, EntityPredicate possessed, ItemPredicate usedItem, int useTime, PossessionItemAction action) {
        this.priority = priority;
        this.enabled = enabled;
        this.possessed = possessed;
        this.usedItem = usedItem;
        this.useTime = useTime;
        this.action = action;
    }

    public static PossessionItemUse deserialize(JsonObject json) {
        int schemaVersion = JsonHelper.getInt(json, "schema_version", 0);
        if (schemaVersion != 0) {
            throw new JsonParseException(String.format("Invalid/Unsupported schema version \"%s\" was found", schemaVersion));
        }

        return deserializeV0(json);
    }

    private static PossessionItemUse deserializeV0(JsonObject json) {
        return new PossessionItemUse(
            JsonHelper.getInt(json, "priority", 100),
            JsonHelper.getBoolean(json, "enabled", true),
            EntityPredicate.fromJson(json.get("possessed")),
            ItemPredicate.fromJson(json.get("used_item")),
            JsonHelper.getInt(json, "use_time", 0),
            RequiemRegistries.MOB_ACTIONS.getOrEmpty(Identifier.tryParse(JsonHelper.getString(json, "action"))).orElseThrow(RuntimeException::new)
        );
    }

    public static PossessionItemUse fromPacket(PacketByteBuf buf) {
        int priority = buf.readVarInt();
        boolean enabled = buf.readBoolean();
        EntityPredicate possessed = EntityPredicate.fromJson(JsonHelper.deserialize(buf.readString()));
        ItemPredicate usedItem = ItemPredicate.fromJson(JsonHelper.deserialize(buf.readString()));
        int useTime = buf.readVarInt();
        PossessionItemAction action = RequiemRegistries.MOB_ACTIONS.getOrEmpty(buf.readIdentifier()).orElseThrow(RuntimeException::new);
        return new PossessionItemUse(priority, enabled, possessed, usedItem, useTime, action);
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeVarInt(this.priority);
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public EntityPredicate getPossessed() {
        return possessed;
    }

    public ItemPredicate getUsedItem() {
        return usedItem;
    }

    public int getUseTime() {
        return useTime;
    }

    public PossessionItemAction getAction() {
        return action;
    }

    @Override
    public int compareTo(@NotNull PossessionItemUse o) {
        return o.priority - this.priority;
    }
}
