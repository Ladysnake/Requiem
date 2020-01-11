/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
 */
package ladysnake.requiem.mixin.client.render.model;

import ladysnake.requiem.Requiem;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelItemOverride;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {

    @Unique
    private static final Identifier ENCHANTED_BOOK_ID = new Identifier("item/enchanted_book");

    @ModifyVariable(method = "loadModelFromJson", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;deserialize(Ljava/io/Reader;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;"))
    private JsonUnbakedModel addEnchantedBookOverride(JsonUnbakedModel model, Identifier id) {
        if (ENCHANTED_BOOK_ID.equals(id)) {
            Map<Identifier, Float> minPropertyValues = new HashMap<>();
            minPropertyValues.put(Requiem.id("humanity"), 1F);
            model.getOverrides().add(new ModelItemOverride(Requiem.id("item/humanity_enchanted_book"), minPropertyValues));
        }
        return model;
    }
}
