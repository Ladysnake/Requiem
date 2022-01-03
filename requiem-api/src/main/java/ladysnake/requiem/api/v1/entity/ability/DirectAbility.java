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
package ladysnake.requiem.api.v1.entity.ability;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public interface DirectAbility<E extends LivingEntity, T extends Entity> extends MobAbility<E> {
    Identifier ABILITY_ICON = new Identifier("requiem", "textures/gui/ability_icon.png");

    /**
     * If the range is 0, the vanilla targeting system is used
     */
    double getRange();

    Class<T> getTargetType();

    boolean canTarget(T target);

    ActionResult trigger(T target);

    @CheckEnv(Env.CLIENT)
    default Identifier getIconTexture() {
        return ABILITY_ICON;
    }
}
