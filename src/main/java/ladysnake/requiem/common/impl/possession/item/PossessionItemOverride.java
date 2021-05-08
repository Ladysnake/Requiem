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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.api.v1.possession.item.PossessionItemAction;
import ladysnake.requiem.common.RequiemRegistries;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class PossessionItemOverride implements Comparable<PossessionItemOverride> {
    private static final Codec<JsonElement> JSON_CODEC = Codec.PASSTHROUGH.xmap(d -> d.convert(JsonOps.INSTANCE).getValue(), json -> new Dynamic<>(JsonOps.INSTANCE));
    private static final Codec<PossessionItemOverride> CODEC_V0 = RecordCodecBuilder.create((instance) -> instance.group(
        Codec.INT.optionalFieldOf("priority", 100).forGetter(PossessionItemOverride::getPriority),
        Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PossessionItemOverride::isEnabled),
        JSON_CODEC.optionalFieldOf("possessed", null).xmap(EntityPredicate::fromJson, EntityPredicate::toJson).forGetter(PossessionItemOverride::getPossessed),
        JSON_CODEC.optionalFieldOf("used_item", null).xmap(ItemPredicate::fromJson, ItemPredicate::toJson).forGetter(PossessionItemOverride::getUsedItem),
        Codec.INT.optionalFieldOf("use_time", 0).forGetter(PossessionItemOverride::getUseTime),
        Result.CODEC.fieldOf("result").forGetter(PossessionItemOverride::getResult)
    ).apply(instance, PossessionItemOverride::new));

    private static final int CURRENT_SCHEMA_VERSION = 0;

    public static final Codec<PossessionItemOverride> CODEC = Codec.PASSTHROUGH.flatXmap(
        d -> {
            int schemaVersion = d.get("schema_version").asInt(-1);
            if (schemaVersion == 0) {
                return CODEC_V0.parse(d);
            } else if (schemaVersion == -1) {
                return DataResult.error("Missing schema version");
            } else {
                return DataResult.error(String.format("Invalid/Unsupported schema version \"%s\" was found", schemaVersion));
            }
        },
        d -> CODEC_V0.encodeStart(JsonOps.INSTANCE, d).map(j -> {
            j.getAsJsonObject().addProperty("schema_version", CURRENT_SCHEMA_VERSION);
            return new Dynamic<>(JsonOps.INSTANCE, j);
        })
    );

    private final int priority;
    private final boolean enabled;
    private final EntityPredicate possessed;
    private final ItemPredicate usedItem;
    private final int useTime;
    private final Result result;

    private PossessionItemOverride(int priority, boolean enabled, EntityPredicate possessed, ItemPredicate usedItem, int useTime, Result result) {
        this.priority = priority;
        this.enabled = enabled;
        this.possessed = possessed;
        this.usedItem = usedItem;
        this.useTime = useTime;
        this.result = result;
    }

    public static Optional<PossessionItemOverride> findOverride(World world, MobEntity possessedEntity, ItemStack heldStack) {
        return world.getRegistryManager().get(RequiemRegistries.MOB_ITEM_OVERRIDE_KEY).stream()
            .sorted()
            .filter(override -> override.test(possessedEntity, heldStack))
            .findFirst();
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public EntityPredicate getPossessed() {
        return possessed;
    }

    public ItemPredicate getUsedItem() {
        return usedItem;
    }

    public int getUseTime() {
        return useTime;
    }

    public Result getResult() {
        return result;
    }

    public boolean test(MobEntity possessed, ItemStack stack) {
        return this.isEnabled() && this.getUsedItem().test(stack) && this.testPossessed(possessed);
    }

    private boolean testPossessed(MobEntity possessed) {
        if (!possessed.world.isClient) {
            return this.getPossessed().test((ServerWorld) possessed.world, null, possessed);
        } else {
            // We still need to have some idea of whether we can use an item clientside
            // Thankfully, most tests will never use the server world, so we can just pass null and pray
            try {
                return this.getPossessed().test(null/*Possible NPE*/, null, possessed);
            } catch (NullPointerException npe) {
                // We will have to check this serverside
                return true;
            }
        }
    }

    @Override
    public int compareTo(@NotNull PossessionItemOverride o) {
        return o.priority - this.priority;
    }

    public TypedActionResult<ItemStack> runAction(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
        return this.result.run(player, possessedEntity, heldStack, world, hand);
    }

    public static class Result {
        public static final Codec<Result> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RequiemRegistries.MOB_ACTIONS.fieldOf("action").forGetter(Result::getAction),
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
            TypedActionResult<ItemStack> result = this.getAction().interact(player, possessedEntity, stack, world, hand);
            if (result.getResult().isAccepted()) {
                if (this.getCooldown() > 0) {
                    player.getItemCooldownManager().set(stack.getItem(), this.getCooldown());
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
