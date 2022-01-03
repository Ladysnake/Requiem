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
package ladysnake.requiem.api.v1.event.requiem;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public interface HumanityCheckCallback {
    /**
     * Called to obtain the humanity level of a possessed mob.
     *
     * <p> Humanity makes mobs behave more like players. The higher the humanity level,
     * the less special possession gameplay becomes. The actual humanity level
     * is the maximum of the levels returned by all registered {@code HumanityCheckCallback}.
     *
     * <p> By default, humanity is provided by the Humanity enchantment,
     * and has 2 levels. Level 1 makes mobs drop items as if they were killed by the possessor,
     * and level 2 additionally makes mobs drop XP.
     *
     * @return the humanity level of the mob, according to this callback
     */
    int getHumanityLevel(LivingEntity possessedEntity);

    Event<HumanityCheckCallback> EVENT = EventFactory.createArrayBacked(HumanityCheckCallback.class,
        (listeners) -> (possessed) -> {
            int ret = 0;
            for (HumanityCheckCallback listener : listeners) {
                int result = listener.getHumanityLevel(possessed);
                if (ret < result) {
                    ret = result;
                }
            }
            return ret;
        });
}
