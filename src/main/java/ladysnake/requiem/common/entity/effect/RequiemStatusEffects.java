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
package ladysnake.requiem.common.entity.effect;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.mixin.client.texture.SpriteAtlasHolderAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class RequiemStatusEffects {
    public static final StatusEffect ATTRITION = new AttritionStatusEffect(StatusEffectType.HARMFUL, 0xAA3322)
        .addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "069ae0b1-4014-41dd-932f-a5da4417d711", -0.2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

    public static void init() {
        registerEffect(ATTRITION, "attrition");
    }

    public static void registerEffect(StatusEffect effect, String name) {
        Registry.register(Registry.STATUS_EFFECT, Requiem.id(name), effect);
    }

    public static Sprite substituteSprite(Sprite baseSprite, StatusEffectInstance renderedEffect) {
        int amplifier = renderedEffect.getAmplifier();
        if (renderedEffect.getEffectType() == ATTRITION && amplifier < 4) {
            Identifier baseId = baseSprite.getId();
            return ((SpriteAtlasHolderAccessor) MinecraftClient.getInstance().getStatusEffectSpriteManager())
                .getAtlas().getSprite(new Identifier(baseId.getNamespace(), baseId.getPath() + '_' + (amplifier + 1)));
        }
        return baseSprite;
    }
}
