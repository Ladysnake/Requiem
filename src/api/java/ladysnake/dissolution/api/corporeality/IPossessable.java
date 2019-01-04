package ladysnake.dissolution.api.corporeality;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

import java.util.UUID;

/**
 * Indicates that an entity is able to be possessed by a soul
 */
public interface IPossessable {

    /**
     * Checks if this entity is ready to be possessed by the given player
     *
     * @return true if the possession should succeed
     */
    boolean canBePossessedBy(EntityPlayer player);

    /**
     * Called when an incorporeal player attempts to interact with this entity
     *
     * @param player the player attempting to possess this entity
     * @return true if no further processing of the action should be attempted
     */
    boolean onEntityPossessed(EntityPlayer player);

    /**
     * Called when a player attempts to dismount this entity
     *
     * @param player the player attempting to stop the possession
     * @return false if the action is denied
     */
    boolean onPossessionStop(EntityPlayer player, boolean force);

    UUID getPossessingEntityId();

    EntityPlayer getPossessingEntity();

    void setSleeping(boolean sleeping);

    /**
     * Called when an entity is attacked by the player possessing this entity
     *
     * @param victim the entity to attack through this
     * @return true to cancel the original damage
     */
    boolean proxyAttack(EntityLivingBase victim, DamageSource source, float amount);

    @Deprecated
    default void possessTickClient() {}

    default void updatePossessing() {}

    default boolean isBeingPossessed() {
        return this.getPossessingEntityId() != null;
    }

    /**
     * Signals to this entity that the possessing player has logged out,
     * and that the entity should disappear in the next tick
     */
    void markForLogOut();

    void setDummyRidingEntity(Entity riding);
}
