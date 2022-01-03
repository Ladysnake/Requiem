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
package ladysnake.requiem.common.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.DamagePredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PossessedHitEntityCriterion extends AbstractCriterion<PossessedHitEntityCriterion.Conditions> {
    private final Identifier id;

    public PossessedHitEntityCriterion(Identifier id) {
        this.id = id;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Conditions(
            this.id,
            playerPredicate,
            DamagePredicate.fromJson(obj.get("damage")),
            EntityPredicate.Extended.getInJson(obj, "possessed", predicateDeserializer),
            EntityPredicate.Extended.getInJson(obj, "entity", predicateDeserializer)
        );
    }

    public void handle(ServerPlayerEntity player, Entity possessed, Entity entity, DamageSource source, float dealt, float taken, boolean blocked) {
        this.trigger(player, (conditions) -> conditions.test(player, source, dealt, taken, blocked, possessed, entity));
    }

    @Override
    public Identifier getId() {
        return this.id;
    }


    public static class Conditions extends AbstractCriterionConditions {
        private final DamagePredicate damage;
        private final EntityPredicate.Extended possessed;
        private final EntityPredicate.Extended entity;

        public Conditions(Identifier id, EntityPredicate.Extended player, DamagePredicate damage, EntityPredicate.Extended possessed, EntityPredicate.Extended entity) {
            super(id, player);
            this.damage = damage;
            this.possessed = possessed;
            this.entity = entity;
        }

        public boolean test(ServerPlayerEntity player, DamageSource source, float dealt, float taken, boolean blocked, Entity possessed, Entity target) {
            if (this.damage.test(player, source, dealt, taken, blocked)) {
                LootContext possessedCtx = EntityPredicate.createAdvancementEntityLootContext(player, possessed);
                LootContext targetCtx = EntityPredicate.createAdvancementEntityLootContext(player, target);
                return this.possessed.test(possessedCtx) && this.entity.test(targetCtx);
            }
            return false;
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("damage", this.damage.toJson());
            jsonObject.add("possessed", this.possessed.toJson(predicateSerializer));
            jsonObject.add("entity", this.entity.toJson(predicateSerializer));
            return jsonObject;
        }
    }
}
