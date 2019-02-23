package ladysnake.dissolution.common;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.DissolutionPlugin;
import ladysnake.dissolution.api.v1.entity.ability.AbilityType;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.dissolution.api.v1.event.ItemPickupCallback;
import ladysnake.dissolution.api.v1.event.PlayerCloneCallback;
import ladysnake.dissolution.api.v1.event.PlayerRespawnCallback;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.conversion.PossessionConversionRegistry;
import ladysnake.dissolution.api.v1.remnant.RemnantState;
import ladysnake.dissolution.api.v1.remnant.RemnantType;
import ladysnake.dissolution.client.DissolutionFx;
import ladysnake.dissolution.common.entity.DissolutionEntities;
import ladysnake.dissolution.common.entity.PlayerShellEntity;
import ladysnake.dissolution.common.entity.ability.*;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.SnowmanEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;
import static ladysnake.dissolution.common.remnant.RemnantStates.LARVA;
import static ladysnake.dissolution.common.remnant.RemnantStates.YOUNG;

public class VanillaDissolutionPlugin implements DissolutionPlugin {

    @Override
    public void onDissolutionInitialize() {
        registerEtherealEventHandlers();
        registerPossessionEventHandlers();
    }

    private void registerEtherealEventHandlers() {
        // Prevent incorporeal players from picking up anything
        ItemPickupCallback.EVENT.register((player, pickedUp) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isSoul).isPresent()) {
                Entity possessed = (Entity) ((DissolutionPlayer)player).getPossessionComponent().getPossessedEntity();
                if (possessed == null || !DissolutionEntityTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });
        // Prevent incorporeal players from breaking anything
        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, facing) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isIncorporeal).isPresent()) {
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });
        // Prevent incorporeal players from hitting anything
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isIncorporeal).isPresent()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        PlayerCloneCallback.EVENT.register(((original, clone, returnFromEnd) -> ((DissolutionPlayer)original).getRemnantState().onPlayerClone(clone, !returnFromEnd)));
        PlayerRespawnCallback.EVENT.register(((player, returnFromEnd) -> {
            CustomPayloadS2CPacket corporealityMessage = createCorporealityMessage(player);
            sendTo(player, corporealityMessage);
            sendToAllTracking(player, corporealityMessage);
            CustomPayloadS2CPacket possessionMessage = createPossessionMessage(player.getUuid(), -1);
            sendTo(player, possessionMessage);
            sendToAllTracking(player, possessionMessage);
        }));
    }

    private void registerPossessionEventHandlers() {
        // Start possession on right click
        UseEntityCallback.EVENT.register((player, world, hand, target, hitPosition) -> {
            if (((DissolutionPlayer)player).getRemnantState().isIncorporeal()) {
                if (target instanceof MobEntity && target.world.isClient) {
                    DissolutionFx.INSTANCE.beginFishEyeAnimation(target);
                }
                return ActionResult.FAIL;
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
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
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
        abilityRegistry.register(EntityType.IRON_GOLEM, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.LLAMA, MobAbilityConfig.<LlamaEntity>builder().indirectAttack(RangedAttackAbility::new).build());
        abilityRegistry.register(EntityType.OCELOT, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.SNOW_GOLEM, MobAbilityConfig.<SnowmanEntity>builder().indirectAttack(RangedAttackAbility::new).build());
        abilityRegistry.register(EntityType.TRADER_LLAMA, MobAbilityConfig.<LlamaEntity>builder().indirectAttack(RangedAttackAbility::new).build());
        abilityRegistry.register(EntityType.WITCH, MobAbilityConfig.<WitchEntity>builder().indirectAttack(RangedAttackAbility::new).build());
    }

    @Override
    public void registerPossessedConversions(PossessionConversionRegistry registry) {
        registry.registerPossessedConverter(DissolutionEntities.PLAYER_SHELL, PlayerShellEntity::onSoulInteract);
    }

    @Override
    public void registerRemnantStates(Registry<RemnantType> registry) {
        Registry.register(registry, Dissolution.id("larva"), LARVA);
        Registry.register(registry, Dissolution.id("young"), YOUNG);
    }
}
