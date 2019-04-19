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
package ladysnake.requiem.common.impl.possession;

import com.google.common.collect.MapMaker;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.entity.ai.attribute.AttributeHelper;
import ladysnake.requiem.common.entity.ai.attribute.PossessionDelegatingAttribute;
import ladysnake.requiem.common.impl.movement.SerializableMovementConfig;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import ladysnake.requiem.common.util.InventoryHelper;
import ladysnake.requiem.mixin.entity.LivingEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static ladysnake.requiem.common.network.RequiemNetworking.createPossessionMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToAllTrackingIncluding;

public final class PossessionComponentImpl implements PossessionComponent {
    // Identity weak map. Should probably be made into its own util class.
    private static final Set<PlayerEntity> attributeUpdated = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private final PlayerEntity player;
    @Nullable private UUID possessedUuid;
    private int possessedNetworkId;

    public PossessionComponentImpl(PlayerEntity player) {
        this.player = player;
        this.possessedNetworkId = -1;
    }

    @Override
    public boolean canStartPossessing(final MobEntity mob) {
        RequiemPlayer dp = (RequiemPlayer) this.player;
        return player.world.isClient || (!player.isSpectator() && dp.isRemnant() && dp.getRemnantState().isIncorporeal());
    }

    @Override
    public boolean startPossessing(final MobEntity host) {
        // 1- check that the player can initiate possession
        if (!canStartPossessing(host)) {
            return false;
        }

        PossessionStartCallback.Result result = PossessionStartCallback.EVENT.invoker().onPossessionAttempted(host, this.player);
        if (result != PossessionStartCallback.Result.ALLOW) {
            return result.isSuccess();
        }

        Possessable possessable = (Possessable) host;
        // 2- check that the mob can be possessed
        if (!possessable.canBePossessedBy(player)) {
            return false;
        }
        // 3- transfer inventory and mount
        if (RequiemEntityTags.ITEM_USER.contains(host.getType())) {
            InventoryHelper.transferEquipment(host, player);
        }
        Entity ridden = ((Entity)possessable).getRiddenEntity();
        if (ridden != null) {
            ((MobEntity) possessable).stopRiding();
            player.startRiding(ridden);
        }
        // 4- Actually set the possessed entity
        this.possessedUuid = host.getUuid();
        this.possessedNetworkId = host.getEntityId();
        possessable.setPossessor(this.player);
        this.syncPossessed();
        // 5- Update some attributes
        this.player.setPositionAndAngles(host);
        this.player.refreshSize(); // update size
        ((RequiemPlayer)this.player).getMovementAlterer().setConfig(Requiem.getMovementAltererManager().getEntityMovementConfig(host.getType()));
        if (!attributeUpdated.contains(this.player)) {
            this.swapAttributes(this.player);
            attributeUpdated.add(this.player);
        }

        // 6- Make the mob react a bit
        host.playAmbientSound();
        return true;
    }

    private void swapAttributes(PlayerEntity player) {
        AbstractEntityAttributeContainer attributeMap = player.getAttributeContainer();
        // Replace every registered attribute
        for (EntityAttributeInstance current: attributeMap.values()) {
            EntityAttributeInstance replacement = new PossessionDelegatingAttribute(attributeMap, current, this);
            AttributeHelper.substituteAttributeInstance(attributeMap, replacement);
        }
    }

    @Override
    public void stopPossessing() {
        Possessable possessed = this.getPossessedEntity();
        if (possessed != null) {
            this.possessedUuid = null;
            resetState();
            possessed.setPossessor(null);
            if (player instanceof ServerPlayerEntity && !player.isCreative()) {
                LivingEntity possessedEntity = (LivingEntity) possessed;
                if (RequiemEntityTags.ITEM_USER.contains(possessedEntity.getType())) {
                    InventoryHelper.transferEquipment(player, (LivingEntity) possessed);
                }
                ((LivingEntityAccessor)player).invokeDropInventory();
            }
            Entity ridden = player.getRiddenEntity();
            if (ridden != null) {
                player.stopRiding();
                ((Entity)possessed).startRiding(ridden);
            }
        }
    }

    private void syncPossessed() {
        if (!this.player.world.isClient) {
            sendToAllTrackingIncluding(this.player, createPossessionMessage(this.player.getUuid(), this.possessedNetworkId));
        }
    }

    @CheckForNull
    @Override
    public Possessable getPossessedEntity() {
        if (!isPossessing()) {
            return null;
        }
        // First attempt: use the network id (client & server)
        Entity host = this.player.world.getEntityById(this.possessedNetworkId);
        if (host == null) {
            if (this.player.world instanceof ServerWorld) {
                // Second attempt: use the UUID (server)
                // method_14190 == getEntityByUuid
                host = ((ServerWorld)this.player.world).getEntity(this.getPossessedEntityUuid());
            }
            // Set the possessed uuid to null to avoid infinite recursion
            this.possessedUuid = null;
            if (host instanceof MobEntity && host instanceof Possessable) {
                this.startPossessing((MobEntity) host);
            } else {
                if (host != null) {
                    Requiem.LOGGER.warn("{}: this player's supposedly possessed entity ({}) cannot be possessed!", this.player, host);
                }
                Requiem.LOGGER.debug("{}: this player's possessed entity is nowhere to be found", this);
                this.resetState();
                host = null;
            }
        }
        return (Possessable) host;
    }

    private void resetState() {
        this.possessedNetworkId = -1;
        ((RequiemPlayer) this.player).getMovementAlterer().setConfig(SerializableMovementConfig.SOUL);
        this.player.refreshSize(); // update size
        this.player.setBreath(this.player.getMaxBreath());
        syncPossessed();
    }

    @Override
    public boolean isPossessing() {
        return this.possessedUuid != null;
    }

    @CheckForNull
    public UUID getPossessedEntityUuid() {
        return this.possessedUuid;
    }

}
