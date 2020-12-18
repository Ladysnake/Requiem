/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.entity.InventoryLimiter;
import ladysnake.requiem.api.v1.entity.InventoryPart;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.event.minecraft.AllowUseEntityCallback;
import ladysnake.requiem.api.v1.event.minecraft.LivingEntityDropCallback;
import ladysnake.requiem.api.v1.event.minecraft.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.event.minecraft.PrepareRespawnCallback;
import ladysnake.requiem.api.v1.event.requiem.HumanityCheckCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStateChangeCallback;
import ladysnake.requiem.api.v1.event.requiem.RemnantStateChangeCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.*;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.enchantment.RequiemEnchantments;
import ladysnake.requiem.common.entity.ability.AutoAimAbility;
import ladysnake.requiem.common.entity.ability.ShulkerPeekAbility;
import ladysnake.requiem.common.entity.ability.ShulkerShootAbility;
import ladysnake.requiem.common.entity.ability.SnowmanSnowballAbility;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.impl.ability.PlayerAbilityController;
import ladysnake.requiem.common.impl.remnant.dialogue.PlayerDialogueTracker;
import ladysnake.requiem.common.impl.resurrection.ResurrectionDataLoader;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.remnant.BasePossessionHandlers;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nonnull;
import java.util.UUID;

import static ladysnake.requiem.common.remnant.RemnantTypes.MORTAL;

public final class VanillaRequiemPlugin implements RequiemPlugin {

    public static final UUID INHERENT_MOB_SLOWNESS_UUID = UUID.fromString("a2ebbb6b-fd10-4a30-a0c7-dadb9700732e");
    /**
     * Mobs do not use 100% of their movement speed attribute, so we compensate with this modifier when they are possessed
     */
    public static final EntityAttributeModifier INHERENT_MOB_SLOWNESS = new EntityAttributeModifier(
        INHERENT_MOB_SLOWNESS_UUID,
        "Inherent Mob Slowness",
        -0.66,
        EntityAttributeModifier.Operation.MULTIPLY_TOTAL
    );

    @Override
    public void onRequiemInitialize() {
        registerEtherealEventHandlers();
        registerPossessionEventHandlers();
        LivingEntityDropCallback.EVENT.register((dead, deathCause) -> {
            if (!(dead instanceof ServerPlayerEntity)) {
                return false;
            }
            ServerPlayerEntity lazarus = (ServerPlayerEntity) dead;
            MobEntity secondLife = ResurrectionDataLoader.INSTANCE.getNextBody(lazarus, deathCause);
            if (secondLife != null) {
                ((MobResurrectable) lazarus).setResurrectionEntity(secondLife);
                return RemnantComponent.get(lazarus).getRemnantType().isDemon();
            }
            return false;
        });
        HumanityCheckCallback.EVENT.register(possessedEntity -> EnchantmentHelper.getEquipmentLevel(RequiemEnchantments.HUMANITY, possessedEntity));
    }

