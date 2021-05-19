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
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.RequiemRegistries;
import ladysnake.requiem.common.util.MoreCodecs;
import ladysnake.requiem.common.util.PolymorphicCodecBuilder;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class PossessionItemOverrideWrapper implements Comparable<PossessionItemOverrideWrapper> {
    public static final int CURRENT_SCHEMA_VERSION = 0;

    // the fun bit is that V0 is both a schema version and an override type, so we need to flatten some stuff here
    public static final Codec<PossessionItemOverrideWrapper> CODEC_V0 = RecordCodecBuilder.create((instance) -> instance.group(
        Codec.INT.optionalFieldOf("priority", 100).forGetter(PossessionItemOverrideWrapper::getPriority),
        Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PossessionItemOverrideWrapper::isEnabled),
        MoreCodecs.text(MoreCodecs.DYNAMIC_JSON).optionalFieldOf("tooltip").forGetter(w -> ((OldPossessionItemOverride) w.override).getTooltip()),
        OldPossessionItemOverride.Requirements.codec(MoreCodecs.DYNAMIC_JSON).fieldOf("requirements").forGetter(o -> ((OldPossessionItemOverride) o.override).getRequirements()),
        Codec.INT.optionalFieldOf("use_time", 0).forGetter(w -> ((OldPossessionItemOverride) w.override).getUseTime()),
        OldPossessionItemOverride.Result.CODEC.fieldOf("result").forGetter(w -> ((OldPossessionItemOverride) w.override).getResult())
    ).apply(instance, (p, e, t, req, u, res) -> new PossessionItemOverrideWrapper(p, e, new OldPossessionItemOverride(t, req, u, res))));

    public static final Codec<PossessionItemOverrideWrapper> CODEC_V1 = codecV1(MoreCodecs.DYNAMIC_JSON);

    public static final Codec<PossessionItemOverrideWrapper> CODEC = PolymorphicCodecBuilder.create("schema_version", Codec.INT, (PossessionItemOverrideWrapper o) -> CURRENT_SCHEMA_VERSION)
        .with(0, CODEC_V0)
        .with(1, CODEC_V1)
        .build()
        .xmap(PossessionItemOverrideWrapper::initNow, Function.identity());

    // The compressed NBT codec used by PacketByteBuf#encode fails on nulls, so we cannot use regular JSON objects
    public static final Codec<PossessionItemOverrideWrapper> NETWORK_CODEC = codecV1(MoreCodecs.STRING_JSON);

    private static Codec<PossessionItemOverrideWrapper> codecV1(Codec<JsonElement> jsonCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("priority", 100).forGetter(PossessionItemOverrideWrapper::getPriority),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(PossessionItemOverrideWrapper::isEnabled),
            overrideCodecV1(jsonCodec).fieldOf("override").forGetter(w -> w.override)
        ).apply(instance, PossessionItemOverrideWrapper::new));
    }

    private static Codec<PossessionItemOverride> overrideCodecV1(Codec<JsonElement> jsonCodec) {
        return PolymorphicCodecBuilder.create("type", Identifier.CODEC, PossessionItemOverride::getType)
            .with(OldPossessionItemOverride.ID, OldPossessionItemOverride.codec(jsonCodec))
            .with(DietItemOverride.ID, DietItemOverride.codec(jsonCodec))
            .build();
    }

    public PossessionItemOverrideWrapper(int priority, boolean enabled, PossessionItemOverride override) {
        this.priority = priority;
        this.enabled = enabled;
        this.override = override;
    }

    public static Optional<TypedActionResult<ItemStack>> tryUseOverride(World world, PlayerEntity player, ItemStack heldStack, Hand hand) {
        OverridableItemStack.get(heldStack).requiem$clearOverriddenUseTime();

        MobEntity possessedEntity = PossessionComponent.getPossessedEntity(player);
        if (possessedEntity != null) {
            return PossessionItemOverrideWrapper.findOverride(world, player, possessedEntity, heldStack)
                .map(override -> override.use(player, possessedEntity, heldStack, world, hand))
                .filter(res -> res.getResult() != ActionResult.PASS);
        }

        return Optional.empty();
    }

    public static Optional<TypedActionResult<ItemStack>> tryFinishUsingOverride(World world, PlayerEntity player, ItemStack heldStack, Hand hand) {
        OverridableItemStack.get(heldStack).requiem$clearOverriddenUseTime();

        MobEntity possessedEntity = PossessionComponent.getPossessedEntity(player);
        if (possessedEntity != null) {
            return PossessionItemOverrideWrapper.findOverride(world, player, possessedEntity, heldStack)
                .map(override -> override.finishUsing(player, possessedEntity, heldStack, world, hand))
                .filter(res -> res.getResult() != ActionResult.PASS);
        }

        return Optional.empty();
    }

    public static Optional<InstancedItemOverride> findOverride(World world, PlayerEntity player, MobEntity possessedEntity, ItemStack heldStack) {
        return world.getRegistryManager().get(RequiemRegistries.MOB_ITEM_OVERRIDE_KEY).stream()
            .sorted()
            .reduce(
                Optional.empty(),     // identity
                (previousResult, override) -> { // Accumulator, converts wrappers into partial results
                    if (previousResult.filter(InstancedItemOverride::shortCircuits).isPresent()) return previousResult;
                    Optional<InstancedItemOverride> tested = override.test(player, possessedEntity, heldStack);
                    if (!previousResult.isPresent() || tested.filter(InstancedItemOverride::shortCircuits).isPresent()) {
                        return tested;
                    }
                    return previousResult;
                }, (r1, r2) -> {    // Combiner, combines partial results
                    if (r1.filter(InstancedItemOverride::shortCircuits).isPresent()) return r1;
                    if (!r1.isPresent() || r2.filter(InstancedItemOverride::shortCircuits).isPresent()) return r2;
                    return r1;
                });
    }

    private final int priority;
    private final boolean enabled;
    final PossessionItemOverride override;

    public int getPriority() {
        return this.priority;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Optional<InstancedItemOverride> test(PlayerEntity player, MobEntity host, ItemStack stack) {
        return this.isEnabled() ? this.override.test(player, host, stack) : Optional.empty();
    }

    public PossessionItemOverrideWrapper initNow() {
        this.override.initNow();
        return this;
    }

    @Override
    public int compareTo(@NotNull PossessionItemOverrideWrapper o) {
        return o.priority - this.priority;
    }
}
