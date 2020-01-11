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
package ladysnake.requiem.common.impl.remnant;

import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import net.minecraft.entity.effect.StatusEffect;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SoulbindingRegistryImpl implements SoulbindingRegistry {
    private final Set<StatusEffect> soulboundEffects = new LinkedHashSet<>();

    @Override
    public void registerSoulbound(StatusEffect effect) {
        soulboundEffects.add(effect);
    }

    @Override
    public boolean isSoulbound(StatusEffect effect) {
        return soulboundEffects.contains(effect);
    }
}
