package ladysnake.requiem.common.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class OnDeathAfterPossessionCriterion extends AbstractCriterion<OnDeathAfterPossessionCriterion.Conditions> {
    private final Identifier id;

    public OnDeathAfterPossessionCriterion(Identifier id) {
        this.id = id;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(
            this.id,
            playerPredicate,
            EntityPredicate.Extended.getInJson(obj, "entity", predicateDeserializer),
            DamageSourcePredicate.fromJson(obj.get("killing_blow")),
            JsonHelper.getBoolean(obj, "seppukku", false)
        );
    }

    public void handle(ServerPlayerEntity player, Entity entity, DamageSource deathCause) {
        this.test(player, (conditions) -> conditions.test(player, entity, deathCause));
    }

    @Override
    public Identifier getId() {
        return this.id;
    }


    public static class Conditions extends AbstractCriterionConditions {
        private final EntityPredicate.Extended entity;
        private final DamageSourcePredicate killingBlow;
        private final boolean seppukku;

        public Conditions(Identifier id, EntityPredicate.Extended player, EntityPredicate.Extended entity, DamageSourcePredicate killingBlow, boolean seppukku) {
            super(id, player);
            this.entity = entity;
            this.killingBlow = killingBlow;
            this.seppukku = seppukku;
        }

        public boolean test(ServerPlayerEntity player, Entity entity, DamageSource killingBlow) {
            LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, entity);
            return this.killingBlow.test(player, killingBlow)
                && this.entity.test(lootContext)
                && (!seppukku || killingBlow.getAttacker() == entity);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("entity", this.entity.toJson(predicateSerializer));
            jsonObject.add("killing_blow", this.killingBlow.toJson());
            jsonObject.addProperty("seppukku", this.seppukku);
            return jsonObject;
        }
    }
}
