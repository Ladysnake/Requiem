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
package ladysnake.requiem.common.impl.remnant;

import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.common.RequiemComponents;
import ladysnake.requiem.common.util.DamageSourceSerialization;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.PacketByteBuf;

import javax.annotation.Nullable;

public class RevivingDeathSuspender implements DeathSuspender, EntitySyncedComponent {
    private boolean lifeTransient;
    private PlayerEntity player;
    @Nullable
    private DamageSource deathCause;

    public RevivingDeathSuspender(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void suspendDeath(DamageSource deathCause) {
        if (this.isLifeTransient()) {
            return;
        }
        this.player.setHealth(1f);
        this.player.setInvulnerable(true);
        this.player.abilities.invulnerable = true;
        this.deathCause = deathCause;
        this.setLifeTransient(true);
        this.markDirty();
    }

    @Override
    public boolean isLifeTransient() {
        return this.lifeTransient;
    }

    @Override
    public void setLifeTransient(boolean lifeTransient) {
        this.lifeTransient = lifeTransient;
    }

    @Override
    public void resumeDeath() {
        this.player.setInvulnerable(false);
        this.player.abilities.invulnerable = false;
        this.player.setHealth(0f);
        this.setLifeTransient(false);
        this.markDirty();
        this.player.onDeath(this.deathCause != null ? deathCause : DamageSource.GENERIC);
    }

    @Override
    public PlayerEntity getEntity() {
        return this.player;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return RequiemComponents.DEATH_SUSPENDER;
    }

    @Override
    public void markDirty() {
        EntitySyncedComponent.super.markDirty();
        this.syncWith((ServerPlayerEntity) this.player);
    }

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        buf.writeBoolean(this.isLifeTransient());
    }

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        this.setLifeTransient(buf.readBoolean());
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean("lifeTransient", this.lifeTransient);
        if (this.deathCause != null) {
            tag.put("deathCause", DamageSourceSerialization.toTag(this.deathCause));
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.setLifeTransient(tag.getBoolean("lifeTransient"));
        if (tag.contains("deathCause") && this.player.world.isClient) {
            this.deathCause = DamageSourceSerialization.fromTag(tag.getCompound("deathCause"), (ServerWorld)this.player.world);
        }
    }
}
