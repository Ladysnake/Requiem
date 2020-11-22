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
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
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
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import ladysnake.requiem.common.impl.remnant.dialogue.PlayerDialogueTracker;
import ladysnake.requiem.common.impl.resurrection.ResurrectionDataLoader;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.remnant.BasePossessionHandlers;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

    public static boolean canCure(LivingEntity possessedEntity, ItemStack cure) {
        return !possessedEntity.world.getGameRules().getBoolean(RequiemGamerules.NO_CURE)
            && possessedEntity.isUndead()
            && RequiemItemTags.UNDEAD_CURES.contains(cure.getItem())
            && possessedEntity.hasStatusEffect(StatusEffects.WEAKNESS);
    }

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
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> getInteractionResult(player));
        // Prevent incorporeal players from interacting with anything
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> getInteractionResult(player));
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> !player.world.isClient && isInteractionForbidden(player) ? ActionResult.FAIL : ActionResult.PASS);
        UseItemCallback.EVENT.register((player, world, hand) -> new TypedActionResult<>(getInteractionResult(player), player.getStackInHand(hand)));
        // Make players respawn in the right place with the right state
        PrepareRespawnCallback.EVENT.register((original, clone, returnFromEnd) -> RemnantComponent.get(clone).prepareRespawn(original, returnFromEnd));
        PlayerRespawnCallback.EVENT.register(((player, returnFromEnd) -> {
            player.sendAbilitiesUpdate();
            RemnantComponent.KEY.sync(player);
            ((MobResurrectable) player).spawnResurrectionEntity();
        }));
        RemnantStateChangeCallback.EVENT.register((player, remnant) ->
            InventoryLimiter.KEY.get(player).setEnabled(remnant.isSoul())
        );
    }

    @Nonnull
    private ActionResult getInteractionResult(PlayerEntity player) {
        return isInteractionForbidden(player) ? ActionResult.FAIL : ActionResult.PASS;
    }

    private boolean isInteractionForbidden(PlayerEntity player) {
        return !player.isCreative() && RemnantComponent.get(player).isIncorporeal() || DeathSuspender.get(player).isLifeTransient();
    }

    private void registerPossessionEventHandlers() {
        BasePossessionHandlers.register();
        // Proxy melee attacks
        AttackEntityCallback.EVENT.register((playerEntity, world, hand, target, hitResult) -> {
            LivingEntity possessed = PossessionComponent.get(playerEntity).getPossessedEntity();
            if (possessed != null && !possessed.removed) {
                if (possessed.world.isClient || target != possessed && MobAbilityController.get(possessed).useDirect(AbilityType.ATTACK, target)) {
                    playerEntity.resetLastAttackedTicks();
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
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
        PossessionStateChangeCallback.EVENT.register((player, possessed) -> {
                InventoryLimiter inventoryLimiter = InventoryLimiter.KEY.get(player);
                if (possessed == null) {
                    for (InventoryPart part : InventoryPart.VALUES) {
                        inventoryLimiter.lock(part);
                    }
                } else {
                    if (RequiemEntityTypeTags.FULL_INVENTORY.contains(possessed.getType())) {
                        inventoryLimiter.unlock(InventoryPart.MAIN);
                    } else {
                        inventoryLimiter.lock(InventoryPart.MAIN);
                    }
                    if (RequiemEntityTypeTags.ITEM_USERS.contains(possessed.getType())) {
                        inventoryLimiter.unlock(InventoryPart.HANDS);
                        inventoryLimiter.unlock(InventoryPart.CRAFTING);
                    } else {
                        inventoryLimiter.lock(InventoryPart.HANDS);
                        inventoryLimiter.lock(InventoryPart.CRAFTING);
                    }
                }
            }
        );
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry abilityRegistry) {
    }

    @Override
    public void registerRemnantStates(Registry<RemnantType> registry) {
        Registry.register(registry, Requiem.id("remnant"), RemnantTypes.REMNANT);
    }

    @Override
    public void registerSoulBindings(SoulbindingRegistry registry) {
        registry.registerSoulbound(RequiemStatusEffects.ATTRITION);
    }

    @Override
    public void registerDialogueActions(DialogueRegistry registry) {
        registry.registerAction(PlayerDialogueTracker.BECOME_REMNANT, p -> handleRemnantChoiceAction(p, RemnantTypes.REMNANT));
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
            RequiemNetworking.sendTo(player, RequiemNetworking.createOpusUsePacket(false, false));
        }
        RequiemCriteria.MADE_REMNANT_CHOICE.handle(player, chosenType);
    }

}
