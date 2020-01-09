package ladysnake.requiem.api.v1.entity;

import ladysnake.requiem.api.v1.internal.ApiInternals;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Registry for {@link MovementConfig}.
 *
 * <p> This registry is data-driven; entries are populated through
 * <tt>requiem/entity_mobility.json</tt> data files.
 */
public interface MovementRegistry {
    /**
     * Retrieve the movement registry for the given {@link World}.
     *
     * <p> If {@code world} is {@code null}, the returned dialogue registry is the one
     * used by server worlds.
     *
     * @param world the world for which to get the movement registry, or {@code null} to get the main registry
     * @return the movement registry for the given world
     */
    static MovementRegistry get(@Nullable World world) {
        return ApiInternals.getMovementRegistry(world);
    }

    MovementConfig getEntityMovementConfig(EntityType<?> type);
}
