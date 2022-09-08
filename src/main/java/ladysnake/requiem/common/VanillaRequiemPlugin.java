/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common;

import baritone.api.fakeplayer.AutomatoneFakePlayer;
import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.locki.DefaultInventoryNodes;
import io.github.ladysnake.locki.ModdedInventoryNodes;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.entity.CurableEntityComponent;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.event.minecraft.AllowUseEntityCallback;
import ladysnake.requiem.api.v1.event.minecraft.LivingEntityDropCallback;
import ladysnake.requiem.api.v1.event.minecraft.MobTravelRidingCallback;
import ladysnake.requiem.api.v1.event.minecraft.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.event.minecraft.PrepareRespawnCallback;
import ladysnake.requiem.api.v1.event.requiem.CanCurePossessedCallback;
import ladysnake.requiem.api.v1.event.requiem.ConsumableItemEvents;
import ladysnake.requiem.api.v1.event.requiem.HumanityCheckCallback;
import ladysnake.requiem.api.v1.event.requiem.InitiateFractureCallback;
import ladysnake.requiem.api.v1.event.requiem.PlayerShellEvents;
import ladysnake.requiem.api.v1.event.requiem.PossessionEvents;
import ladysnake.requiem.api.v1.event.requiem.PossessionStateChangeCallback;
import ladysnake.requiem.api.v1.event.requiem.RemnantStateChangeCallback;
import ladysnake.requiem.api.v1.event.requiem.SoulCaptureEvents;
import ladysnake.requiem.api.v1.possession.PossessedData;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.possession.item.PossessionItemAction;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.api.v1.remnant.MobResurrectable;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.api.v1.remnant.VagrantInteractionRegistry;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.entity.SkeletonBoneComponent;
import ladysnake.requiem.common.entity.ability.*;
import ladysnake.requiem.common.entity.effect.ReclamationStatusEffect;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.gamerule.PossessionDetection;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.possession.MobRidingType;
import ladysnake.requiem.common.remnant.BasePossessionHandlers;
import ladysnake.requiem.common.remnant.PlayerSplitter;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.tag.RequiemBlockTags;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.core.ability.PlayerAbilityController;
import ladysnake.requiem.core.entity.SoulHolderComponent;
import ladysnake.requiem.core.entity.ability.AutoAimAbility;
import ladysnake.requiem.core.entity.ability.DelegatingDirectAbility;
import ladysnake.requiem.core.entity.ability.RangedAttackAbility;
import ladysnake.requiem.core.entity.ability.SnowmanSnowballAbility;
import ladysnake.requiem.core.record.EntityPositionClerk;
import ladysnake.requiem.core.resurrection.ResurrectionDataLoader;
import ladysnake.requiem.core.tag.RequiemCoreTags;
import ladysnake.requiem.core.util.RayHelper;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class VanillaRequiemPlugin implements RequiemPlugin {

    public static final String INFINITY_SHOT_TAG = "requiem:infinity_shot";
    public static final MobAbilityConfig<PlayerEntity> SOUL_ABILITY_CONFIG = MobAbilityConfig.<PlayerEntity>builder()
        .directAttack(player -> new DelegatingDirectAbility<>(player, LivingEntity.class, AbilityType.INTERACT))
        .directInteract(VagrantPossessAbility::new)
        .build();

    @Override
    public void onRequiemInitialize() {
        registerEtherealEventHandlers();
        registerPossessionEventHandlers();
        EntityPositionClerk.registerCallbacks();
        ReclamationStatusEffect.registerEventHandlers();
        CanCurePossessedCallback.EVENT.register((body) -> {
            CurableEntityComponent curableEntityComponent = CurableEntityComponent.KEY.get(body);
            return (curableEntityComponent.canBeCured() || curableEntityComponent.canBeAssimilated()) ? TriState.TRUE : TriState.DEFAULT;
        });
        LivingEntityDropCallback.EVENT.register((dead, deathCause) -> {
            if (!(dead instanceof ServerPlayerEntity lazarus)) {
                return false;
            }
            MobEntity secondLife = ResurrectionDataLoader.INSTANCE.getNextBody(lazarus, deathCause);
            if (secondLife != null) {
                ((MobResurrectable) lazarus).setResurrectionEntity(secondLife);
                return RemnantComponent.get(lazarus).getRemnantType().isDemon();
            }
            return false;
        });
        LivingEntityDropCallback.EVENT.register((dead, deathCause) -> {
            PossessedData.KEY.maybeGet(dead).ifPresent(PossessedData::dropItems);
            return false;
        });
        InitiateFractureCallback.EVENT.register(player -> {
            RemnantComponent remnantComponent = RemnantComponent.get(player);
            PossessionComponent possessionComponent = PossessionComponent.get(player);
            MobEntity host = possessionComponent.getHost();

            boolean success;

            if (remnantComponent.splitPlayer(false).isPresent()) {
                success = true;
            } else if (host != null) {
                Entity targetedEntity = RayHelper.getTargetedEntity(player);
                if (targetedEntity instanceof PlayerShellEntity shell && Objects.equals(player.getUuid(), ((PlayerShellEntity) targetedEntity).getOwnerUuid())) {
                    possessionComponent.stopPossessing();
                    remnantComponent.merge(shell);
                    RequiemNetworking.sendBodyCureMessage(player);
                    success = true;
                } else if (remnantComponent.canDissociateFrom(host)) {
                    possessionComponent.stopPossessing();
                    success = true;
                } else {
                    success = false;
                }
            } else {
                success = false;
            }

            if (success) {
                RequiemNetworking.sendEtherealAnimationMessage(player);
            }

            return success;
        });
        HumanityCheckCallback.EVENT.register(possessedEntity -> EnchantmentHelper.getEquipmentLevel(RequiemEnchantments.HUMANITY, possessedEntity));
        ConsumableItemEvents.POST_CONSUMED.register(RequiemCriteria.USED_TOTEM::trigger);
        SoulCaptureEvents.BEFORE_ATTEMPT.register((stealer, target, captureType) -> Optional.ofNullable(stealer.getStatusEffect(RequiemStatusEffects.ATTRITION)).map(StatusEffectInstance::getAmplifier).orElse(-1) < 3);
        SoulCaptureEvents.BEFORE_ATTEMPT.register((stealer, target, captureType) -> !SoulHolderComponent.isSoulless(target));
    }

    private void registerEtherealEventHandlers() {
        // Prevent incorporeal players from breaking anything
        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, facing) -> getInteractionResult(player));
        // Prevent incorporeal players from hitting anything
        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            // Proxy melee attacks
            ActionResult result = MobAbilityController.get(player).useDirect(AbilityType.ATTACK, target);
            if (result.isAccepted()) {
                if (result.shouldSwingHand()) {
                    player.resetLastAttackedTicks();
                }
                return ActionResult.SUCCESS;
            }
            return isInteractionForbidden(player, true) ? ActionResult.FAIL : ActionResult.PASS;
        });
        // Prevent incorporeal players from interacting with anything
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (isInteractionForbidden(player) && !world.getBlockState(hitResult.getBlockPos()).isIn(RequiemBlockTags.SOUL_INTERACTABLE)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        AllowUseEntityCallback.EVENT.register((player, world, hand, target) -> !isInteractionForbidden(player));
        UseItemCallback.EVENT.register((player, world, hand) -> new TypedActionResult<>(getInteractionResult(player), player.getStackInHand(hand)));
        // Make players respawn in the right place with the right state
        PrepareRespawnCallback.EVENT.register((original, clone, returnFromEnd) -> {
            if (original.isDead()) resetIdentity(clone);
            RemnantComponent.get(clone).prepareRespawn(original, returnFromEnd);
        });
        PlayerRespawnCallback.EVENT.register(((player, returnFromEnd) -> {
            player.sendAbilitiesUpdate();
            ((MobResurrectable) player).spawnResurrectionEntity();

            // effects do not normally get synced after respawn, so we do it ourselves
            for (StatusEffectInstance effect : player.getStatusEffects()) {
                player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), effect));
            }
            // Fix for MC-108707: when you respawn while a player (or a shell) is watching you, you don't get data tracker updates
            player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(player.getId(), player.getDataTracker(), true));
        }));
        RemnantStateChangeCallback.EVENT.register((player, remnant, cause) -> {
            if (cause.isCharacterSwitch()) resetIdentity(player);

            if (!remnant.isVagrant()) {
                PossessionComponent.get(player).stopPossessing(false);
                InventoryLimiter.instance().disable(player);
            } else {
                InventoryLimiter.instance().enable(player);
            }
            MobEntity possessed = PossessionComponent.getHost(player);
            if (possessed != null) {
                PlayerAbilityController.get(player).usePossessedAbilities(possessed);
            } else {
                PlayerAbilityController.get(player).resetAbilities(remnant.isIncorporeal());
            }
        });
    }

    private static void resetIdentity(PlayerEntity player) {
        GameProfile previouslyImpersonated = Impersonate.IMPERSONATION.get(player).stopImpersonation(PlayerSplitter.BODY_IMPERSONATION);
        if (previouslyImpersonated != null && player instanceof ServerPlayerEntity sp) {
            PlayerShellEvents.RESET_IDENTITY.invoker().resetIdentity(sp, previouslyImpersonated);
        }
    }

    @Nonnull
    private ActionResult getInteractionResult(PlayerEntity player) {
        return isInteractionForbidden(player) ? ActionResult.FAIL : ActionResult.PASS;
    }

    private boolean isInteractionForbidden(PlayerEntity player) {
        return isInteractionForbidden(player, false);
    }

    private boolean isInteractionForbidden(PlayerEntity player, boolean includeSouls) {
        RemnantComponent c = RemnantComponent.get(player);
        return !player.isCreative() && ((includeSouls && c.isVagrant()) || c.isIncorporeal()) || DeathSuspender.get(player).isLifeTransient();
    }

    private void registerPossessionEventHandlers() {
        BasePossessionHandlers.register();
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            LivingEntity possessed = PossessionComponent.get(player).getHost();
            if (possessed != null && MobRidingType.get(entity, possessed).canMount()) {
                if (!world.isClient) {
                    possessed.startRiding(entity);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
        MobTravelRidingCallback.EVENT.register((mount, rider) -> {
            MobEntity possessedEntity = PossessionComponent.getHost(rider);
            return possessedEntity != null && MobRidingType.get(mount, possessedEntity).canSteer();
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            LivingEntity possessed = PossessionComponent.getHost(player);
            if (possessed != null && !possessed.getType().isIn(RequiemCoreTags.Entity.ITEM_USERS) && !player.isCreative()) {
                return new TypedActionResult<>(ActionResult.FAIL, player.getStackInHand(hand));
            }
            return new TypedActionResult<>(ActionResult.PASS, player.getStackInHand(hand));
        });
        PossessionStateChangeCallback.EVENT.register(((player, possessed) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && possessed != null) {
                RequiemCriteria.PLAYER_POSSESSED_ENTITY.handle(serverPlayer, possessed);
            }
        }));
        PossessionStateChangeCallback.EVENT.register((player, possessed) -> {
                InventoryLimiter inventoryLimiter = InventoryLimiter.instance();
                if (!player.world.isClient) {
                    inventoryLimiter.enable(player);
                }
                if (possessed == null) {
                    PlayerAbilityController.get(player).resetAbilities(RemnantComponent.isIncorporeal(player));
                } else {
                    PlayerAbilityController.get(player).usePossessedAbilities(possessed);

                    if (!player.world.isClient) {
                        if (possessed.getType().isIn(RequiemCoreTags.Entity.INVENTORY_CARRIERS)) {
                            inventoryLimiter.unlock(player, DefaultInventoryNodes.MAIN_INVENTORY);
                        }
                        if (canUseItems(possessed)) {
                            inventoryLimiter.unlock(player, DefaultInventoryNodes.HANDS);
                            inventoryLimiter.unlock(player, DefaultInventoryNodes.CRAFTING);
                        }
                        if (canWearArmor(possessed)) {
                            inventoryLimiter.unlock(player, DefaultInventoryNodes.ARMOR);
                            inventoryLimiter.unlock(player, ModdedInventoryNodes.TOOL_SPACE);
                        }
                        if (canCarryHotbar(possessed)) {
                            inventoryLimiter.unlock(player, DefaultInventoryNodes.HOTBAR);
                        }
                        PossessedData.KEY.get(possessed).giftFirstPossessionLoot(player);
                    }
                }
            }
        );
        EntitySleepEvents.ALLOW_SLEEPING.register((player, pos) -> {
            MobEntity host = PossessionComponent.getHost(player);
            if (host != null && !host.getType().isIn(RequiemCoreTags.Entity.SLEEPERS)) {
                player.sendMessage(Text.translatable("requiem:block.minecraft.bed.invalid_body"), true);
                return PlayerEntity.SleepFailureReason.OTHER_PROBLEM;
            }
            return null;
        });
        PossessionEvents.DETECTION_ATTEMPT.register((sensed, sensor, reason) -> {
            PossessionDetection cfg = sensed.world.getGameRules().get(RequiemGamerules.POSSESSION_DETECTION).get();
            return switch (reason) {
                case BUMP, ATTACKING -> switch (cfg) {
                    case DISABLED -> PossessionEvents.DetectionAttempt.DetectionResult.UNDETECTED;
                    case NORMAL -> PossessionEvents.DetectionAttempt.DetectionResult.DETECTED;
                    case HARD -> PossessionEvents.DetectionAttempt.DetectionResult.CROWD_DETECTED;
                };
                case ATTACKED -> switch (cfg) {
                    case DISABLED -> PossessionEvents.DetectionAttempt.DetectionResult.UNDETECTED;
                    case NORMAL, HARD -> PossessionEvents.DetectionAttempt.DetectionResult.CROWD_DETECTED;
                };
            };
        });
    }

    private static boolean canUseItems(MobEntity possessed) {
        if (possessed.getType().isIn(RequiemCoreTags.Entity.ITEM_USERS)) {
            return true;
        }
        return possessed.canPickUpLoot();
    }

    private static boolean canCarryHotbar(MobEntity possessed) {
        return possessed.getType().isIn(RequiemEntityTypeTags.HOTBAR_CARRIERS);
    }

    private static boolean canWearArmor(MobEntity possessed) {
        if (possessed.getType().isIn(RequiemCoreTags.Entity.ARMOR_BANNED)) {
            return false;
        }
        if (possessed.getType().isIn(RequiemEntityTypeTags.ARMOR_USERS)) {
            return true;
        }
        return !possessed.getEquippedStack(EquipmentSlot.HEAD).isEmpty() || possessed.canEquip(new ItemStack(Items.LEATHER_HELMET));
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry reg) {
        reg.beginRegistration(EntityType.AXOLOTL).indirectInteract(AxolotlPlayingDeadAbility::new).build();
        reg.beginRegistration(EntityType.BLAZE).indirectAttack(BlazeFireballAbility::new).build();
        reg.beginRegistration(EntityType.CREEPER).indirectAttack(CreeperPrimingAbility::new).build();
        reg.beginRegistration(EntityType.ENDERMAN).indirectInteract(BlinkAbility::new).build();
        reg.beginRegistration(EntityType.EVOKER)
            .directAttack(EvokerFangAbility::new)
            .directInteract(EvokerWololoAbility::new)
            .indirectInteract(EvokerVexAbility::new)
            .build();
        reg.beginRegistration(EntityType.FROG)
            .directAttack(FrogCatchAbility::new)
            .build();
        reg.beginRegistration(EntityType.GHAST).indirectAttack(GhastFireballAbility::new).build();
        reg.beginRegistration(EntityType.GOAT).directAttack(GoatRamAbility::create).build();
        reg.beginRegistration(EntityType.GUARDIAN).directAttack(GuardianBeamAbility::new).build();
        reg.beginRegistration(EntityType.ELDER_GUARDIAN).directAttack(GuardianBeamAbility::new).build();
        reg.beginRegistration(EntityType.LLAMA).directAttack(RangedAttackAbility::new).build();
        reg.beginRegistration(EntityType.PUFFERFISH).indirectAttack(PufferfishInflationAbility::new).build();
        reg.beginRegistration(EntityType.SHULKER)
            .directAttack(ShulkerShootAbility::new)
            .indirectAttack(shulker -> new AutoAimAbility<>(shulker, AbilityType.ATTACK, 16.0, 4.0))
            .indirectInteract(ShulkerPeekAbility::new).build();
        reg.beginRegistration(EntityType.SNOW_GOLEM)
            .directAttack(e -> new RangedAttackAbility<>(e, 20, 10))
            .indirectInteract(SnowmanSnowballAbility::new).build();
        reg.beginRegistration(EntityType.TRADER_LLAMA).directAttack(RangedAttackAbility::new).build();
        reg.beginRegistration(EntityType.WITCH)
            .directAttack(owner -> new RangedAttackAbility<>(owner, 50, 10.)).build();
        reg.beginRegistration(EntityType.WITHER)
            .indirectAttack(WitherSkullAbility.BlueWitherSkullAbility::new)
            .directAttack(WitherSkullAbility.BlackWitherSkullAbility::new).build();
    }

    @Override
    public void registerRemnantStates(Registry<RemnantType> registry) {
        Registry.register(registry, Requiem.id("remnant"), RemnantTypes.REMNANT);
    }

    @Override
    public void registerSoulBindings(SoulbindingRegistry registry) {
        registry.registerSoulbound(RequiemStatusEffects.ATTRITION);
        registry.registerSoulbound(RequiemStatusEffects.EMANCIPATION);
        registry.registerSoulbound(RequiemStatusEffects.PENANCE);
        registry.registerSoulbound(RequiemStatusEffects.RECLAMATION);
    }

    @Override
    public void registerVagrantInteractions(VagrantInteractionRegistry registry) {
        registry.registerPossessionInteraction(
            EndermanEntity.class,
            (mob, player) -> !SoulHolderComponent.isSoulless(mob) && !PossessionComponent.get(player).startPossessing(mob, true),
            BasePossessionHandlers::performEndermanSoulAction,
            Requiem.id("textures/gui/enderjacking_icon.png")
        );
        registry.registerPossessionInteraction(PlayerEntity.class,
            (target, possessor) -> target.getType() == RequiemEntities.PLAYER_SHELL && target instanceof AutomatoneFakePlayer shell && PlayerShellEvents.PRE_MERGE.invoker().canMerge(possessor, target, shell.getDisplayProfile()),
            (target, possessor) -> {
                if (target instanceof PlayerShellEntity shell && !RemnantComponent.get(possessor).merge(shell)) {
                    possessor.sendMessage(Text.translatable("requiem:possess.incompatible_body"), true);
                }
            }
        );
        registry.registerPossessionInteraction(
            MobEntity.class,
            (mob, player) -> PossessionComponent.get(player).startPossessing(mob, true),
            (mob, player) -> PossessionComponent.get(player).startPossessing(mob)
        );
    }

    @Override
    public void registerPossessionItemActions(Registry<PossessionItemAction> registry) {
        Registry.register(registry, Requiem.id("pass"), (player, possessed, stack, world, hand) -> TypedActionResult.pass(stack));
        Registry.register(registry, Requiem.id("fail"), (player, possessed, stack, world, hand) -> TypedActionResult.fail(stack));
        Registry.register(registry, Requiem.id("cure"), VanillaRequiemPlugin::cure);
        Registry.register(registry, Requiem.id("eat_to_heal"), VanillaRequiemPlugin::healWithFood);
        Registry.register(registry, Requiem.id("replace_bone"), VanillaRequiemPlugin::replaceBone);
        Registry.register(registry, Requiem.id("witch_eat"), VanillaRequiemPlugin::eatWitchFood);
    }

    public static TypedActionResult<ItemStack> cure(PlayerEntity player, MobEntity possessed, ItemStack stack, World world, Hand hand) {
        if (RemnantComponent.get(player).canCurePossessed(possessed)) {
            PossessionComponent.get(player).startCuring();
            stack.decrement(1);
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.fail(stack);
    }

    public static TypedActionResult<ItemStack> healWithFood(PlayerEntity player, MobEntity possessed, ItemStack stack, World world, Hand hand) {
        FoodComponent food = stack.getItem().getFoodComponent();

        if (food != null) {
            possessed.heal(food.getHunger());
            player.eatFood(world, stack);
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.fail(stack);
    }

    public static TypedActionResult<ItemStack> replaceBone(PlayerEntity player, MobEntity possessed, ItemStack stack, World world, Hand hand) {
        if (SkeletonBoneComponent.KEY.get(possessed).replaceBone()) {
            stack.decrement(1);
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.fail(stack);
    }

    public static TypedActionResult<ItemStack> eatWitchFood(PlayerEntity player, MobEntity possessed, ItemStack stack, World world, Hand hand) {
        Map<StatusEffect, StatusEffectInstance> before = new HashMap<>(possessed.getActiveStatusEffects());
        ItemStack ret = stack.getItem().finishUsing(stack, world, player);
        Map<StatusEffect, StatusEffectInstance> after = new HashMap<>(possessed.getActiveStatusEffects());
        // Remove all negative status effects from the food
        revertHarmfulEffects(player, before, after);
        return TypedActionResult.success(ret);
    }

    private static void revertHarmfulEffects(PlayerEntity player, Map<StatusEffect, StatusEffectInstance> before, Map<StatusEffect, StatusEffectInstance> after) {
        for (StatusEffect statusEffect : after.keySet()) {
            if (statusEffect.getType() == StatusEffectType.HARMFUL) {
                StatusEffectInstance previous = before.get(statusEffect);
                StatusEffectInstance current = after.get(statusEffect);
                if (!Objects.equals(previous, current)) {
                    player.removeStatusEffect(statusEffect);
                    if (previous != null) {
                        player.addStatusEffect(new StatusEffectInstance(
                            statusEffect,
                            Math.min(previous.getDuration(), current.getDuration()),
                            Math.min(previous.getAmplifier(), current.getAmplifier()),
                            previous.isAmbient(),
                            previous.shouldShowParticles(),
                            previous.shouldShowIcon(),
                            previous,
                            statusEffect.getFactorData()
                        ));
                    }
                }
            }
        }
    }
}
