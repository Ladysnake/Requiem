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

    boolean shouldFlopOnLand();

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
