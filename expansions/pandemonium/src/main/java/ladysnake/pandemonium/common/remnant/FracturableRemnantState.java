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

import ladysnake.pandemonium.api.PandemoniumWorld;
import ladysnake.pandemonium.api.anchor.FractureAnchor;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.pandemonium.common.impl.anchor.EntityFractureAnchor;
import ladysnake.requiem.common.impl.remnant.MutableRemnantState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

import static ladysnake.pandemonium.common.network.PandemoniumNetworking.createAnchorDamageMessage;
import static ladysnake.requiem.common.network.RequiemNetworking.sendTo;

public class FracturableRemnantState extends MutableRemnantState {
    @Nullable
    private UUID anchorUuid;
    private float previousAnchorHealth = -1;

    public FracturableRemnantState(RemnantType type, PlayerEntity owner) {
        super(type, owner);
    }

    @Override
    public void update() {
        FractureAnchor anchor = this.getAnchor();
        if (this.player instanceof ServerPlayerEntity) {
            if (anchor instanceof EntityFractureAnchor) {
                Entity anchorEntity = ((EntityFractureAnchor) anchor).getEntity();
                if (anchorEntity instanceof LivingEntity) {
                    float health = ((LivingEntity) anchorEntity).getHealth();
                    if (health < this.previousAnchorHealth) {
                        sendTo((ServerPlayerEntity) this.player, createAnchorDamageMessage(false));
                    }
                    this.previousAnchorHealth = health;
                }
            } else if (this.previousAnchorHealth > 0) {
                sendTo((ServerPlayerEntity) this.player, createAnchorDamageMessage(true));
                this.previousAnchorHealth = -1;
            }
        }
    }

    @Nullable
    public FractureAnchor getAnchor() {
        return this.anchorUuid != null
                ? ((PandemoniumWorld) player.world).getAnchorManager().getAnchor(this.anchorUuid)
                : null;
    }

    @Nonnull
    @Override
    public CompoundTag toTag(@Nonnull CompoundTag tag) {
        super.toTag(tag);
        if (this.anchorUuid != null) {
            tag.putUuid("AnchorUuid", this.anchorUuid);
        }
        return tag;
    }

    @Override
    public void fromTag(@Nonnull CompoundTag tag) {
        super.fromTag(tag);
        if (tag.hasUuid("AnchorUuid")) {
            this.anchorUuid = tag.getUuid("AnchorUuid");
        }
    }
}
