package ladysnake.requiem.common.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class OnPossessionCriterion extends AbstractCriterion<OnPossessionCriterion.Conditions> {
    private final Identifier id;

    public OnPossessionCriterion(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    public void handle(ServerPlayerEntity possessor, LivingEntity possessed) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(possessor, possessed);
        this.test(possessor, (conditions) -> conditions.test(lootContext));
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(this.id, playerPredicate, EntityPredicate.Extended.getInJson(obj, "entity", predicateDeserializer));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final EntityPredicate.Extended predicate;

        public Conditions(Identifier id, EntityPredicate.Extended playerPredicate, EntityPredicate.Extended predicate) {
            super(id, playerPredicate);
            this.predicate = predicate;
        }

        public boolean test(LootContext ctx) {
            return this.predicate.test(ctx);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = super.toJson(predicateSerializer);
            json.add("entity", this.predicate.toJson(predicateSerializer));
            return json;
        }
    }
}
