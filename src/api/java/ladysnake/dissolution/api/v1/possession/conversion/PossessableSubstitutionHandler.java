package ladysnake.dissolution.api.v1.possession.conversion;

import ladysnake.dissolution.api.v1.possession.Possessable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.apiguardian.api.API;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Substitutes an entity in the world with a possessable equivalent.
 * This functional interface is expected to operate via side effects.
 *
 * @param <E> the type of mobs that this handler can substitute
 */
@FunctionalInterface
@API(status = API.Status.EXPERIMENTAL)
public interface PossessableSubstitutionHandler<E extends MobEntity> extends BiFunction<E, PlayerEntity, Possessable> {

    @Nullable
    Possessable apply(E entity, @Nullable PlayerEntity possessor);

    /**
     * Creates a substitution handler using basic components.
     *
     * @param possessableProvider       a provider of possessable entities
     * @param copyStrategy              a strategy for copying relevant attributes from the original entity to the clone
     * @param worldSubstitutionExecutor a consumer responsible for spawning the generated entity and removing the old one
     * @param <E>                       the type of mobs that the handler can substitute
     * @param <P>                       the type of possessed entities generated
     * @return a substitution handler combining all 3 functions
     */
    static <E extends MobEntity, P extends MobEntity & Possessable> PossessableSubstitutionHandler<E> using(
            Function<E, P> possessableProvider,
            BiConsumer<E, P> copyStrategy,
            BiConsumer<E, P> worldSubstitutionExecutor
    ) {
        return ((entity, possessor) -> {
            if (entity instanceof Possessable) {
                return (Possessable) entity;
            }
            P possessable = possessableProvider.apply(entity);
            if (possessable != null) {
                copyStrategy.accept(entity, possessable);
                worldSubstitutionExecutor.accept(entity, possessable);
            }
            return possessable;
        });
    }

    static <E extends MobEntity, P extends MobEntity & Possessable> BiConsumer<E, P> swapEntities() {
        return (entity, clone) -> {
            // Server and clients have different methods to remove entities
            if (clone.world.isClient) {
                // On clients, we force replace the existing entity with the clone
                ((ClientWorld)clone.world).method_2942(clone.getEntityId(), clone);
            } else {
                // On servers, we remove the entity immediately
                ((ServerWorld)entity.world).method_18217(entity);
                // Then spawn the clone
                clone.world.spawnEntity(clone);
            }
        };
    }
}
