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
package ladysnake.requiem.core.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public record FoodComponentPredicate(
    NumberRange.IntRange hunger,
    NumberRange.FloatRange saturationModifier,
    @Nullable Boolean meat,
    @Nullable Boolean alwaysEdible,
    @Nullable Boolean snack
) {
    public static final FoodComponentPredicate ANY = new FoodComponentPredicate(NumberRange.IntRange.ANY, NumberRange.FloatRange.ANY, null, null, null);

    public static FoodComponentPredicate fromJson(@Nullable JsonElement json) {
        if (json != null && !json.isJsonNull()) {
            JsonObject jsonObject = JsonHelper.asObject(json, "requiem:food");
            return new FoodComponentPredicate(
                NumberRange.IntRange.fromJson(jsonObject.get("hunger")),
                NumberRange.FloatRange.fromJson(jsonObject.get("saturation")),
                jsonObject.has("meat") ? JsonHelper.getBoolean(jsonObject, "meat") : null,
                jsonObject.has("always_edible") ? JsonHelper.getBoolean(jsonObject, "always_edible") : null,
                jsonObject.has("snack") ? JsonHelper.getBoolean(jsonObject, "snack") : null
            );
        }
        return ANY;
    }

    public boolean test(ItemStack stack) {
        if (this == ANY) return true;

        FoodComponent foodComponent = stack.getItem().getFoodComponent();
        if (foodComponent == null) {
            return false;
        } else if (!this.hunger.test(foodComponent.getHunger())) {
            return false;
        } else if (!this.saturationModifier.test(foodComponent.getSaturationModifier())) {
            return false;
        } else if (this.meat != null && this.meat != foodComponent.isMeat()) {
            return false;
        } else if (this.alwaysEdible != null && this.alwaysEdible != foodComponent.isAlwaysEdible()) {
            return false;
        } else if (this.snack != null && this.snack != foodComponent.isSnack()) {
            return false;
        }
        return true;
    }
}
