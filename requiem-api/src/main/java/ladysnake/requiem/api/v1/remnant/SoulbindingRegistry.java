/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.remnant;

import ladysnake.requiem.api.v1.internal.ApiInternals;
import net.minecraft.entity.effect.StatusEffect;

public interface SoulbindingRegistry {
    static SoulbindingRegistry instance() {
        return ApiInternals.getSoulbindingRegistry();
    }

    /**
     * Registers a {@code StatusEffect} as soulbound.
     * @see #isSoulbound(StatusEffect)
     */
    void registerSoulbound(StatusEffect effect);

    /**
     * Returns {@code true} if the given {@link StatusEffect} is registered as
     * soulbound.
     *
     * <p> Soulbound status effects are carried over when the player dies,
     * and stay with the soul when leaving a body. They also have custom backgrounds in the
     * inventory and in-game hud.
     *
     * @param effect status effect to test
     * @return {@code true} if {@code effect} is soulbound
     * @see #registerSoulbound(StatusEffect)
     */
    boolean isSoulbound(StatusEffect effect);
}
