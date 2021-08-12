/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.requiem.core.possession;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.entity.MovementRegistry;
import ladysnake.requiem.api.v1.event.requiem.PossessionEvents;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStateChangeCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessedData;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.AttritionFocus;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.SoulbindingRegistry;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.RequiemCoreNetworking;
import ladysnake.requiem.core.mixin.access.LivingEntityAccessor;
import ladysnake.requiem.core.movement.SerializableMovementConfig;
import ladysnake.requiem.core.tag.RequiemCoreTags;
import ladysnake.requiem.core.util.InventoryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public final class PossessionComponentImpl implements PossessionComponent {
    private final PlayerEntity player;
    @Nullable
    private MobEntity possessed;
    private int conversionTimer;

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
            if (RequiemCoreTags.Entity.INVENTORY_CARRIERS.contains(host.getType())) {
                PossessedData.KEY.get(host).moveItems(player.getInventory(), false);
            }
            if (RequiemCoreTags.Entity.ITEM_USERS.contains(host.getType())) {
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
            if (RequiemCoreTags.Entity.EATERS.contains(host.getType())) {
                player.getHungerManager().readNbt(PossessedData.KEY.get(host).getHungerData());
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

        // Ensure health matches max health (attrition)
        host.setHealth(host.getHealth());

        // Make the mob react a bit
        host.playAmbientSound();

        //Set persistent
        host.setPersistent();

        // Fire event
        PossessionStateChangeCallback.EVENT.invoker().onPossessionStateChange(this.player, host);
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
        LivingEntity host = this.getHost();
        if (host != null) {
            this.resetState();
            ((Possessable) host).setPossessor(null);

            if (player instanceof ServerPlayerEntity serverPlayer) {
                Entity ridden = player.getVehicle();
                if (ridden != null) {
                    player.stopRiding();
                    host.startRiding(ridden);
                }

                if (transfer) {
                    dropEquipment(host, serverPlayer);
                }

                if (RequiemCoreTags.Entity.EATERS.contains(host.getType())) {
                    player.getHungerManager().writeNbt(PossessedData.KEY.get(host).getHungerData());
                }

                // move soulbound effects from the host to the soul
                // careful with ConcurrentModificationException
                for (StatusEffectInstance effect : host.getStatusEffects().toArray(new StatusEffectInstance[0])) {
                    if (SoulbindingRegistry.instance().isSoulbound(effect.getEffectType())) {
                        host.removeStatusEffect(effect.getEffectType());
                        player.addStatusEffect(new StatusEffectInstance(effect));
                    }
                }

                host.setSprinting(false);
            }
        }
    }

    public static void dropEquipment(LivingEntity possessed, ServerPlayerEntity player) {
        if (PossessionEvents.INVENTORY_TRANSFER_CHECK.invoker().shouldTransfer(player, possessed).get()) {
            if (RequiemCoreTags.Entity.ITEM_USERS.contains(possessed.getType())) {
                InventoryHelper.transferEquipment(player, possessed);
            }
            if (RequiemCoreTags.Entity.INVENTORY_CARRIERS.contains(possessed.getType())) {
                PossessedData.KEY.get(possessed).moveItems(player.getInventory(), true);
            }
            ((LivingEntityAccessor) player).requiem$invokeDropInventory();
        }
        player.clearStatusEffects();
        if (player.world.getLevelProperties().isHardcore()) {
            AttritionFocus.KEY.get(possessed).applyAttrition(player);
        }
        PossessionEvents.DISSOCIATION_CLEANUP.invoker().cleanUpAfterDissociation(player, possessed);
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeInt(this.possessed == null ? -1 : this.possessed.getId());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int possessedId = buf.readInt();
        Entity entity = player.world.getEntityById(possessedId);

        if (entity instanceof MobEntity) {
            this.startPossessing((MobEntity) entity);
            updateCamera(this.player, entity);
        } else {
            this.stopPossessing();
            updateCamera(this.player, this.player);
        }
    }

    @CheckEnv(Env.CLIENT)
    private static void updateCamera(PlayerEntity player, Entity cameraEntity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.getPerspective().isFirstPerson() && player == mc.player) {
            mc.gameRenderer.onCameraEntitySet(cameraEntity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @CheckForNull MobEntity getHost() {
        if (!this.isPossessionOngoing()) {
            return null;
        }

        if (this.possessed.isRemoved()) {
            UUID possessedUuid = this.possessed.getUuid();
            RequiemCore.LOGGER.debug("{}: this player's possessed entity has disappeared", this.player);
            this.resetState();
            // Attempt to find an equivalent entity using the UUID
            if (this.player.world instanceof ServerWorld) {
                Entity host = ((ServerWorld) this.player.world).getEntity(possessedUuid);
                if (host instanceof MobEntity && host instanceof Possessable) {
                    this.startPossessing((MobEntity) host);
                }
            }
        }
        return this.possessed;
    }

    private void resetState() {
        this.possessed = null;
        this.conversionTimer = 0;
        MovementAlterer.get(this.player).setConfig(RemnantComponent.get(this.player).isVagrant() ? SerializableMovementConfig.SOUL : null);
        this.player.calculateDimensions(); // update size
        this.player.setAir(this.player.getMaxAir());
        PossessionComponent.KEY.sync(this.player);
        PossessionStateChangeCallback.EVENT.invoker().onPossessionStateChange(this.player, null);
        RequiemCoreNetworking.sendToAllTrackingIncluding(player, new EntityAttributesS2CPacket(player.getId(), player.getAttributes().getAttributesToSend()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPossessionOngoing() {
        return this.possessed != null;
    }

    @Override
    public boolean canBeCured(ItemStack cure) {
        MobEntity possessedEntity = this.getHost();
        return possessedEntity != null
            && RequiemCoreTags.Item.UNDEAD_CURES.contains(cure.getItem())
            && possessedEntity.hasStatusEffect(StatusEffects.WEAKNESS)
            && RemnantComponent.get(this.player).canCurePossessed(possessedEntity);
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
    public boolean isCuring() {
        return this.conversionTimer > 0;
    }

    @Override
    public void serverTick() {
        if (this.player.isSpectator()) {
            this.stopPossessing();
        }
        if (this.isCuring()) {
            if (!this.isPossessionOngoing()) this.conversionTimer = 0;
            else this.conversionTimer--;

            if (this.conversionTimer == 0) {
                this.finishCuring();
            }
        }
    }

    private void finishCuring() {
        MobEntity possessedEntity = this.getHost();

        if (possessedEntity != null) {
            RemnantComponent.get(this.player).curePossessed(possessedEntity);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("conversionTimer", this.conversionTimer);
    }

    @Override
    public void readFromNbt(NbtCompound compound) {
        this.conversionTimer = compound.getInt("conversionTimer");
    }

    public static double reachSq(BlockPos pos, MobEntity host) {
        double width = host.getWidth();
        double dx = (host.getX() + width / 2) - (pos.getX() + 0.5D);
        double dy = (host.getY() + host.getEyeHeight(host.getPose())) - (pos.getY() + 0.5D);
        double dz = (host.getZ() + width / 2) - (pos.getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz;
    }
}
