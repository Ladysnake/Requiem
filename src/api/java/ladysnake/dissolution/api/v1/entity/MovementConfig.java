package ladysnake.dissolution.api.v1.entity;

public interface MovementConfig {
    /**
     * @return the flight mode applied to the entity
     * @see FlightMode
     */
    FlightMode getFlightMode();

    /**
     * Returns the gravity that should be applied by movement alterers using this config.
     * Minecraft's default gravity is {@literal 0.2f}.
     *
     * @return the gravity that should be applied by movement alterers using this config
     */
    float getGravity();

    /**
     * Returns the inertia that should be applied by movement alterers using this config.
     * Inertia here is the amount of motion from the previous tick that should be kept,
     * according to the following formula: <pre>
     * <tt>motion * (1 - inertia) + lastMotion * inertia</tt>
     *
     * @return the inertia that should be applied by movement alterers using this config
     */
    float getInertia();

    enum FlightMode {
        /**
         * The entity cannot fly
         */
        DISABLED,
        /**
         * The entity can fly, and can stop flying at any time
         */
        ENABLED,
        /**
         * The entity is always flying
         */
        FORCED
    }
}
