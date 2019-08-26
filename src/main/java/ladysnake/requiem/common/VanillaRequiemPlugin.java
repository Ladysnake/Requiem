/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.requiem.common;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.event.minecraft.ItemPickupCallback;
import ladysnake.requiem.api.v1.event.minecraft.LivingEntityDropCallback;
import ladysnake.requiem.api.v1.event.minecraft.PlayerCloneCallback;
import ladysnake.requiem.api.v1.event.minecraft.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.api.v1.remnant.MobResurrectable;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.impl.remnant.dialogue.DialogueTrackerImpl;
import ladysnake.requiem.common.impl.resurrection.ResurrectionDataLoader;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.remnant.BasePossessionHandlers;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.tag.RequiemItemTags;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

import static ladysnake.requiem.common.network.RequiemNetworking.createCorporealityMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToAllTrackingIncluding;
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
    ).setSerialize(false);

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
                return ((RequiemPlayer) lazarus).asRemnant().getType().isDemon();
            }
            return false;
        });
    }

    private void registerEtherealEventHandlers() {
        // Prevent incorporeal players from picking up anything
        ItemPickupCallback.EVENT.register((player, pickedUp) -> {
            if (isInteractionForbidden(player)) {
                Entity possessed = ((RequiemPlayer)player).asPossessor().getPossessedEntity();
                if (possessed == null || !RequiemEntityTypeTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });
        // Prevent incorporeal players from breaking anything
        AttackBlockCallback.EVENT.register((player, world, hand, blockPos, facing) -> isInteractionForbidden(player) ? ActionResult.FAIL : ActionResult.PASS);
        // Prevent incorporeal players from hitting anything
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> isInteractionForbidden(player) ? ActionResult.FAIL : ActionResult.PASS);
        // Prevent incorporeal players from interacting with anything
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> isInteractionForbidden(player) ? ActionResult.FAIL : ActionResult.PASS);
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> !player.world.isClient && isInteractionForbidden(player) ? ActionResult.FAIL : ActionResult.PASS);
        UseItemCallback.EVENT.register((player, world, hand) -> isInteractionForbidden(player) ? ActionResult.FAIL : ActionResult.PASS);
        // Make players respawn in the right place with the right state
        PlayerCloneCallback.EVENT.register((original, clone, returnFromEnd) -> {
            RequiemPlayer requiemClone = RequiemPlayer.from(clone);
            requiemClone.become(RequiemPlayer.from(original).asRemnant().getType());
            requiemClone.asRemnant().copyFrom(original, returnFromEnd);
        });
        PlayerRespawnCallback.EVENT.register(((player, returnFromEnd) -> {
            sendToAllTrackingIncluding(player, createCorporealityMessage(player));
            ((MobResurrectable)player).spawnResurrectionEntity();
        }));
    }

    private boolean isInteractionForbidden(PlayerEntity player) {
        return !player.isCreative() && ((RequiemPlayer) player).asRemnant().isIncorporeal() || ((RequiemPlayer) player).getDeathSuspender().isLifeTransient();
    }

    private void registerPossessionEventHandlers() {
        BasePossessionHandlers.register();
        // Proxy melee attacks
        AttackEntityCallback.EVENT.register((playerEntity, world, hand, target, hitResult) -> {
            LivingEntity possessed = ((RequiemPlayer)playerEntity).asPossessor().getPossessedEntity();
            if (possessed != null && !possessed.removed) {
                if (possessed.world.isClient || target != possessed && ((Possessable)possessed).getMobAbilityController().useDirect(AbilityType.ATTACK, target)) {
                    playerEntity.resetLastAttackedTicks();
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack heldItem = player.getStackInHand(hand);
            if (RequiemItemTags.BONES.contains(heldItem.getItem())) {
                if (!world.isClient) {
                    MobEntity possessedEntity = ((RequiemPlayer) player).asPossessor().getPossessedEntity();
                    if (possessedEntity != null && EntityTypeTags.SKELETONS.contains(possessedEntity.getType())) {
                        if (possessedEntity.getHealth() < possessedEntity.getHealthMaximum()) {
                            possessedEntity.heal(4.0f);
                            possessedEntity.playAmbientSound();
                            heldItem.decrement(1);
                            player.getItemCooldownManager().set(heldItem.getItem(), 40);
                        }
                    }
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof SpiderEntity) {
                LivingEntity possessed = ((RequiemPlayer)player).asPossessor().getPossessedEntity();
                if (possessed instanceof SkeletonEntity) {
                    if (!world.isClient) {
                        possessed.startRiding(entity);
                    }
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry abilityRegistry) {
    }

    @Override
    public void registerRemnantStates(Registry<RemnantType> registry) {
        Registry.register(registry, Requiem.id("remnant"), RemnantTypes.REMNANT);
    }

    @Override
    public void registerDialogueActions(DialogueRegistry serverRegistry) {
        serverRegistry.registerAction(DialogueTrackerImpl.BECOME_REMNANT, p -> handleRemnantChoiceAction(p, RemnantTypes.REMNANT));
        serverRegistry.registerAction(DialogueTrackerImpl.STAY_MORTAL, p -> handleRemnantChoiceAction(p, MORTAL));
    }

    private static void handleRemnantChoiceAction(ServerPlayerEntity player, RemnantType chosenType) {
        RequiemPlayer requiemPlayer = RequiemPlayer.from(player);
        DeathSuspender deathSuspender = requiemPlayer.getDeathSuspender();
        if (deathSuspender.isLifeTransient()) {
            requiemPlayer.become(chosenType);
            if (chosenType != MORTAL) {
                player.world.playSound(null, player.x, player.y, player.z, RequiemSoundEvents.EFFECT_BECOME_REMNANT, player.getSoundCategory(), 1.4F, 0.1F);
                RequiemNetworking.sendTo(player, RequiemNetworking.createOpusUsePacket(false, false));
            }
            RequiemCriteria.MADE_REMNANT_CHOICE.handle(player, chosenType);
            deathSuspender.resumeDeath();
        }
    }

}
