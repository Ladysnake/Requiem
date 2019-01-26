package ladysnake.dissolution.common.impl;

import ladysnake.dissolution.api.v1.entity.MovementConfig;

public class SerializableMovementConfig implements MovementConfig {
    private FlightMode flightMode;
    private float gravity;
    private float inertia;

    public SerializableMovementConfig(FlightMode flightMode, float gravity, float inertia) {
        this.flightMode = flightMode;
        this.gravity = gravity;
        this.inertia = inertia;
    }

    @Override
    public FlightMode getFlightMode() {
        return flightMode;
    }

    @Override
    public float getGravity() {
        return gravity;
    }

    @Override
    public float getInertia() {
        return inertia;
    }
}
