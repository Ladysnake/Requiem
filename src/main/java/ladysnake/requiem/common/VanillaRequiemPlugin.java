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
import ladysnake.requiem.api.v1.event.minecraft.ItemPickupCallback;
import ladysnake.requiem.api.v1.event.minecraft.PlayerCloneCallback;
import ladysnake.requiem.api.v1.event.minecraft.PlayerRespawnCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.entity.ability.MeleeAbility;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

import static ladysnake.requiem.common.remnant.RemnantStates.LARVA;

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
        PossessionStartCallback.EVENT.register(Requiem.id("blacklist"), (target, possessor) -> {
            if (RequiemEntityTags.POSSESSION_BLACKLIST.contains(target.getType())) {
                return PossessionStartCallback.Result.DENY;
            }
            return PossessionStartCallback.Result.PASS;
        });
        PossessionStartCallback.EVENT.register(Requiem.id("allow_base_mobs"), (target, possessor) -> {
            if (target.isUndead() || target instanceof GolemEntity) {
                return PossessionStartCallback.Result.ALLOW;
            }
            return PossessionStartCallback.Result.PASS;
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
    }

    @Override
    public void registerMobAbilities(MobAbilityRegistry abilityRegistry) {
        abilityRegistry.register(EntityType.IRON_GOLEM, MobAbilityConfig.builder().directAttack(e -> new MeleeAbility(e, true)).build());
    }

    @Override
    public void registerRemnantStates(Registry<RemnantType> registry) {
        Registry.register(registry, Requiem.id("larva"), LARVA);
    }
}
