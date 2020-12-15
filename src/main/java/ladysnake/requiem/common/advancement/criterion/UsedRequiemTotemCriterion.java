package ladysnake.requiem.common.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.UsedTotemCriterion;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UsedRequiemTotemCriterion extends AbstractCriterion<UsedRequiemTotemCriterion.Conditions> {
    private final Identifier id;

    public UsedRequiemTotemCriterion(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return this.id;
    }

    public UsedRequiemTotemCriterion.Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended extended, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        return new UsedRequiemTotemCriterion.Conditions(this.id, extended, itemPredicate);
    }

    public void trigger(ServerPlayerEntity player, ItemStack stack) {
        this.test(player, (conditions) -> conditions.matches(stack));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final ItemPredicate item;

        public Conditions(Identifier id, EntityPredicate.Extended player, ItemPredicate item) {
            super(id, player);
            this.item = item;
        }

        public static UsedTotemCriterion.Conditions create(ItemConvertible item) {
            return new UsedTotemCriterion.Conditions(EntityPredicate.Extended.EMPTY, ItemPredicate.Builder.create().item(item).build());
        }

        public boolean matches(ItemStack stack) {
            return this.item.test(stack);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("item", this.item.toJson());
            return jsonObject;
        }
    }
}
