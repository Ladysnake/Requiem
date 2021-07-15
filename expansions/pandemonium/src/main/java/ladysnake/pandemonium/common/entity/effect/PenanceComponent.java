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
package ladysnake.pandemonium.common.entity.effect;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public final class PenanceComponent implements ServerTickingComponent, ClientTickingComponent, AutoSyncedComponent {
    public static final ComponentKey<PenanceComponent> KEY = ComponentRegistry.getOrCreate(Requiem.id("penance"), PenanceComponent.class);
    public static final int PENANCE_WARNING_TIME = 30 * 20;

    private final PlayerEntity owner;
    private int timeWithPenance = -1;

    private transient float lastPenanceStrength;
    private transient float nextPenanceStrength;

    public PenanceComponent(PlayerEntity owner) {
        this.owner = owner;
    }

    public boolean shouldApplyPenance() {
        return this.timeWithPenance >= PENANCE_WARNING_TIME;
    }

    @CheckEnv(Env.CLIENT)
    public float getOverlayStrength(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastPenanceStrength, this.nextPenanceStrength);
    }

    @Override
    public void clientTick() {
        this.lastPenanceStrength = this.nextPenanceStrength;
        this.nextPenanceStrength = Math.min(1, (float) this.timeWithPenance / PENANCE_WARNING_TIME);
    }

    @Override
    public void serverTick() {
        StatusEffectInstance penance = this.owner.getStatusEffect(PandemoniumStatusEffects.PENANCE);
        if (penance != null && this.shouldSplitFromCurrentBody(penance)) {
            this.timeWithPenance++;
            if (this.shouldApplyPenance()) {
                PenanceStatusEffect.applyPenance(this.owner, penance.getAmplifier());
            }
            // if we sync here right after the player has respawned,
            // we will sync the old value to the new player, causing a desync
            // (connections are still functional after the player got removed)
            if (!this.owner.isRemoved()) {
                KEY.sync(this.owner);
            }
        } else if (this.timeWithPenance >= 0) {
            this.timeWithPenance = -1;
            KEY.sync(this.owner);
        }
    }

    private boolean shouldSplitFromCurrentBody(StatusEffectInstance penance) {
        switch (penance.getAmplifier()) {
            default:
                // fallthrough
            case PenanceStatusEffect.MOB_BAN_THRESHOLD:
                if (!RemnantComponent.isIncorporeal(this.owner)) return true;
                // fallthrough
            case PenanceStatusEffect.PLAYER_BAN_THRESHOLD:
                if (!RemnantComponent.isVagrant(this.owner)) return true;
                // fallthrough
            case PenanceStatusEffect.PREVENT_CURE_THRESHOLD:
        }
        return false;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.owner;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarInt(this.timeWithPenance);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        this.timeWithPenance = buf.readVarInt();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.timeWithPenance = tag.getInt("countdown");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("countdown", this.timeWithPenance);
    }
}
