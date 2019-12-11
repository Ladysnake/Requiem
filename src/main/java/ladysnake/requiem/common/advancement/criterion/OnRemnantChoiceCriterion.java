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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class OnRemnantChoiceCriterion extends CriterionBase<OnRemnantChoiceCriterion.Conditions, OnRemnantChoiceCriterion.Handler> {
    public OnRemnantChoiceCriterion(Identifier id) {
        super(id, Handler::new);
    }

    @Override
    public Conditions conditionsFromJson(JsonObject json, JsonDeserializationContext ctx) {
        return new Conditions(this.getId(), RemnantTypePredicate.deserialize(json.get("remnant_type")));
    }

    public void handle(ServerPlayerEntity player, RemnantType chosenType) {
        Handler handler = this.getHandler(player.getAdvancementTracker());
        if (handler != null) {
            handler.handle(chosenType);
        }
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final RemnantTypePredicate predicate;

        public Conditions(Identifier id, RemnantTypePredicate predicate) {
            super(id);
            this.predicate = predicate;
        }

        public boolean test(RemnantType type) {
            return this.predicate.matches(type);
        }

        @Override
        public JsonElement toJson() {
            JsonObject json = new JsonObject();
            json.add("type", this.predicate.serialize());
            return json;
        }
    }

    public static class Handler extends CriterionBase.Handler<Conditions> {
        public Handler(PlayerAdvancementTracker tracker) {
            super(tracker);
        }

        public void handle(RemnantType chosenType) {
            List<ConditionsContainer<Conditions>> conditionsContainers = null;

            for (ConditionsContainer<Conditions> condition : this.conditions) {
                if (condition.getConditions().test(chosenType)) {
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
