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
import net.minecraft.util.Identifier;

public class OnResurrectCriterion implements Criterion<OnResurrectCriterion.Conditions> {
    private final Identifier id;

    public OnResurrectCriterion(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public void beginTrackingCondition(PlayerAdvancementTracker var1, ConditionsContainer<Conditions> var2) {

    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker var1, ConditionsContainer<Conditions> var2) {

    }

    @Override
    public void endTracking(PlayerAdvancementTracker var1) {

    }

    @Override
    public Conditions conditionsFromJson(JsonObject var1, JsonDeserializationContext var2) {
        return null;
    }

    public static class Conditions extends AbstractCriterionConditions {
        public Conditions(Identifier id) {
            super(id);
        }
    }
}
