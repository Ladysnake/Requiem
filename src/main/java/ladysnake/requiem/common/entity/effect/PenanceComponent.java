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
package ladysnake.requiem.common.entity.effect;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public final class PenanceComponent implements ServerTickingComponent, ClientTickingComponent, AutoSyncedComponent {
    public static final ComponentKey<PenanceComponent> KEY = ComponentRegistry.getOrCreate(Requiem.id("penance"), PenanceComponent.class);

    public static final byte DATA_SYNC = 0;
    public static final byte FX_SYNC = 1;

    public static final int PENANCE_WARNING_TIME = 30 * 20;
    public static final int PENANCE_FLASH_INTENSITY = 5;

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
        if (RemnantComponent.isIncorporeal(this.owner) && this.owner.hasStatusEffect(RequiemStatusEffects.PENANCE)) {
            this.nextPenanceStrength = 0.4f;
        } else {
            this.nextPenanceStrength = Math.min(1, (float) this.timeWithPenance / PENANCE_WARNING_TIME);
        }
    }

    @Override
    public void serverTick() {
        StatusEffectInstance penance = this.owner.getStatusEffect(RequiemStatusEffects.PENANCE);
        if (penance != null && this.shouldSplitFromCurrentBody(penance)) {
            this.timeWithPenance++;
            updatePenance(penance.getAmplifier());
        } else if (this.timeWithPenance >= 0) {
            this.timeWithPenance = -1;
            KEY.sync(this.owner);
        }
    }

    public @Nullable ServerPlayerEntity maxOutPenance(int amplifier) {
        this.timeWithPenance = PENANCE_WARNING_TIME;
        return this.updatePenance(amplifier);
    }

    public void resetPenanceTime() {
        this.timeWithPenance = 0;
    }

    private @Nullable ServerPlayerEntity updatePenance(int amplifier) {
        ServerPlayerEntity soul;

        if (this.shouldApplyPenance()) {
            var result = PenanceStatusEffect.applyPenance(this.owner, amplifier);
            if (result.split()) {
                // Probably not sending to the right player object, but they share the same connection anyway
                RequiemNetworking.sendEtherealAnimationMessage((ServerPlayerEntity) this.owner);
            }
            // Again doesn't matter which player object carries the message
            KEY.sync(this.owner, this::writeFxPacket);
            soul = result.soul();
        } else {
            soul = null;
        }

        // if we sync here right after the player has respawned,
        // we will sync the old value to the new player, causing a desync
        // (connections are still functional after the player got removed)
        if (!this.owner.isRemoved()) {
            KEY.sync(this.owner);
        }

        return soul;
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
        buf.writeByte(DATA_SYNC);
        buf.writeVarInt(this.timeWithPenance);
    }

    private void writeFxPacket(PacketByteBuf buf, ServerPlayerEntity player) {
        buf.writeByte(FX_SYNC);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        byte op = buf.readByte();
        switch (op) {
            case DATA_SYNC -> this.timeWithPenance = buf.readVarInt();
            case FX_SYNC -> RequiemClient.instance().fxRenderer().playEtherealPulseAnimation(PENANCE_FLASH_INTENSITY, RequiemStatusEffects.PENANCE.getColor());
        }
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
