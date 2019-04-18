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
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityConfig;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.event.ItemPickupCallback;
import ladysnake.requiem.api.v1.event.PlayerCloneCallback;
import ladysnake.requiem.api.v1.event.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.event.PossessionStartCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.requiem.common.entity.PlayerShellEntity;
import ladysnake.requiem.common.entity.ability.*;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

import static ladysnake.requiem.common.remnant.RemnantStates.LARVA;
import static ladysnake.requiem.common.remnant.RemnantStates.YOUNG;

public class VanillaRequiemPlugin implements RequiemPlugin {

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
    }

    private void registerEtherealEventHandlers() {
        // Prevent incorporeal players from picking up anything
        ItemPickupCallback.EVENT.register((player, pickedUp) -> {
            if (!player.isCreative() && RemnantState.getIfRemnant(player).filter(RemnantState::isSoul).isPresent()) {
                Entity possessed = (Entity) ((RequiemPlayer)player).getPossessionComponent().getPossessedEntity();
                if (possessed == null || !RequiemEntityTags.ITEM_USER.contains(possessed.getType())) {
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
        PlayerCloneCallback.EVENT.register(((original, clone, returnFromEnd) -> ((RequiemPlayer)original).getRemnantState().onPlayerClone(clone, !returnFromEnd)));
        PlayerRespawnCallback.EVENT.register(((player, returnFromEnd) -> player.onTeleportationDone()));
    }

    private void registerPossessionEventHandlers() {
        // Start possession on right click
        UseEntityCallback.EVENT.register((player, world, hand, target, hitPosition) -> {
            RemnantState state = ((RequiemPlayer) player).getRemnantState();
            if (state.isIncorporeal()) {
                if (target instanceof MobEntity && target.world.isClient) {
                    RequiemFx.INSTANCE.beginFishEyeAnimation(target);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
        // Proxy melee attacks
        AttackEntityCallback.EVENT.register((playerEntity, world, hand, target, hitResult) -> {
            LivingEntity possessed = (LivingEntity) ((RequiemPlayer)playerEntity).getPossessionComponent().getPossessedEntity();
            if (possessed != null && !possessed.removed) {
                if (possessed.world.isClient || ((Possessable)possessed).getMobAbilityController().useDirect(AbilityType.ATTACK, target)) {
                    return ActionResult.SUCCESS;
                }
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
        PossessionStartCallback.EVENT.register((target, possessor) -> {
            if (target instanceof PlayerShellEntity) {
                ((PlayerShellEntity) target).onSoulInteract(possessor);
                return ActionResult.SUCCESS;
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
        abilityRegistry.register(EntityType.GUARDIAN, MobAbilityConfig.<GuardianEntity>builder().directAttack(GuardianBeamAbility::new).build());
        abilityRegistry.register(EntityType.IRON_GOLEM, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
        abilityRegistry.register(EntityType.OCELOT, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
    }

    @Override
    public void registerRemnantStates(Registry<RemnantType> registry) {
        Registry.register(registry, Requiem.id("larva"), LARVA);
        Registry.register(registry, Requiem.id("young"), YOUNG);
    }
}
