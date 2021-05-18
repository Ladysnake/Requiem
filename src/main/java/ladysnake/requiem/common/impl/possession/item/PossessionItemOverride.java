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

import com.google.gson.Gson;
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
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public final class PossessionItemOverride implements Comparable<PossessionItemOverride> {
    private static final Gson GSON = new Gson();
    private static final Codec<JsonElement> DYNAMIC_JSON_CODEC = Codec.PASSTHROUGH.comapFlatMap(
        dynamic -> DataResult.success(dynamic.convert(JsonOps.INSTANCE).getValue()),
        json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );
    private static final Codec<JsonElement> STRING_JSON_CODEC = Codec.STRING.xmap(
        str -> GSON.fromJson(str, JsonElement.class),
        GSON::toJson
    );
    // The compressed NBT codec used by PacketByteBuf#encode fails on nulls, so we cannot use regular JSON objects
    public static final Codec<PossessionItemOverride> NETWORK_CODEC = codecV0(STRING_JSON_CODEC);
    private static final Codec<PossessionItemOverride> CODEC_V0 = codecV0(DYNAMIC_JSON_CODEC);

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
    ).xmap(PossessionItemOverride::initNow, Function.identity());

    private static Codec<PossessionItemOverride> codecV0(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.create((instance) -> instance.group(
            Codec.INT.optionalFieldOf("priority", 100).forGetter(PossessionItemOverride::getPriority),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PossessionItemOverride::isEnabled),
            jsonCodec.optionalFieldOf("tooltip").<Optional<Text>>xmap(o -> o.map(Text.Serializer::fromJson), txt -> txt.map(Text.Serializer::toJsonTree)).forGetter(PossessionItemOverride::getTooltip),
            Requirements.codec(jsonCodec).fieldOf("requirements").forGetter(o -> o.requirements),
            Codec.INT.optionalFieldOf("use_time", 0).forGetter(PossessionItemOverride::getUseTime),
            Result.CODEC.fieldOf("result").forGetter(PossessionItemOverride::getResult)
        ).apply(instance, PossessionItemOverride::new));
    }

    private final int priority;
    private final boolean enabled;
    private final Optional<Text> tooltip;
    private final Requirements requirements;
    private final int useTime;
    private final Result result;

    private PossessionItemOverride(int priority, boolean enabled, Optional<Text> tooltip, Requirements requirements, int useTime, Result result) {
        this.priority = priority;
        this.enabled = enabled;
        this.requirements = requirements;
        this.useTime = useTime;
        this.result = result;
        this.tooltip = tooltip;
    }

    public static Optional<PossessionItemOverride> findOverride(World world, PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack) {
        return world.getRegistryManager().get(RequiemRegistries.MOB_ITEM_OVERRIDE_KEY).stream()
            .sorted()
            .filter(override -> override.test(player, possessedEntity, heldStack))
            .findFirst();
    }

    /**
     * Initializes this object's lazy fields
     */
    private PossessionItemOverride initNow() {
        this.requirements.initNow();
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Optional<Text> getTooltip() {
        return tooltip;
    }

    public int getUseTime() {
        return useTime;
    }

    public Result getResult() {
        return result;
    }

    public boolean test(PlayerEntity player, MobEntity possessed, ItemStack stack) {
        return this.isEnabled() && this.requirements.test(player, possessed, stack);
    }

    @Override
    public int compareTo(@NotNull PossessionItemOverride o) {
        return o.priority - this.priority;
    }

    public TypedActionResult<ItemStack> runAction(PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack, World world, Hand hand) {
        return this.result.run(player, possessedEntity, heldStack, world, hand);
    }

    public static class Requirements {
        public static Codec<Requirements> codec(Codec<JsonElement> jsonCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(
                jsonCodec.optionalFieldOf("possessed", null).forGetter(o -> o.possessedJson),
                jsonCodec.optionalFieldOf("used_item", null).forGetter(o -> o.usedItemJson),
                Codec.BOOL.optionalFieldOf("can_eat").forGetter(o -> o.canEat)
            ).apply(instance, Requirements::new));
        }

        private final JsonElement possessedJson;
        private final JsonElement usedItemJson;
        private @Nullable EntityPredicate possessed;
        private @Nullable ItemPredicate usedItem;
        private final Optional<Boolean> canEat;

        public Requirements(JsonElement possessedJson, JsonElement usedItemJson, Optional<Boolean> canEat) {
            this.possessedJson = possessedJson;
            this.usedItemJson = usedItemJson;
            this.canEat = canEat;
        }

        public boolean test(PlayerEntity player, MobEntity possessed, ItemStack stack) {
            if (!this.getUsedItem(possessed.world).test(stack)) {
                return false;
            }
            if (!this.testPossessed(possessed)) {
                return false;
            }
            if (this.canEat.isPresent()) {
                FoodComponent foodComponent = stack.getItem().getFoodComponent();
                if (this.canEat.get() != (foodComponent != null && player.canConsume(foodComponent.isAlwaysEdible()))) {
                    return false;
                }
            }
            return true;
        }

        private void initNow() {
            this.possessed = EntityPredicate.fromJson(this.possessedJson);
            this.usedItem = ItemPredicate.fromJson(this.usedItemJson);
        }

        private EntityPredicate getPossessed(World world) {
            if (this.possessed == null) {
                // fromJson references the server tag manager singleton, which is not set on the client
                if (world.isClient) ServerTagManagerHolder.setTagManager(world.getTagManager());
                this.possessed = EntityPredicate.fromJson(this.possessedJson);
            }
            return this.possessed;
        }

        private ItemPredicate getUsedItem(World world) {
            if (this.usedItem == null) {
                // fromJson references the server tag manager singleton, which is not set on the client
                if (world.isClient) ServerTagManagerHolder.setTagManager(world.getTagManager());
                this.usedItem = ItemPredicate.fromJson(this.usedItemJson);
            }
            return this.usedItem;
        }

        private boolean testPossessed(MobEntity possessed) {
            if (!possessed.world.isClient) {
                return this.getPossessed(possessed.world).test((ServerWorld) possessed.world, null, possessed);
            } else {
                // We still need to have some idea of whether we can use an item clientside
                // Thankfully, most tests will never use the server world, so we can just pass null and pray
                try {
                    return this.getPossessed(possessed.world).test(null/*Possible NPE*/, null, possessed);
                } catch (NullPointerException npe) {
                    // We will have to check this serverside
                    return true;
                }
            }
        }
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
