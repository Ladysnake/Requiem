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
package ladysnake.requiem.common.impl.movement;

import com.google.gson.Gson;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.entity.MovementConfig;
import org.apiguardian.api.API;

/**
 * A {@link MovementConfig} that can be easily manipulated by {@link Gson} and equivalent.
 */
public class SerializableMovementConfig implements MovementConfig {
    public static final SerializableMovementConfig SOUL = new SerializableMovementConfig(MovementMode.ENABLED, MovementMode.ENABLED, false, 0, 1f, 0.1F);

    private MovementMode flightMode;
    private MovementMode swimMode;
    private boolean flopsOnLand;
    private float gravity;
    private float fallSpeedModifier;
    private float inertia;

    @CalledThroughReflection
    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig() {
        this(MovementMode.UNSPECIFIED, MovementMode.UNSPECIFIED, false, 0, 1f, 0);
    }

    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig(MovementMode flightMode, MovementMode swimMode, boolean flopsOnLand, float gravity, float fallSpeedModifier, float inertia) {
        this.flightMode = flightMode;
        this.swimMode = swimMode;
        this.flopsOnLand = flopsOnLand;
        this.gravity = gravity;
        this.fallSpeedModifier = fallSpeedModifier;
        this.inertia = inertia;
    }

    @Override
    public MovementMode getFlightMode() {
        return flightMode;
    }

    @Override
    public float getAddedGravity() {
        return gravity;
    }

    @Override
    public float getFallSpeedModifier() {
        return fallSpeedModifier;
    }

    @Override
    public float getInertia() {
        return inertia;
    }

    @Override
    public MovementMode getSwimMode() {
        return swimMode;
    }

    @Override
    public boolean shouldFlopOnLand() {
        return flopsOnLand;
    }
}
