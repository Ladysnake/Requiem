/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.common.impl.possession.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.VanillaRequiemPlugin;
import ladysnake.requiem.common.impl.data.LazyEntityPredicate;
import ladysnake.requiem.common.impl.data.LazyItemPredicate;
import ladysnake.requiem.common.util.MoreCodecs;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Optional;

public class DietItemOverride implements PossessionItemOverride {
    public static final Identifier ID = Requiem.id("diet");

    public static Codec<DietItemOverride> codec(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            MoreCodecs.text(jsonCodec).optionalFieldOf("tooltip").forGetter(DietItemOverride::getTooltip),
            LazyEntityPredicate.codec(jsonCodec).fieldOf("entity").forGetter(o -> o.entity),
            LazyItemPredicate.codec(jsonCodec).fieldOf("food").forGetter(o -> o.food),
            MoreCodecs.enumeration(Filter.class).optionalFieldOf("filter", Filter.DEFAULT).forGetter(o -> o.filter)
        ).apply(instance, DietItemOverride::new));
    }

    private final Optional<Text> tooltip;
    private final LazyEntityPredicate entity;
    private final LazyItemPredicate food;
    private final Filter filter;
    private final Success success;
    private final Failure failure;

    public DietItemOverride(Optional<Text> tooltip, LazyEntityPredicate entity, LazyItemPredicate food, Filter filter) {
        this.tooltip = tooltip;
        this.entity = entity;
        this.food = food;
        this.filter = filter;
        success = new Success();
        failure = new Failure();
    }

    @Override
    public void initNow() {
        this.entity.initNow();
        this.food.initNow();
    }

    @Override
    public Identifier getType() {
        return ID;
    }

    @Override
    public Optional<InstancedItemOverride> test(PlayerEntity player, MobEntity possessed, ItemStack stack) {
        if (this.entity.test(possessed)) {
            if (this.food.get(possessed.world).test(stack)) {
                return Optional.of(this.success);
            } else if (stack.isFood()) {
                return Optional.of(this.failure);
            }
        }
        return Optional.empty();
    }

    public Optional<Text> getTooltip() {
        return tooltip;
    }

    public enum Filter {
        DEFAULT, REMOVE_HARMFUL_EFFECTS {
            @Override
            public TypedActionResult<ItemStack> consume(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
                return VanillaRequiemPlugin.eatWitchFood(player, possessedEntity, heldStack, world, hand);
            }
        };

        public TypedActionResult<ItemStack> consume(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
            return TypedActionResult.pass(heldStack);
        }
    }

    public class Success implements InstancedItemOverride {
        @Override
        public boolean shortCircuits() {
            return true;
        }

        @Override
        public TypedActionResult<ItemStack> use(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
            return TypedActionResult.pass(heldStack);
        }

        @Override
        public TypedActionResult<ItemStack> finishUsing(PlayerEntity user, MobEntity possessedEntity, ItemStack heldStack, World world, Hand activeHand) {
            return filter.consume(user, possessedEntity, heldStack, world, activeHand);
        }

        @Override
        public Optional<Text> getTooltip() {
            return DietItemOverride.this.tooltip;
        }
    }

    public class Failure implements InstancedItemOverride {
        private final Optional<Text> tooltip = DietItemOverride.this.tooltip.map(t -> t.shallowCopy().formatted(Formatting.STRIKETHROUGH));

        @Override
        public boolean shortCircuits() {
            return false;
        }

        @Override
        public TypedActionResult<ItemStack> use(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
            return TypedActionResult.fail(heldStack);
        }

        @Override
        public TypedActionResult<ItemStack> finishUsing(PlayerEntity user, MobEntity possessedEntity, ItemStack heldStack, World world, Hand activeHand) {
            return TypedActionResult.fail(heldStack);
        }

        @Override
        public Optional<Text> getTooltip() {
            return this.tooltip;
        }
    }
}
