package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.possession.Possessable;
import ladysnake.dissolution.api.possession.PossessableSubstitutionHandler;
import ladysnake.dissolution.api.possession.PossessionRegistry;
import ladysnake.dissolution.api.possession.Possessor;
import ladysnake.dissolution.common.entity.PossessableEntityImpl;
import ladysnake.dissolution.common.impl.possession.asm.ASMConverterProvider;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.ActionResult;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Entry point for the possession mechanic.
 * Everything here is subject to be moved to a more specialized place.
 */
@API(status = EXPERIMENTAL)
public class Possession {
    private static PossessionRegistry registry = new LazyDefaultPossessionRegistry(new ASMConverterProvider());

    public static PossessionRegistry getConversionRegistry() {
        return registry;
    }

    public static void init() {
        PlayerInteractionEvent.INTERACT_ENTITY_POSITIONED.register((player, world, hand, entity, hitPosition) -> {
            if (entity instanceof MobEntity && !entity.world.isClient) {
                MobEntity mob = (MobEntity) entity;
                if (((Possessor) player).startPossessing(mob)) {
                    return ActionResult.SUCCESS;
                }
            } else if (entity.world.isClient) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAILURE;
        });
        registerDefaultConversions();
    }

    private static void registerDefaultConversions() {
        registry.registerPossessedConverter(EntityType.ZOMBIE, PossessableSubstitutionHandler.using(
                z -> new PossessableEntityImpl(z.world),
                CopyStrategies::basicCopy,
                Possession::swapEntities
        ));
    }

    public static <E extends MobEntity, P extends MobEntity & Possessable> void swapEntities(E entity, P clone) {
        entity.world.method_8507(entity);
        if (clone.world.isClient) {
            ((ClientWorld)clone.world).method_2942(clone.getEntityId(), clone);
        } else {
            clone.world.spawnEntity(clone);
        }
    }
}