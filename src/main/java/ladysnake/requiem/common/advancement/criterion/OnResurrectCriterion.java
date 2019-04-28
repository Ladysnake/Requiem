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
