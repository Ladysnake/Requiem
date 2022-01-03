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

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import ladysnake.requiem.common.entity.MorticianEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

import java.util.Set;

public class RiftMorticianLootCondition implements LootCondition {
    private final LootContext.EntityTarget entity;
    private final boolean rift;

    public RiftMorticianLootCondition(LootContext.EntityTarget entity, boolean rift) {
        this.entity = entity;
        this.rift = rift;
    }

    @Override
    public LootConditionType getType() {
        return RequiemLootTables.RIFT_MORTICIAN_CONDITION;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.ORIGIN, this.entity.getParameter());
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.get(this.entity.getParameter()) instanceof MorticianEntity mortician && mortician.isObeliskProjection();
    }

    public static class Serializer implements JsonSerializer<RiftMorticianLootCondition> {
        @Override
        public void toJson(JsonObject jsonObject, RiftMorticianLootCondition condition, JsonSerializationContext ctx) {
            jsonObject.add("entity", ctx.serialize(condition.entity));
            jsonObject.addProperty("rift", condition.rift);
        }

        @Override
        public RiftMorticianLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext ctx) {
            return new RiftMorticianLootCondition(
                JsonHelper.deserialize(jsonObject, "entity", ctx, LootContext.EntityTarget.class),
                JsonHelper.getBoolean(jsonObject, "rift", true)
            );
        }
    }
}
