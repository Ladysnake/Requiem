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
package ladysnake.requiem.common.impl.possession;

import com.google.common.collect.MapMaker;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.entity.MovementRegistry;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStateChangeCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.entity.attribute.DelegatingAttribute;
import ladysnake.requiem.common.entity.attribute.PossessionDelegatingAttribute;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.impl.movement.SerializableMovementConfig;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.util.InventoryHelper;
import ladysnake.requiem.mixin.common.access.LivingEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class PossessionComponentImpl implements PossessionComponent {
    // Identity weak map. Should probably be made into its own util class.
    private static final Set<PlayerEntity> attributeUpdated = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private final PlayerEntity player;
    @Nullable private MobEntity possessed;
    private int conversionTimer = -1;

    public PossessionComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    private boolean isReadyForPossession() {
        return player.world.isClient || (!player.isSpectator() && RemnantComponent.get(this.player).isIncorporeal());
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
            if (RequiemEntityTypeTags.ITEM_USERS.contains(host.getType())) {
                InventoryHelper.transferEquipment(host, player);
            }
            for (StatusEffectInstance effect : player.getStatusEffects()) {
                if (SoulbindingRegistry.instance().isSoulbound(effect.getEffectType())) {
                    host.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
            for (StatusEffectInstance effect : host.getStatusEffects()) {
                player.addStatusEffect(new StatusEffectInstance(effect));
            }
            Entity ridden = ((Entity) possessable).getVehicle();
            if (ridden != null) {
                ((MobEntity) possessable).stopRiding();
                player.startRiding(ridden);
            }
            host.setTarget(null);
        }
        // Actually set the possessed entity
        this.possessed = host;
        possessable.setPossessor(this.player);
        PossessionComponent.KEY.sync(this.player);
        // Update some attributes
        this.player.copyPositionAndRotation(host);
        this.player.calculateDimensions(); // update size
        MovementAlterer.get(this.player).setConfig(MovementRegistry.get(this.player.world).getEntityMovementConfig(host.getType()));

        if (!attributeUpdated.contains(this.player)) {
            this.swapAttributes(this.player);
            attributeUpdated.add(this.player);
        }

        // Ensure health matches max health (attrition)
        host.setHealth(host.getHealth());

        // Make the mob react a bit
        host.playAmbientSound();

        // Fire event
        PossessionStateChangeCallback.EVENT.invoker().onPossessionStateChange(this.player, host);
    }

    private void swapAttributes(PlayerEntity player) {
        AttributeContainer attributeMap = player.getAttributes();
        // Replace every registered attribute
        for (EntityAttribute attribute : Registry.ATTRIBUTE) {
            // Note: this fills the attribute map for the player, whether this is an issue is to be determined
            EntityAttributeInstance current = player.getAttributeInstance(attribute);
            if (current != null) {
                DelegatingAttribute.replaceAttribute(attributeMap, new PossessionDelegatingAttribute(current, this));
            }
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
            if (player instanceof ServerPlayerEntity) {
                if (transfer) {
                    if (RequiemEntityTypeTags.ITEM_USERS.contains(possessed.getType())) {
                        InventoryHelper.transferEquipment(player, possessed);
                    }
                    ((LivingEntityAccessor) player).invokeDropInventory();
                    player.clearStatusEffects();
                    RequiemNetworking.sendToAllTrackingIncluding(player, new EntityAttributesS2CPacket(player.getEntityId(), player.getAttributes().getAttributesToSend()));
                    Entity ridden = player.getVehicle();
                    if (ridden != null) {
                        player.stopRiding();
                        possessed.startRiding(ridden);
                    }
                    this.conversionTimer = -1;
                }
                // move soulbound effects from the host to the soul
                // careful with ConcurrentModificationException
                for (StatusEffectInstance effect : possessed.getStatusEffects().toArray(new StatusEffectInstance[0])) {
                    if (SoulbindingRegistry.instance().isSoulbound(effect.getEffectType())) {
                        possessed.removeStatusEffect(effect.getEffectType());
                        player.addStatusEffect(new StatusEffectInstance(effect));
                    }
                }
            }
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeInt(this.possessed == null ? -1 : this.possessed.getEntityId());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int possessedId = buf.readInt();
        Entity entity = player.world.getEntityById(possessedId);

        if (entity instanceof MobEntity) {
            this.startPossessing((MobEntity) entity);
            RequiemClient.INSTANCE.updateCamera(this.player, entity);
        } else {
            this.stopPossessing();
            RequiemClient.INSTANCE.updateCamera(this.player, this.player);
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
        MovementAlterer.get(this.player).setConfig(RemnantComponent.get(this.player).isSoul() ? SerializableMovementConfig.SOUL : null);
        this.player.calculateDimensions(); // update size
        this.player.setAir(this.player.getMaxAir());
        PossessionComponent.KEY.sync(this.player);
        PossessionStateChangeCallback.EVENT.invoker().onPossessionStateChange(this.player, null);
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
        if (!this.player.world.isClient) {
            Random rand = this.player.getRandom();
            this.conversionTimer = rand.nextInt(1201) + 2400;  // a bit shorter than villager
            this.player.removeStatusEffect(StatusEffects.WEAKNESS);
            this.player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, conversionTimer, 0));
            this.player.world.playSound(null, this.player.getX() + 0.5D, this.player.getY() + 0.5D, this.player.getZ() + 0.5D, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, this.player.getSoundCategory(), 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);
        }
    }

    @Override
    public void serverTick() {
        if (this.conversionTimer > 0) {
            this.conversionTimer--;
            if (this.conversionTimer == 0) {
                MobEntity possessedEntity = this.getPossessedEntity();
                if (possessedEntity != null) {
                    RemnantComponent.get(this.player).setSoul(false);
                    RequiemCriteria.TRANSFORMED_POSSESSED_ENTITY.handle((ServerPlayerEntity) this.player, possessedEntity, this.player, true);
                    possessedEntity.remove();
                    this.player.removeStatusEffect(RequiemStatusEffects.ATTRITION);
                    this.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
                    this.player.world.syncWorldEvent(null, 1027, this.player.getBlockPos(), 0);
                }
                this.conversionTimer = -1;
            }
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putInt("conversionTimer", this.conversionTimer);
    }

    @Override
    public void readFromNbt(CompoundTag compound) {
        this.conversionTimer = compound.getInt("conversionTimer");
    }
}
