/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
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
package ladysnake.requiem.core.remnant;

import io.github.ladysnake.blabber.Blabber;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.core.RequiemCore;
import ladysnake.requiem.core.util.serde.DamageSourceSerialization;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;

public final class RevivingDeathSuspender implements DeathSuspender {
    public static final AbilitySource DEATH_SUSPENSION_ABILITIES = Pal.getAbilitySource(RequiemCore.id("death_suspension"), AbilitySource.FREE);
    public static final int TIME_BEFORE_DIALOGUE = 20;

    private final PlayerEntity player;
    private boolean lifeTransient;
    @Nullable
    private DamageSource deathCause;
    private int timeBeforeDialogue;

    public RevivingDeathSuspender(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void suspendDeath(DamageSource deathCause) {
        if (this.isLifeTransient() || this.player.getClass() != ServerPlayerEntity.class) {
            return;
        }
        this.player.setHealth(1f);
        this.player.setInvulnerable(true);
        Pal.grantAbility(player, VanillaAbilities.INVULNERABLE, DEATH_SUSPENSION_ABILITIES);
        this.deathCause = deathCause;
        this.timeBeforeDialogue = TIME_BEFORE_DIALOGUE;
        this.setLifeTransient(true);
        DeathSuspender.KEY.sync(this.player);
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
        Pal.revokeAbility(player, VanillaAbilities.INVULNERABLE, DEATH_SUSPENSION_ABILITIES);
        this.player.setHealth(0f);
        this.setLifeTransient(false);
        DeathSuspender.KEY.sync(this.player);
        this.player.onDeath(this.deathCause != null ? deathCause : DamageSource.GENERIC);
    }

    @Override
    public void serverTick() {
        if (this.isLifeTransient()) {
            if (--timeBeforeDialogue == 0) {
                Blabber.startDialogue((ServerPlayerEntity) this.player, RequiemCore.id("remnant_choice"));
            }
        }
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity player) {
        buf.writeBoolean(this.isLifeTransient());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.setLifeTransient(buf.readBoolean());
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("lifeTransient", this.lifeTransient);
        if (this.deathCause != null) {
            tag.put("deathCause", DamageSourceSerialization.toTag(this.deathCause));
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.setLifeTransient(tag.getBoolean("lifeTransient"));
        if (tag.contains("deathCause") && this.player.world.isClient) {
            this.deathCause = DamageSourceSerialization.fromTag(tag.getCompound("deathCause"), (ServerWorld)this.player.world);
        }
    }
}
