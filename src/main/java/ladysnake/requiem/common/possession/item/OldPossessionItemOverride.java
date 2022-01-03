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
package ladysnake.requiem.common.possession.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.item.PossessionItemAction;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.core.data.LazyEntityPredicate;
import ladysnake.requiem.core.data.LazyItemPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Optional;

public record OldPossessionItemOverride(
    OldPossessionItemOverride.Requirements requirements,
    int useTime,
    OldPossessionItemOverride.Result result
) implements InstancedItemOverride, PossessionItemOverride {
    public static final Identifier ID = Requiem.id("override_v0");

    static Codec<OldPossessionItemOverride> codec(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.create((instance) -> instance.group(
            Requirements.codec(jsonCodec).fieldOf("requirements").forGetter(OldPossessionItemOverride::requirements),
            Codec.INT.optionalFieldOf("use_time", 0).forGetter(OldPossessionItemOverride::useTime),
            Result.CODEC.fieldOf("result").forGetter(OldPossessionItemOverride::result)
        ).apply(instance, OldPossessionItemOverride::new));
    }

    /**
     * Initializes this object's lazy fields
     */
    @Override
    public void initNow() {
        this.requirements.initNow();
    }

    @Override
    public Identifier getType() {
        return ID;
    }

    @Override
    public Optional<InstancedItemOverride> test(PlayerEntity player, MobEntity possessed, ItemStack stack) {
        return this.requirements.test(player, possessed, stack) ? Optional.of(this) : Optional.empty();
    }

    @Override
    public boolean shortCircuits() {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
        if (this.useTime() <= 0) {
            return this.result.run(player, possessedEntity, heldStack, world, hand);
        } else {
            OverridableItemStack.overrideMaxUseTime(heldStack, this.useTime());
            player.setCurrentHand(hand);
            return TypedActionResult.success(heldStack);
        }
    }

    @Override
    public TypedActionResult<ItemStack> finishUsing(PlayerEntity user, MobEntity possessedEntity, ItemStack heldStack, World world, Hand activeHand) {
        return this.result.run(user, possessedEntity, heldStack, world, activeHand);
    }

    public static class Requirements {
        public static Codec<Requirements> codec(Codec<JsonElement> jsonCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(
                LazyEntityPredicate.codec(jsonCodec).optionalFieldOf("possessed", LazyEntityPredicate.ANY).forGetter(o -> o.possessed),
                LazyItemPredicate.codec(jsonCodec).optionalFieldOf("used_item", LazyItemPredicate.ANY).forGetter(o -> o.usedItem),
                Codec.BOOL.optionalFieldOf("can_eat").forGetter(o -> o.canEat)
            ).apply(instance, Requirements::new));
        }

        final LazyEntityPredicate possessed;
        private final LazyItemPredicate usedItem;
        private final Optional<Boolean> canEat;

        public Requirements(LazyEntityPredicate possessed, LazyItemPredicate usedItem, Optional<Boolean> canEat) {
            this.possessed = possessed;
            this.usedItem = usedItem;
            this.canEat = canEat;
        }

        public boolean test(PlayerEntity player, MobEntity possessed, ItemStack stack) {
            if (!this.usedItem.test(possessed.world, stack)) {
                return false;
            }
            if (!this.possessed.test(possessed)) {
                return false;
            }
            if (this.canEat.isPresent()) {
                FoodComponent foodComponent = stack.getItem().getFoodComponent();
                return this.canEat.get() == (foodComponent != null && player.canConsume(foodComponent.isAlwaysEdible()));
            }
            return true;
        }

        private void initNow() {
            this.possessed.initNow();
            this.usedItem.initNow();
        }

    }

    public static class Result {
        public static final Codec<Result> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RequiemRegistries.MOB_ACTIONS.getCodec().fieldOf("action").forGetter(Result::getAction),
            Codec.INT.optionalFieldOf("cooldown", 0).forGetter(Result::getCooldown)
            ).apply(instance, Result::new)
        );

        private final PossessionItemAction action;
        private final int cooldown;

        public Result(PossessionItemAction action, int cooldown) {
            this.action = action;
            this.cooldown = cooldown;
        }

        public TypedActionResult<ItemStack> run(PlayerEntity player, MobEntity possessedEntity, ItemStack stack, World world, Hand hand) {
            Item item = stack.getItem();    // get item beforehand, after interaction it will be empty
            TypedActionResult<ItemStack> result = this.getAction().interact(player, possessedEntity, stack, world, hand);
            if (result.getResult().isAccepted()) {
                if (this.getCooldown() > 0) {
                    player.getItemCooldownManager().set(item, this.getCooldown());
                }
            }
            return result;
        }

        public PossessionItemAction getAction() {
            return action;
        }

        public int getCooldown() {
            return cooldown;
        }
    }
}
