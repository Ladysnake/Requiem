package ladysnake.dissolution.common.impl;

import ladysnake.dissolution.api.v1.entity.MovementConfig;
import org.apiguardian.api.API;

public class SerializableMovementConfig implements MovementConfig {
    public static final SerializableMovementConfig SOUL = new SerializableMovementConfig(FlightMode.ENABLED, 0, 1f, 0.1F);

    private FlightMode flightMode;
    private float gravity;
    private float fallSpeedModifier;
    private float inertia;

    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig(FlightMode flightMode, float gravity, float fallSpeedModifier, float inertia) {
        this.flightMode = flightMode;
        this.gravity = gravity;
        this.fallSpeedModifier = fallSpeedModifier;
        this.inertia = inertia;
    }

    @Override
    public FlightMode getFlightMode() {
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
}
