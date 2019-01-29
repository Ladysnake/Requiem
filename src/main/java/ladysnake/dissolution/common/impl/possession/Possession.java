package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.entity.ability.AbilityType;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.conversion.PossessableSubstitutionHandler;
import ladysnake.dissolution.api.v1.possession.conversion.PossessionConversionRegistry;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.client.ShaderHandler;
import ladysnake.dissolution.common.entity.PossessableEntityImpl;
import ladysnake.dissolution.common.entity.ability.BlazeFireballAbility;
import ladysnake.dissolution.common.entity.ability.CreeperPrimingAbility;
import ladysnake.dissolution.common.entity.ability.GhastFireballAbility;
import ladysnake.dissolution.common.impl.ability.DefaultedMobAbilityRegistry;
import ladysnake.dissolution.common.impl.ability.SimpleMobAbilityConfig;
import ladysnake.dissolution.common.impl.possession.asm.AsmConverterProvider;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.ActionResult;
import org.apiguardian.api.API;

import java.util.function.BiConsumer;

import static ladysnake.dissolution.common.impl.ability.SimpleMobAbilityConfig.defaultMelee;
import static ladysnake.dissolution.common.impl.possession.CopyStrategies.basicCopy;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Entry point for the possession mechanic.
 * Everything here is subject to be moved to a more specialized place.
 */
@API(status = EXPERIMENTAL)
public final class Possession {
    private static PossessionConversionRegistry conversionRegistry = new LazyDefaultPossessionConversionRegistry(new AsmConverterProvider());
    private static MobAbilityRegistry abilityRegistry = new DefaultedMobAbilityRegistry(SimpleMobAbilityConfig.DEFAULT);

    public static PossessionConversionRegistry getConversionRegistry() {
        return conversionRegistry;
    }

    public static MobAbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    public static void init() {
        // Start possession on right click
        PlayerInteractionEvent.INTERACT_ENTITY_POSITIONED.register((player, world, hand, target, hitPosition) -> {
            if (RemnantState.getIfRemnant(player).filter(RemnantState::isIncorporeal).isPresent()) {
                if (target instanceof MobEntity && target.world.isClient) {
                    ShaderHandler.INSTANCE.beginFishEyeAnimation(target);
                }
                return ActionResult.FAILURE;
            }
            return ActionResult.PASS;
        });
        // Proxy melee attacks
        PlayerInteractionEvent.ATTACK_ENTITY.register((playerEntity, world, hand, target) -> {
            LivingEntity possessed = (LivingEntity) ((DissolutionPlayer)playerEntity).getPossessionComponent().getPossessedEntity();
            if (possessed != null && !possessed.invalid) {
                if (possessed.world.isClient || ((Possessable)possessed).getMobAbilityController().useDirect(AbilityType.ATTACK, target)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAILURE;
            }
            return ActionResult.PASS;
        });
        registerDefaultConversions();
        registerDefaultAbilities();
    }

    private static void registerDefaultAbilities() {
        abilityRegistry.register(EntityType.BLAZE, new SimpleMobAbilityConfig<>(defaultMelee(), BlazeFireballAbility::new));
        abilityRegistry.register(EntityType.GHAST, new SimpleMobAbilityConfig<>(defaultMelee(), GhastFireballAbility::new));
        abilityRegistry.register(EntityType.CREEPER, new SimpleMobAbilityConfig<>(defaultMelee(), CreeperPrimingAbility::new));
    }

    private static void registerDefaultConversions() {
        conversionRegistry.registerPossessedConverter(EntityType.ZOMBIE, PossessableSubstitutionHandler.using(
                z -> new PossessableEntityImpl(z.world),
                basicCopy(),
                swapEntities()
        ));
    }

    public static <E extends MobEntity, P extends MobEntity & Possessable> BiConsumer<E, P> swapEntities() {
        return (entity, clone) -> {
            entity.world.method_8507(entity);
            if (clone.world.isClient) {
                ((ClientWorld)clone.world).method_2942(clone.getEntityId(), clone);
            } else {
                clone.world.spawnEntity(clone);
            }
        };
    }
}