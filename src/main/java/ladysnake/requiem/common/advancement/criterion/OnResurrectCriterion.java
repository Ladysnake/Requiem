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
package ladysnake.requiem.common.advancement.criterion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OnResurrectCriterion extends CriterionBase<OnResurrectCriterion.Conditions, OnResurrectCriterion.Handler> {
    public OnResurrectCriterion(Identifier id) {
        super(id, Handler::new);
    }

    public void handle(ServerPlayerEntity player, Entity body) {
        Handler handler = this.getHandler(player.getAdvancementTracker());
        if (handler != null) {
            handler.handle(player, body);
        }
    }

    @Override
    public Conditions conditionsFromJson(JsonObject json, JsonDeserializationContext ctx) {
        return new Conditions(this.getId(), EntityPredicate.fromJson(json.get("body")));
    }

    static class Conditions extends AbstractCriterionConditions {
        private final EntityPredicate entity;

        public Conditions(Identifier id, EntityPredicate entity) {
            super(id);
            this.entity = entity;
        }

        public boolean test(ServerPlayerEntity player, @Nullable Entity body) {
            return this.entity.test(player, body);
        }
    }

    static class Handler extends CriterionBase.Handler<Conditions> {
        public Handler(PlayerAdvancementTracker tracker) {
            super(tracker);
        }

        public void handle(ServerPlayerEntity player, Entity body) {
            List<ConditionsContainer<Conditions>> conditionsContainers = null;

            for (ConditionsContainer<Conditions> condition : this.conditions) {
                if (condition.getConditions().test(player, body)) {
                    if (conditionsContainers == null) {
                        conditionsContainers = new ArrayList<>();
                    }

                    conditionsContainers.add(condition);
                }
            }

            this.grant(conditionsContainers);
        }
    }
}
