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
        Handler handler = this.getHandler(player.getAdvancementManager());
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

            this.apply(conditionsContainers);
        }
    }
}
