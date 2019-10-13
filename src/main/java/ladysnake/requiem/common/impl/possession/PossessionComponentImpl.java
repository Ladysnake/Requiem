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
import ladysnake.requiem.common.RequiemComponents;
import ladysnake.requiem.common.entity.ai.attribute.AttributeHelper;
import ladysnake.requiem.common.entity.ai.attribute.PossessionDelegatingAttribute;
import ladysnake.requiem.common.impl.movement.SerializableMovementConfig;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.util.InventoryHelper;
import ladysnake.requiem.mixin.possession.player.LivingEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static ladysnake.requiem.common.network.RequiemNetworking.createPossessionMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToAllTrackingIncluding;

public final class PossessionComponentImpl implements PossessionComponent {
    // Identity weak map. Should probably be made into its own util class.
    private static final Set<PlayerEntity> attributeUpdated = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private final PlayerEntity player;
    @Nullable private MobEntity possessed;
    private int conversionTimer = -1;

    public PossessionComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public RequiemPlayer asRequiemPlayer() {
        return RequiemPlayer.from(this.player);
    }

    private boolean isReadyForPossession() {
        RequiemPlayer dp = (RequiemPlayer) this.player;
        return player.world.isClient || (!player.isSpectator() && dp.asRemnant().isIncorporeal());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startPossessing(final MobEntity host, boolean simulate) {
        // Check that the player can initiate possession
        if (!isReadyForPossession()) {
            return false;
        }

        PossessionStartCallback.Result result = PossessionStartCallback.EVENT.invoker().onPossessionAttempted(host, this.player, simulate);
        if (result != PossessionStartCallback.Result.ALLOW) {
            return result.isSuccess();
        }

        Possessable possessable = (Possessable) host;
        // Check that the mob can be possessed
        if (!possessable.canBePossessedBy(player)) {
            return false;
        }
        if (!simulate) {
            startPossessing0(host, possessable);
        }
        return true;
    }

    private void startPossessing0(MobEntity host, Possessable possessable) {
        possessable.setPossessor(null);
        // Transfer inventory and mount
        if (!player.world.isClient) {
            if (RequiemEntityTypeTags.ITEM_USER.contains(host.getType())) {
                InventoryHelper.transferEquipment(host, player);
            }
            for (StatusEffectInstance effect : host.getStatusEffects()) {
                player.addPotionEffect(new StatusEffectInstance(effect));
            }
            Entity ridden = ((Entity) possessable).getVehicle();
            if (ridden != null) {
                ((MobEntity) possessable).stopRiding();
                player.startRiding(ridden);
            }
        }
        // Actually set the possessed entity
        this.possessed = host;
        possessable.setPossessor(this.player);
        this.syncPossessed(host.getEntityId());
        // Update some attributes
        this.player.copyPositionAndRotation(host);
        this.player.calculateDimensions(); // update size
        ((RequiemPlayer) this.player).getMovementAlterer().setConfig(RequiemComponents.MOVEMENT_ALTERER_MANAGER.get(this.player.world).getEntityMovementConfig(host.getType()));
        if (!attributeUpdated.contains(this.player)) {
            this.swapAttributes(this.player);
            attributeUpdated.add(this.player);
        }

        // 6- Make the mob react a bit
        host.playAmbientSound();
    }

    private void swapAttributes(PlayerEntity player) {
        AbstractEntityAttributeContainer attributeMap = player.getAttributeContainer();
        // Replace every registered attribute
        for (EntityAttributeInstance current: attributeMap.values()) {
            EntityAttributeInstance replacement = new PossessionDelegatingAttribute(attributeMap, current, this);
            AttributeHelper.substituteAttributeInstance(attributeMap, replacement);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopPossessing() {
        this.stopPossessing(!this.player.isCreative());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopPossessing(boolean transfer) {
        LivingEntity possessed = this.getPossessedEntity();
        if (possessed != null) {
            this.resetState();
            ((Possessable)possessed).setPossessor(null);
            if (player instanceof ServerPlayerEntity && transfer) {
                if (RequiemEntityTypeTags.ITEM_USER.contains(possessed.getType())) {
                    InventoryHelper.transferEquipment(player, possessed);
                }
                ((LivingEntityAccessor)player).invokeDropInventory();
                player.clearPotionEffects();
                Entity ridden = player.getVehicle();
                if (ridden != null) {
                    player.stopRiding();
                    possessed.startRiding(ridden);
                }
                this.conversionTimer = -1;
            }
        }
    }

    private void syncPossessed(int entityId) {
        if (!this.player.world.isClient) {
            sendToAllTrackingIncluding(this.player, createPossessionMessage(this.player.getUuid(), entityId));
        }
    }

    /**
     * {@inheritDoc}
     */
    @CheckForNull
    @Override
    public MobEntity getPossessedEntity() {
        if (!isPossessing()) {
            return null;
        }

        if (this.possessed.removed) {
            UUID possessedUuid = this.possessed.getUuid();
            Requiem.LOGGER.debug("{}: this player's possessed entity has disappeared", this.player);
            this.resetState();
            // Attempt to find an equivalent entity using the UUID
            if (this.player.world instanceof ServerWorld) {
                Entity host = ((ServerWorld)this.player.world).getEntity(possessedUuid);
                if (host instanceof MobEntity && host instanceof Possessable) {
                    this.startPossessing((MobEntity) host);
                }
            }
        }
        return this.possessed;
    }

    private void resetState() {
        this.possessed = null;
        ((RequiemPlayer) this.player).getMovementAlterer().setConfig(((RequiemPlayer)player).asRemnant().isSoul() ? SerializableMovementConfig.SOUL : null);
        this.player.calculateDimensions(); // update size
        this.player.setBreath(this.player.getMaxBreath());
        syncPossessed(-1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPossessing() {
        return this.possessed != null;
    }

    @Override
    public void startCuring() {
        Random rand = this.player.getRand();
        this.conversionTimer = rand.nextInt(1201) + 2400;  // a bit shorter than villager
        this.player.removeStatusEffect(StatusEffects.WEAKNESS);
        this.player.addPotionEffect(new StatusEffectInstance(StatusEffects.STRENGTH, conversionTimer, 0));
        this.player.world.playSound(null, this.player.x + 0.5D, this.player.y + 0.5D, this.player.z + 0.5D, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.player.getSoundCategory(), 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);
    }

    @Override
    public void update() {
        if (!this.player.world.isClient && this.conversionTimer > 0) {
            this.conversionTimer--;
            if (this.conversionTimer == 0) {
                MobEntity possessedEntity = this.getPossessedEntity();
                if (possessedEntity != null) {
                    this.asRequiemPlayer().asRemnant().setSoul(false);
                    possessedEntity.remove();
                    this.player.addPotionEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
                    this.player.world.playLevelEvent(null, 1027, new BlockPos(this.player), 0);
                }
                this.conversionTimer = -1;
            }
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("conversionTimer", this.conversionTimer);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag compound) {
        this.conversionTimer = compound.getInt("conversionTimer");
    }
}
