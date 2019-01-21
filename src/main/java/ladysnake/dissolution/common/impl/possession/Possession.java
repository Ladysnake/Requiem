package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.entity.TriggerableAttacker;
import ladysnake.dissolution.api.possession.Possessable;
import ladysnake.dissolution.api.possession.conversion.PossessableSubstitutionHandler;
import ladysnake.dissolution.api.possession.conversion.PossessionRegistry;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import ladysnake.dissolution.client.ShaderHandler;
import ladysnake.dissolution.common.entity.PossessableEntityImpl;
import ladysnake.dissolution.common.impl.possession.asm.AsmConverterProvider;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
    private static PossessionRegistry registry = new LazyDefaultPossessionRegistry(new AsmConverterProvider());

    public static PossessionRegistry getConversionRegistry() {
        return registry;
    }

    public static void init() {
        // Start possession on right click
        PlayerInteractionEvent.INTERACT_ENTITY_POSITIONED.register((player, world, hand, entity, hitPosition) -> {
            if (RemnantHandler.get(player).filter(RemnantHandler::isIncorporeal).isPresent()) {
                if (entity instanceof MobEntity && entity.world.isClient) {
                    ShaderHandler.INSTANCE.beginFishEyeAnimation(entity);
                }
                return ActionResult.FAILURE;
            }
            return ActionResult.PASS;
        });
        // Proxy melee attacks
        PlayerInteractionEvent.ATTACK_ENTITY.register((playerEntity, world, hand, target) -> {
            LivingEntity possessed = (LivingEntity) ((DissolutionPlayer)playerEntity).getPossessionManager().getPossessedEntity();
            if (possessed != null && !possessed.invalid) {
                if (((TriggerableAttacker)possessed).triggerDirectAttack(playerEntity, target)) {
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
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