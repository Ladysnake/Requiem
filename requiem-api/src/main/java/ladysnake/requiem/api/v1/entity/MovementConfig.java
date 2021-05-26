/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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

import ladysnake.requiem.api.v1.entity.movement.SwimMode;
import ladysnake.requiem.api.v1.entity.movement.WalkMode;
import net.fabricmc.fabric.api.util.TriState;

public interface MovementConfig {
    /**
     * @return the flight mode applied to the entity
     * @see MovementMode
     */
    MovementMode getFlightMode();

    /**
     * Returns the gravity that should be applied by movement alterers using this config.
     * This will be applied on top of Minecraft's default gravity, which is of <tt>0.005</tt>.
     *
     * @return the gravity that should be applied by movement alterers using this config
     */
    float getAddedGravity();

    /**
     * Returns the speed modifier that should be applied during falls by movement alterers using this config.
     * The entity's vertical speed will be multiplied by this amount.
     * The modification is made after {@link #getAddedGravity()}.
     *
     * @return the speed modifier that should be applied during falls by movement alterers using this config
     */
    float getFallSpeedModifier();

    /**
     * Returns the speed modifier that should be applied while walking by movement alterers using this config.
     * The entity's ground speed will be multiplied by this amount.
     * The modification is applied using an {@link net.minecraft.entity.attribute.EntityAttributeModifier}.
     *
     * @return the ground speed modifier that should be applied by movement alterers using this config
     */
    float getWalkSpeedModifier();

    /**
     * Returns the inertia that should be applied by movement alterers using this config.
     * Inertia here is the amount of motion from the previous tick that should be kept,
     * according to the following formula: <pre>
     * <tt>motion * (1 - inertia) + lastMotion * inertia</tt></pre>
     *
     * @return the inertia that should be applied by movement alterers using this config
     */
    float getInertia();

    /**
     * @return the swimming mode applied to the entity
     * @see SwimMode
     */
    SwimMode getSwimMode();

    /**
     * @return the walking mode applied to the entity
     * @see WalkMode
     */
    WalkMode getWalkMode();

    /**
     * @deprecated check {@code getSwimMode() == SINKING}
     */
    @Deprecated
    TriState shouldSinkInWater();

    TriState shouldFlopOnLand();

    boolean canClimbWalls();

    boolean canPhaseThroughWalls();

    enum MovementMode {
        /**
         * The entity cannot use this movement
         */
        DISABLED,
        /**
         * The entity can use this movement, and can stop using it at any time
         */
        ENABLED,
        /**
         * The entity is always using this movement
         */
        FORCED,
        /**
         * No information, the {@link MovementAlterer} should use heuristics to determine which of the
         * other modes to use
         */
        UNSPECIFIED
    }
}
