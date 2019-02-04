package ladysnake.dissolution.common.impl.movement;

import ladysnake.dissolution.api.v1.annotation.CalledThroughReflection;
import ladysnake.dissolution.api.v1.entity.MovementConfig;
import org.apiguardian.api.API;

public class SerializableMovementConfig implements MovementConfig {
    public static final SerializableMovementConfig SOUL = new SerializableMovementConfig(MovementMode.ENABLED, MovementMode.ENABLED, 0, 1f, 0.1F);

    private MovementMode flightMode;
    private MovementMode swimMode;
    private float gravity;
    private float fallSpeedModifier;
    private float inertia;

    @CalledThroughReflection
    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig() {
        this(MovementMode.UNSPECIFIED, MovementMode.UNSPECIFIED, 0, 1f, 0);
    }

    @API(status = API.Status.INTERNAL)
    public SerializableMovementConfig(MovementMode flightMode, MovementMode swimMode, float gravity, float fallSpeedModifier, float inertia) {
        this.flightMode = flightMode;
        this.swimMode = swimMode;
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
}
