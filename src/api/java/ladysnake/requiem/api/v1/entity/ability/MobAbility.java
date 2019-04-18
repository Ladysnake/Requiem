package ladysnake.requiem.api.v1.entity.ability;

import net.minecraft.entity.mob.MobEntity;

/**
 * A {@link MobAbility} is a special ability wielded by some entities,
 * that ethereal players possessing those entities can take advantage of.
 * Abilities are usually active, they substitute AI goals for possessed entities.
 *
 * @param <E> The type of mobs that can wield this ability
 * @see net.minecraft.entity.ai.goal.Goal
 */
public interface MobAbility<E extends MobEntity> {
    /**
     * Called each tick. Allows abilities to span over some time.
     */
    default void update() { }
}
