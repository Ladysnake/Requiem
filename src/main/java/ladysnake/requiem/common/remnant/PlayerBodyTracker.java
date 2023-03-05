/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.common.remnant;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.api.v1.record.GlobalRecordKeeper;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public final class PlayerBodyTracker implements ServerTickingComponent {
    public static final ComponentKey<PlayerBodyTracker> KEY = ComponentRegistry.getOrCreate(Requiem.id("body_tracker"), PlayerBodyTracker.class);
    private final PlayerEntity player;
    @Nullable
    private UUID anchorUuid;
    private float previousAnchorHealth = -1;

    public PlayerBodyTracker(PlayerEntity player) {
        this.player = player;
    }

    public static PlayerBodyTracker get(PlayerEntity player) {
        return KEY.get(player);
    }

    @Override
    public void serverTick() {
        assert this.player instanceof ServerPlayerEntity;

        this.getAnchor().ifPresentOrElse(
            this::updateBodyHealth,
            this::onBodyDisappeared
        );
    }

    public Optional<GlobalRecord> getAnchor() {
        return Optional.ofNullable(this.anchorUuid).flatMap(GlobalRecordKeeper.get(this.player.world)::getRecord);
    }

    public void setAnchor(GlobalRecord anchor) {
        this.anchorUuid = anchor.getUuid();
    }

    @Override
    public void writeToNbt(@Nonnull NbtCompound tag) {
        if (this.anchorUuid != null) {
            tag.putUuid("AnchorUuid", this.anchorUuid);
        }
    }

    @Override
    public void readFromNbt(@Nonnull NbtCompound tag) {
        if (tag.containsUuid("AnchorUuid")) {
            this.anchorUuid = tag.getUuid("AnchorUuid");
        }
    }

    private void updateBodyHealth(GlobalRecord anchor) {
        ServerPlayerEntity player = (ServerPlayerEntity) this.player;

        if (anchor.get(RequiemRecordTypes.ENTITY_REF).flatMap(ptr -> ptr.resolve(player.server)).orElse(null) instanceof LivingEntity anchorEntity) {
            float health = anchorEntity.getHealth();

            if (health < this.previousAnchorHealth) {
                RequiemNetworking.sendAnchorDamageMessage(player, false);
            }

            this.previousAnchorHealth = health;
        }
    }

    private void onBodyDisappeared() {
        if (this.previousAnchorHealth > 0) {
            RequiemNetworking.sendAnchorDamageMessage((ServerPlayerEntity) this.player, true);
            this.previousAnchorHealth = -1;
        }
    }
}
