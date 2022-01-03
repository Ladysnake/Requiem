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
package ladysnake.requiem.api.v1.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;

@API(status = API.Status.EXPERIMENTAL)
public interface CurableEntityComponent extends Component {
    ComponentKey<CurableEntityComponent> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "curable"), CurableEntityComponent.class);

    boolean hasBeenCured();

    void setCured();

    /**
     * @return {@code true} if the entity can be assimilated by a vagrant soul to make a human body
     */
    boolean canBeAssimilated();

    /**
     *
     * @return {@code true} if the entity can be cured into another entity
     */
    boolean canBeCured();

    @Nullable MobEntity cure();

}