    private void registerEtherealEventHandlers() {
        // Prevent incorporeal players from breaking anything
        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, facing) -> getInteractionResult(player));
        // Prevent incorporeal players from hitting anything
        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            // Proxy melee attacks
            if (MobAbilityController.get(player).useDirect(AbilityType.ATTACK, target)) {
                player.resetLastAttackedTicks();
                return ActionResult.SUCCESS;
            }
            return isInteractionForbidden(player, true) ? ActionResult.FAIL : ActionResult.PASS;
        });
        // Prevent incorporeal players from interacting with anything
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> getInteractionResult(player));
        AllowUseEntityCallback.EVENT.register((player, world, hand, target) -> !isInteractionForbidden(player));
        UseItemCallback.EVENT.register((player, world, hand) -> new TypedActionResult<>(getInteractionResult(player), player.getStackInHand(hand)));
        // Make players respawn in the right place with the right state
        PrepareRespawnCallback.EVENT.register((original, clone, returnFromEnd) -> RemnantComponent.get(clone).prepareRespawn(original, returnFromEnd));
        PlayerRespawnCallback.EVENT.register(((player, returnFromEnd) -> {
            player.sendAbilitiesUpdate();
            ((MobResurrectable) player).spawnResurrectionEntity();

            for (StatusEffectInstance effect : player.getStatusEffects()) {
                player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getEntityId(), effect));
            }
        }));
        RemnantStateChangeCallback.EVENT.register((player, remnant) -> {
            InventoryLimiter.KEY.get(player).setEnabled(remnant.isVagrant());
            PlayerAbilityController.get(player).resetAbilities(remnant.isIncorporeal());
        });
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
            if (entity instanceof SpiderEntity) {
                LivingEntity possessed = PossessionComponent.get(player).getPossessedEntity();
                if (possessed instanceof SkeletonEntity) {
                    if (!world.isClient) {
                        possessed.startRiding(entity);
                    }
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            LivingEntity possessed = PossessionComponent.getPossessedEntity(player);
            if (possessed != null && !RequiemEntityTypeTags.ITEM_USERS.contains(possessed.getType()) && !player.isCreative()) {
                return new TypedActionResult<>(ActionResult.FAIL, player.getStackInHand(hand));
            }
            return new TypedActionResult<>(ActionResult.PASS, player.getStackInHand(hand));
        });
        PossessionStateChangeCallback.EVENT.register(((player, possessed) -> {
            if (player instanceof ServerPlayerEntity && possessed != null) {
                RequiemCriteria.PLAYER_POSSESSED_ENTITY.handle((ServerPlayerEntity) player, possessed);
            }
        }));
        PossessionStateChangeCallback.EVENT.register((player, possessed) -> {
                InventoryLimiter inventoryLimiter = InventoryLimiter.KEY.get(player);
                if (possessed == null) {
                    for (InventoryPart part : InventoryPart.VALUES) {
                        inventoryLimiter.lock(part);
                    }
                    PlayerAbilityController.get(player).resetAbilities(RemnantComponent.isIncorporeal(player));
                } else {
                    if (RequiemEntityTypeTags.INVENTORY_CARRIERS.contains(possessed.getType())) {
                        inventoryLimiter.unlock(InventoryPart.MAIN);
                    } else {
                        inventoryLimiter.lock(InventoryPart.MAIN);
                    }
                    if (canUseItems(possessed)) {
                        inventoryLimiter.unlock(InventoryPart.HANDS);
                        inventoryLimiter.unlock(InventoryPart.CRAFTING);
                    } else {
                        inventoryLimiter.lock(InventoryPart.HANDS);
                        inventoryLimiter.lock(InventoryPart.CRAFTING);
                    }
                    if (canWearArmor(possessed)) {
                        inventoryLimiter.unlock(InventoryPart.ARMOR);
                    } else {
                        inventoryLimiter.lock(InventoryPart.ARMOR);
                    }
                    PlayerAbilityController.get(player).usePossessedAbilities(possessed);
                }
            }
        );
    }

    private static boolean canUseItems(MobEntity possessed) {
        if (RequiemEntityTypeTags.ITEM_USERS.contains(possessed.getType())) {
            return true;
        }
        return possessed.canPickUpLoot();
    }

    private static boolean canWearArmor(MobEntity possessed) {
        if (RequiemEntityTypeTags.ARMOR_USERS.contains(possessed.getType())) {
            return true;
        }
        return !possessed.getEquippedStack(EquipmentSlot.HEAD).isEmpty() || possessed.canEquip(new ItemStack(Items.LEATHER_HELMET));
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry abilityRegistry) {
        abilityRegistry.register(EntityType.SHULKER, MobAbilityConfig.<ShulkerEntity>builder()
            .directAttack(ShulkerShootAbility::new)
            .indirectAttack(shulker -> new AutoAimAbility<>(shulker, AbilityType.ATTACK, 16.0, 4.0))
            .indirectInteract(ShulkerPeekAbility::new).build());
        abilityRegistry.register(EntityType.SNOW_GOLEM, MobAbilityConfig.builder().indirectInteract(SnowmanSnowballAbility::new).build());
    }

    @Override
    public void registerRemnantStates(Registry<RemnantType> registry) {
        Registry.register(registry, Requiem.id("remnant"), RemnantTypes.REMNANT);
        Registry.register(registry, Requiem.id("wandering_spirit"), RemnantTypes.WANDERING_SPIRIT);
    }

    @Override
    public void registerSoulBindings(SoulbindingRegistry registry) {
        registry.registerSoulbound(RequiemStatusEffects.ATTRITION);
    }

    @Override
    public void registerDialogueActions(DialogueRegistry registry) {
        registry.registerAction(PlayerDialogueTracker.BECOME_REMNANT, p -> handleRemnantChoiceAction(p, RemnantTypes.REMNANT));
        registry.registerAction(PlayerDialogueTracker.BECOME_WANDERING_SPIRIT, p -> handleRemnantChoiceAction(p, RemnantTypes.WANDERING_SPIRIT));
        registry.registerAction(PlayerDialogueTracker.STAY_MORTAL, p -> handleRemnantChoiceAction(p, MORTAL));
    }

    private static void handleRemnantChoiceAction(ServerPlayerEntity player, RemnantType chosenType) {
        DeathSuspender deathSuspender = DeathSuspender.get(player);
        if (deathSuspender.isLifeTransient()) {
            makeRemnantChoice(player, chosenType);
            deathSuspender.resumeDeath();
        }
    }

    public static void makeRemnantChoice(ServerPlayerEntity player, RemnantType chosenType) {
        RemnantComponent.get(player).become(chosenType);
        if (chosenType != MORTAL) {
            player.world.playSound(null, player.getX(), player.getY(), player.getZ(), RequiemSoundEvents.EFFECT_BECOME_REMNANT, player.getSoundCategory(), 1.4F, 0.1F);
            RequiemNetworking.sendTo(player, RequiemNetworking.createOpusUsePacket(chosenType, false));
        }
        RequiemCriteria.MADE_REMNANT_CHOICE.handle(player, chosenType);
    }

}
