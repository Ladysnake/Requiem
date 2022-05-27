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
package ladysnake.requiem.mixin.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import ladysnake.requiem.Requiem;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LocationPredicate.class)
public abstract class LocationPredicateMixin {
    private @Nullable TagKey<Biome> requiem$biomeTag;

    /**
     * For... reasons, we run tests on clients with a null server world. This does not work well with {@code LocationPredicate}s, so we skip all the world checks.
     */
    @Inject(method = "test", at = @At(value = "FIELD", target = "Lnet/minecraft/predicate/entity/LocationPredicate;dimension:Lnet/minecraft/util/registry/RegistryKey;", ordinal = 0), cancellable = true)
    private void cancelTestOnClients(@Nullable ServerWorld world, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (world == null) cir.setReturnValue(true);
    }

    @Inject(method = "test", at = @At("RETURN"), cancellable = true)
    private void test(ServerWorld world, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && this.requiem$biomeTag != null) {
            BlockPos blockPos = new BlockPos(x, y, z);

            if (!world.getBiome(blockPos).isIn(this.requiem$biomeTag)) {
                cir.setReturnValue(false);
            }
        }
    }

    // ANY return is actually an early return in the bytecode
    @Inject(method = "fromJson", at = @At(value = "RETURN", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void fromJson(JsonElement json, CallbackInfoReturnable<LocationPredicate> cir, JsonObject locationData) {
        JsonElement biomeCategory;
        if (locationData.has("requiem:biome_tag")) {
            biomeCategory = locationData.get("requiem:biome_tag");
        } else {
            if (locationData.has("requiem$biome_category") || locationData.has("requiem:biome_category")) {
                Requiem.LOGGER.error("[Requiem] Unsupported biome category extension on LocationPredicate, please switch to requiem:biome_tag");
            }
            return;
        }
        //noinspection ConstantConditions
        ((LocationPredicateMixin) (Object) cir.getReturnValue()).requiem$biomeTag
            = TagKey.identifierCodec(Registry.BIOME_KEY).parse(JsonOps.INSTANCE, biomeCategory)
            .getOrThrow(false, msg -> Requiem.LOGGER.error("[Requiem] Failed to parse biome_tag extension to LocationPredicate: {}", msg));
    }
}
