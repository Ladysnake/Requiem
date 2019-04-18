package ladysnake.requiem.api.v1.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * A {@link DirectAbility} targets a specific entity
 *
 * @param <E> The type of mobs that can wield this ability
 */
@FunctionalInterface
public interface DirectAbility<E extends MobEntity> extends MobAbility<E> {
    /**
     * Triggers the ability on a known entity.
     *
     * @param player the player commanding the ability
     * @param target the targeted entity
     * @return <code>true</code> if the ability has been successfully used
     */
    boolean trigger(PlayerEntity player, Entity target);
}
