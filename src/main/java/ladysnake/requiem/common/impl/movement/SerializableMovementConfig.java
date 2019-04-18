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
