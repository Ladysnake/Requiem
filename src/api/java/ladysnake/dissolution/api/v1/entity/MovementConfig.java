package ladysnake.dissolution.api.v1.entity;

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
     * Returns the inertia that should be applied by movement alterers using this config.
     * Inertia here is the amount of motion from the previous tick that should be kept,
     * according to the following formula: <pre>
     * <tt>motion * (1 - inertia) + lastMotion * inertia</tt>
     *
     * @return the inertia that should be applied by movement alterers using this config
     */
    float getInertia();

    /**
     * @return the swimming mode applied to the entity
     * @see MovementMode
     */
    MovementMode getSwimMode();

    enum MovementMode {
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
        FORCED,
        /**
         * No information
         */
        UNSPECIFIED
    }
}
