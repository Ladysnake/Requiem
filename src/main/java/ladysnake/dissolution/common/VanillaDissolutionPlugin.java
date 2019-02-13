package ladysnake.dissolution.common;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.DissolutionPlugin;
import ladysnake.dissolution.api.v1.entity.ability.AbilityType;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.conversion.PossessionConversionRegistry;
import ladysnake.dissolution.client.DissolutionFx;
import ladysnake.dissolution.common.entity.DissolutionEntities;
import ladysnake.dissolution.common.entity.PlayerShellEntity;
import ladysnake.dissolution.common.entity.ability.*;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.SnowmanEntity;
import net.minecraft.util.ActionResult;

public class VanillaDissolutionPlugin implements DissolutionPlugin {

    @Override
    public void onDissolutionInitialize() {
        registerPossessionEventHandlers();
    }

    private void registerPossessionEventHandlers() {
        // Start possession on right click
        UseEntityCallback.EVENT.register((player, world, hand, target, hitPosition) -> {
            if (((DissolutionPlayer)player).getRemnantState().isIncorporeal()) {
                if (target instanceof MobEntity && target.world.isClient) {
                    DissolutionFx.INSTANCE.beginFishEyeAnimation(target);
                }
                return ActionResult.FAILURE;
            }
            return ActionResult.PASS;
        });
        // Proxy melee attacks
        AttackEntityCallback.EVENT.register((playerEntity, world, hand, target, hitResult) -> {
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
        abilityRegistry.register(EntityType.BLAZE, MobAbilityConfig.builder().indirectAttack(BlazeFireballAbility::new).build());
        abilityRegistry.register(EntityType.CREEPER, MobAbilityConfig.<CreeperEntity>builder().indirectAttack(CreeperPrimingAbility::new).build());
        abilityRegistry.register(EntityType.ENDERMAN, MobAbilityConfig.builder().indirectInteract(BlinkAbility::new).build());
        abilityRegistry.register(EntityType.EVOKER, MobAbilityConfig.<EvokerEntity>builder()
                .directAttack(EvokerFangAbility::new)
                .directInteract(EvokerWololoAbility::new)
                .indirectInteract(EvokerVexAbility::new)
                .build());
        abilityRegistry.register(EntityType.GHAST, MobAbilityConfig.builder().indirectAttack(GhastFireballAbility::new).build());
        abilityRegistry.register(EntityType.LLAMA, MobAbilityConfig.<LlamaEntity>builder().indirectAttack(RangedAttackAbility::new).build());
        abilityRegistry.register(EntityType.TRADER_LLAMA, MobAbilityConfig.<LlamaEntity>builder().indirectAttack(RangedAttackAbility::new).build());
        abilityRegistry.register(EntityType.SNOW_GOLEM, MobAbilityConfig.<SnowmanEntity>builder().indirectAttack(RangedAttackAbility::new).build());
        abilityRegistry.register(EntityType.WITCH, MobAbilityConfig.<WitchEntity>builder().indirectAttack(RangedAttackAbility::new).build());
    }

    @Override
    public void registerPossessedConversions(PossessionConversionRegistry registry) {
        registry.registerPossessedConverter(DissolutionEntities.PLAYER_SHELL, PlayerShellEntity::onSoulInteract);
    }
}
