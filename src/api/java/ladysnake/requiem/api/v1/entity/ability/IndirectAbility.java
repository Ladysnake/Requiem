package ladysnake.requiem.api.v1.entity.ability;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * An {@link IndirectAbility} can be used without any specific target.
 * They tend to use some kind of projectile, or act on the entity itself.
 * Examples include priming an explosion, throwing a fireball, teleporting...
 *
 * @param <E> The type of mobs that can wield this ability
 */
@FunctionalInterface
public interface IndirectAbility<E extends MobEntity> extends MobAbility<E> {
    /**
     * Triggers an indirect ability.
     *
     * @param player the player commanding the ability
     * @return <code>true</code> if the ability has been successfully used
     */
    boolean trigger(PlayerEntity player);
}
