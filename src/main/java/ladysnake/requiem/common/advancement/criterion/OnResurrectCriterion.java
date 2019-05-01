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
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.*;

public class OnResurrectCriterion implements Criterion<OnResurrectCriterion.Conditions> {
    private final Map<PlayerAdvancementTracker, OnResurrectCriterion.Handler> handlers = new HashMap<>();
    private final Identifier id;

    public OnResurrectCriterion(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public void beginTrackingCondition(PlayerAdvancementTracker tracker, ConditionsContainer<Conditions> container) {
        OnResurrectCriterion.Handler handler = this.handlers.get(tracker);
        if (handler == null) {
            handler = new OnResurrectCriterion.Handler(tracker);
            this.handlers.put(tracker, handler);
        }

        handler.addCondition(container);

    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker tracker, ConditionsContainer<Conditions> container) {
        Handler handler = this.handlers.get(tracker);
        if (handler != null) {
            handler.removeCondition(container);
            if (handler.isEmpty()) {
                this.handlers.remove(tracker);
            }
        }
    }

    @Override
    public void endTracking(PlayerAdvancementTracker tracker) {
        this.handlers.remove(tracker);
    }

    public void handle(ServerPlayerEntity player, Entity body) {
        Handler handler = this.handlers.get(player.getAdvancementManager());
        if (handler != null) {
            handler.handle(player, body);
        }
    }

    @Override
    public Conditions conditionsFromJson(JsonObject json, JsonDeserializationContext ctx) {
        return new Conditions(this.id, EntityPredicate.deserialize(json.get("body")));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final EntityPredicate entity;

        public Conditions(Identifier id, EntityPredicate entity) {
            super(id);
            this.entity = entity;
        }

        public boolean test(ServerPlayerEntity player, @Nullable Entity body) {
            return this.entity.test(player, body);
        }
    }

    private class Handler {
        private final PlayerAdvancementTracker tracker;
        private final Set<ConditionsContainer<Conditions>> conditions = new HashSet<>();

        public Handler(PlayerAdvancementTracker tracker) {
            this.tracker = tracker;
        }

        public void addCondition(ConditionsContainer<Conditions> conditionContainer) {
            this.conditions.add(conditionContainer);
        }

        public void removeCondition(Criterion.ConditionsContainer<Conditions> conditionContainer) {
            this.conditions.remove(conditionContainer);
        }
        
        public boolean isEmpty() {
            return this.conditions.isEmpty();
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

            if (conditionsContainers != null) {
                for (ConditionsContainer<Conditions> container : conditionsContainers) {
                    container.apply(this.tracker);
                }
            }
        }
    }
}
