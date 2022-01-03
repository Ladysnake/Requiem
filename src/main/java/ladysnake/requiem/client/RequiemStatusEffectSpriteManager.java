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
package ladysnake.requiem.client;


import ladysnake.requiem.mixin.client.attrition.SpriteAtlasHolderAccessor;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class RequiemStatusEffectSpriteManager implements ClientSpriteRegistryCallback {
    private final Map<Identifier, Identifier[]> spriteMappings = new HashMap<>();

    public void registerAltSprites(StatusEffect effect, int altSpriteCount) {
        Identifier baseId = net.minecraft.util.registry.Registry.STATUS_EFFECT.getId(effect);
        if (baseId == null) throw new IllegalStateException("Unregistered status effect " + effect);
        Identifier[] altSprites = new Identifier[altSpriteCount];
        for (int amplifier = 0; amplifier < altSpriteCount; amplifier++) {
            altSprites[amplifier] = new Identifier(baseId.getNamespace(), "mob_effect/" + baseId.getPath() + '_' + (amplifier + 1));
        }
        spriteMappings.put(new Identifier(baseId.getNamespace(), "mob_effect/" + baseId.getPath()), altSprites);
    }

    public Sprite substituteSprite(Sprite baseSprite, StatusEffectInstance renderedEffect) {
        Identifier[] altSprites = spriteMappings.get(baseSprite.getId());
        if (altSprites != null) {
            int amplifier = renderedEffect.getAmplifier();
            return ((SpriteAtlasHolderAccessor) MinecraftClient.getInstance().getStatusEffectSpriteManager())
                .requiem$getAtlas().getSprite(altSprites[Math.min(altSprites.length - 1, amplifier)]);
        }
        return baseSprite;
    }

    public void registerCallbacks() {
        // Register special icons for different levels of attrition
        ClientSpriteRegistryCallback.event(new Identifier("textures/atlas/mob_effects.png")).register(this);
    }

    @Override
    public void registerSprites(SpriteAtlasTexture atlasTexture, ClientSpriteRegistryCallback.Registry registry) {
        for (Identifier[] altSprites : spriteMappings.values()) {
            for (Identifier altSprite : altSprites) {
                registry.register(altSprite);
            }
        }
    }
}
