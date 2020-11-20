package ladysnake.pandemonium;

import ladysnake.pandemonium.common.PlayerSplitter;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.pandemonium.common.entity.ability.*;
import ladysnake.pandemonium.common.network.PandemoniumNetworking;
import ladysnake.pandemonium.common.remnant.PlayerBodyTracker;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.event.requiem.InitiateFractureCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.entity.ability.MeleeAbility;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PandemoniumRequiemPlugin implements RequiemPlugin {

    @Override
    public void onRequiemInitialize() {
        PossessionStartCallback.EVENT.register(Pandemonium.id("shell_interaction"), (target, possessor, simulate) -> {
            if (target instanceof PlayerShellEntity) {
                if (!simulate && !possessor.world.isClient) {
                    PlayerSplitter.merge(((PlayerShellEntity) target), (ServerPlayerEntity) possessor);
                }
                return PossessionStartCallback.Result.HANDLED;
            }
            return PossessionStartCallback.Result.PASS;
        });

        // Enderman specific behaviour is unneeded now that players can possess them
        PossessionStartCallback.EVENT.unregister(new Identifier(Requiem.MOD_ID, "enderman"));
        PossessionStartCallback.EVENT.register(Pandemonium.id("allow_everything"), (target, possessor, simulate) -> PossessionStartCallback.Result.ALLOW);
        InitiateFractureCallback.EVENT.register(player -> {
            RemnantComponent remnantState = RemnantComponent.get(player);
            PossessionComponent possessionComponent = PossessionComponent.get(player);
            boolean success;

            if (!remnantState.isSoul()) {
                PlayerSplitter.split(player);
                success = true;
            } else if (possessionComponent.getPossessedEntity() != null && PlayerBodyTracker.get(player).getAnchor() != null) {
                // TODO make a gamerule to keep the inventory when leaving a mob
                possessionComponent.stopPossessing();
                success = true;
            } else {
                success = false;
            }

            if (success) {
                PandemoniumNetworking.sendEtherealAnimationMessage(player);
            }

            return success;
        });
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry abilityRegistry) {
        abilityRegistry.register(EntityType.BLAZE, MobAbilityConfig.builder().indirectAttack(BlazeFireballAbility::new).build());
        abilityRegistry.register(EntityType.CAT, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.CREEPER, MobAbilityConfig.<CreeperEntity>builder().indirectAttack(CreeperPrimingAbility::new).build());
        abilityRegistry.register(EntityType.ENDERMAN, MobAbilityConfig.builder().indirectInteract(BlinkAbility::new).build());
        abilityRegistry.register(EntityType.EVOKER, MobAbilityConfig.<EvokerEntity>builder()
                .directAttack(EvokerFangAbility::new)
                .directInteract(EvokerWololoAbility::new)
                .indirectInteract(EvokerVexAbility::new)
                .build());
        abilityRegistry.register(EntityType.GHAST, MobAbilityConfig.builder().indirectAttack(GhastFireballAbility::new).build());
        abilityRegistry.register(EntityType.GUARDIAN, MobAbilityConfig.<GuardianEntity>builder().directAttack(GuardianBeamAbility::new).build());
        abilityRegistry.register(EntityType.ELDER_GUARDIAN, MobAbilityConfig.<GuardianEntity>builder().directAttack(GuardianBeamAbility::new).build());
        abilityRegistry.register(EntityType.IRON_GOLEM, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.LLAMA, MobAbilityConfig.<LlamaEntity>builder().directAttack(RangedAttackAbility::new).build());
        abilityRegistry.register(EntityType.OCELOT, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.SHULKER, MobAbilityConfig.<ShulkerEntity>builder()
                .directAttack(ShulkerShootAbility::new)
                .indirectAttack(ShulkerShootAbility::new)
                .indirectInteract(ShulkerPeekAbility::new).build());
        abilityRegistry.register(EntityType.SNOW_GOLEM, MobAbilityConfig.builder().indirectInteract(SnowmanSnowballAbility::new).build());
        abilityRegistry.register(EntityType.TRADER_LLAMA, MobAbilityConfig.<LlamaEntity>builder().directAttack(RangedAttackAbility::new).build());
        abilityRegistry.register(EntityType.WITCH, MobAbilityConfig.<WitchEntity>builder().directAttack(RangedAttackAbility::new).build());
    }
}
