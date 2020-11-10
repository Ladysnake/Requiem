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
package ladysnake.pandemonium.common.remnant;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.pandemonium.api.anchor.FractureAnchorManager;
import ladysnake.pandemonium.common.impl.anchor.EntityFractureAnchor;
import ladysnake.pandemonium.common.network.PandemoniumNetworking;
import ladysnake.requiem.Requiem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class PlayerBodyTracker implements ServerTickingComponent {
    public static final ComponentKey<PlayerBodyTracker> KEY = ComponentRegistry.getOrCreate(Requiem.id("body_tracker"), PlayerBodyTracker.class);

    public static PlayerBodyTracker get(PlayerEntity player) {
        return KEY.get(player);
    }

    private final PlayerEntity player;
    @Nullable
    private UUID anchorUuid;
    private float previousAnchorHealth = -1;

    public PlayerBodyTracker(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        FractureAnchor anchor = this.getAnchor();
        assert this.player instanceof ServerPlayerEntity;

        if (anchor instanceof EntityFractureAnchor) {
            Entity anchorEntity = ((EntityFractureAnchor) anchor).getEntity();
            if (anchorEntity instanceof LivingEntity) {
                float health = ((LivingEntity) anchorEntity).getHealth();
                if (health < this.previousAnchorHealth) {
                    PandemoniumNetworking.sendAnchorDamageMessage((ServerPlayerEntity) this.player, false);
                }
                this.previousAnchorHealth = health;
            }
        } else if (this.previousAnchorHealth > 0) {
            PandemoniumNetworking.sendAnchorDamageMessage((ServerPlayerEntity) this.player, true);
            this.previousAnchorHealth = -1;
        }
    }

    public void setAnchor(FractureAnchor anchor) {
        this.anchorUuid = anchor.getUuid();
    }

    @Nullable
    public FractureAnchor getAnchor() {
        return this.anchorUuid != null
                ? FractureAnchorManager.get(this.player.world).getAnchor(this.anchorUuid)
                : null;
    }

    @Override
    public void writeToNbt(@Nonnull CompoundTag tag) {
        if (this.anchorUuid != null) {
            tag.putUuid("AnchorUuid", this.anchorUuid);
        }
    }

    @Override
    public void readFromNbt(@Nonnull CompoundTag tag) {
        if (tag.containsUuid("AnchorUuid")) {
            this.anchorUuid = tag.getUuid("AnchorUuid");
        }
    }
}
