package ladysnake.dissolution.api.corporeality;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

/**
 * Indicates that an entity is able to be possessed by a soul
 */
public interface IPossessable extends ISoulInteractable {

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
    default boolean onPossessionStop(EntityPlayer player) {
        return onPossessionStop(player, false);
    }

    boolean onPossessionStop(EntityPlayer player, boolean force);

    UUID getPossessingEntity();

    /**
     * Called when an entity is attacked by the player possessing this entity
     *
     * @param victim the entity to attack through this
     * @return true to cancel the original damage
     */
    boolean proxyAttack(EntityLivingBase victim, DamageSource source, float amount);

    /**
     * Called when the player possessing this entity fires an arrow
     *
     * @param charge the time the bow was used (in ticks)
     * @return true if no further processing is required from the player
     */
    boolean proxyRangedAttack(int charge);

    @SideOnly(Side.CLIENT)
    void possessTickClient();
}
