package ladysnake.dissolution.common;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.DissolutionPlugin;
import ladysnake.dissolution.api.v1.entity.ability.AbilityType;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.client.DissolutionEffects;
import ladysnake.dissolution.common.entity.ability.*;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.util.ActionResult;

public class VanillaDissolutionPlugin implements DissolutionPlugin {
    @Override
    public void onDissolutionInitialize() {
        // Start possession on right click
        PlayerInteractionEvent.INTERACT_ENTITY_POSITIONED.register((player, world, hand, target, hitPosition) -> {
            if (((DissolutionPlayer)player).getRemnantState().isIncorporeal()) {
                if (target instanceof MobEntity && target.world.isClient) {
                    DissolutionEffects.INSTANCE.beginFishEyeAnimation(target);
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
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry abilityRegistry) {
        abilityRegistry.register(EntityType.BLAZE, MobAbilityConfig.<BlazeEntity>builder().indirectAttack(BlazeFireballAbility::new).build());
        abilityRegistry.register(EntityType.GHAST, MobAbilityConfig.<GhastEntity>builder().indirectAttack(GhastFireballAbility::new).build());
        abilityRegistry.register(EntityType.CREEPER, MobAbilityConfig.<CreeperEntity>builder().indirectAttack(CreeperPrimingAbility::new).build());
        abilityRegistry.register(EntityType.EVOKER, MobAbilityConfig.<EvokerEntity>builder()
                .directAttack(EvokerFangAbility::new)
                .directInteract(EvokerWololoAbility::new)
                .indirectInteract(EvokerVexAbility::new)
                .build());
    }
}
