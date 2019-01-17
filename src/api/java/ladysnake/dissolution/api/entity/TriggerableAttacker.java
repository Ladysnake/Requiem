package ladysnake.dissolution.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * An entity which attack can be triggered manually
 */
public interface TriggerableAttacker {
    /**
     * Triggers the attack on a known entity.
     *
     * @param player the player commanding the attack
     * @param target the attacked entity
     * @return <code>true</code> if the attack is a success
     * @implNote the default implementation by {@link LivingEntity} calls {@link LivingEntity#method_6121(Entity)}
     */
    default boolean triggerDirectAttack(PlayerEntity player, Entity target) {
        return false;
    }

    /**
     * Triggers an indirect attack. Indirect attacks have no defined target and are usually some kind of projectile.
     *
     * @param player the player commanding the attack
     * @return <code>true</code> if the attack is a success
     */
    default boolean triggerIndirectAttack(PlayerEntity player) {
        return false;
    }
}
