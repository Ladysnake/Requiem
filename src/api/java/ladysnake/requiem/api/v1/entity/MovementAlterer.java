/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
package ladysnake.requiem.api.v1.entity;

import net.minecraft.util.math.Vec3d;

import javax.annotation.CheckForNull;

/**
 * A {@link MovementAlterer} alters the movement of an {@link net.minecraft.entity.Entity}
 * according to a {@link MovementConfig}.
 */
public interface MovementAlterer {

    void setConfig(@CheckForNull MovementConfig config);

    void applyConfig();

    void update();

    /**
     * Gets the acceleration that this entity has underwater.
     *
     * @param baseAcceleration the default acceleration computed in {@link net.minecraft.entity.LivingEntity#travel(Vec3d)}
     * @return the modified acceleration
     */
    float getSwimmingAcceleration(float baseAcceleration);

    boolean canClimbWalls();
}
