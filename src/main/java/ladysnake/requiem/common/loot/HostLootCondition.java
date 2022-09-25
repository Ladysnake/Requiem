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
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;

import java.util.Set;

public class HostLootCondition implements LootCondition {
    private final CheckedEntity checkedEntity;
    private final LootContext.EntityTarget entity;
    private final EntityPredicate predicate;

    public HostLootCondition(CheckedEntity checkedEntity, LootContext.EntityTarget entity, EntityPredicate predicate) {
        this.checkedEntity = checkedEntity;
        this.entity = entity;
        this.predicate = predicate;
    }

    @Override
    public LootConditionType getType() {
        return RequiemLootTables.HOST_CONDITION;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.ORIGIN, this.entity.getParameter());
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.get(this.entity.getParameter()) instanceof LivingEntity e && predicate.test(
            lootContext.getWorld(),
            lootContext.get(LootContextParameters.ORIGIN),
            switch (checkedEntity) {
                case HOST -> PossessionComponent.getHost(e);
                case POSSESSOR -> ((Possessable) e).getPossessor();
            }
        );
    }

    public static class Serializer implements JsonSerializer<HostLootCondition> {
        private final CheckedEntity checkedEntity;

        public Serializer(CheckedEntity checkedEntity) {
            this.checkedEntity = checkedEntity;
        }

        @Override
        public void toJson(JsonObject jsonObject, HostLootCondition condition, JsonSerializationContext ctx) {
            jsonObject.add("entity", ctx.serialize(condition.entity));
            jsonObject.add("predicate", condition.predicate.toJson());
        }

        @Override
        public HostLootCondition fromJson(JsonObject jsonObject, JsonDeserializationContext ctx) {
            return new HostLootCondition(
                checkedEntity,
                JsonHelper.deserialize(jsonObject, "entity", ctx, LootContext.EntityTarget.class),
                EntityPredicate.fromJson(jsonObject.get("predicate"))
            );
        }
    }

    public enum CheckedEntity {
        HOST, POSSESSOR
    }
}
