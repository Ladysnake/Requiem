package ladysnake.requiem.api.v1;

import net.minecraft.entity.mob.MobEntity;

/**
 * An entity implementing {@link MobResurrectable} can be resurrected as a new mob after death.
 * All {@link net.minecraft.server.network.ServerPlayerEntity} implement this interface.
 */
public interface MobResurrectable {
    /**
     * Sets the mob that this player will use as its second life.
     * <p>
     * This should not be called with an entity that is already spawned in the world.
     * If the player is not {@link RequiemPlayer#isRemnant() remnant}, this call will have no effect.
     */
    void setResurrectionEntity(MobEntity secondLife);

    /**
     * Spawns a previously set resurrection entity, and make the player start possessing the entity.
     */
    void spawnResurrectionEntity();
}
