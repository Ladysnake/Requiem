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
package ladysnake.requiem.common.impl.remnant;

import com.google.common.base.Preconditions;
import ladysnake.requiem.api.v1.event.requiem.RemnantStateChangeCallback;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class RemnantComponentImpl implements RemnantComponent {
    public static final String ETHEREAL_TAG = "ethereal";

    private final PlayerEntity player;

    private RemnantState state = NullRemnantState.NULL_STATE;
    private RemnantType remnantType = RemnantTypes.MORTAL;
    private boolean uninitializedDefaultRemnantType = true;
    private @Nullable RemnantType defaultRemnantType;

    public RemnantComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void become(RemnantType type) {
        if (type == this.remnantType) {
            return;
        }

        boolean wasSoul = this.isVagrant();
        RemnantState oldHandler = this.state;
        RemnantState handler = type.create(this.player);
        this.state.teardown(handler);
        this.state = handler;
        this.remnantType = type;
        this.state.setup(oldHandler);
        RemnantComponent.KEY.sync(this.player);
        this.fireRemnantStateChange(wasSoul);
    }

    @Override
    public RemnantType getRemnantType() {
        return this.remnantType;
    }

    @Override
    public boolean isIncorporeal() {
        return this.state.isIncorporeal();
    }

    @Override
    public boolean isVagrant() {
        return this.state.isVagrant();
    }

    @Override
    public boolean setVagrant(boolean vagrant) {
        boolean soul = this.isVagrant();

        if (soul != vagrant) {
            if (this.state.setVagrant(vagrant)) {
                this.fireRemnantStateChange(soul);
                return true;
            }
            return false;
        }
        return true;
    }

    private void fireRemnantStateChange(boolean wasSoul) {
        boolean nowSoul = this.isVagrant();

        if (wasSoul != nowSoul) {
            RemnantStateChangeCallback.EVENT.invoker().onRemnantStateChange(this.player, this);
        }
    }

    @Override
    public boolean canRegenerateBody() {
        return this.state.canRegenerateBody();
    }

    @Override
    public void curePossessed(LivingEntity body) {
        Preconditions.checkState(!this.player.world.isClient);
        if (!this.player.world.getGameRules().getBoolean(RequiemGamerules.NO_CURE)) {
            this.state.curePossessed(body);
        }
    }

    @Override
    public boolean canDissociateFrom(MobEntity possessed) {
        return this.state.canDissociateFrom(possessed);
    }

    @Override
    public void prepareRespawn(ServerPlayerEntity original, boolean lossless) {
        this.state.prepareRespawn(original, lossless);
    }

    @Override
    public void setDefaultRemnantType(@Nullable RemnantType defaultRemnantType) {
        this.uninitializedDefaultRemnantType = false;
        this.defaultRemnantType = defaultRemnantType;
    }

    @Override
    public @Nullable RemnantType getDefaultRemnantType() {
        if (this.uninitializedDefaultRemnantType) {
            this.defaultRemnantType = this.player.world.getGameRules().get(RequiemGamerules.STARTING_SOUL_MODE).get().getRemnantType();
            this.uninitializedDefaultRemnantType = false;
        }
        return this.defaultRemnantType;
    }

    @Override
    public void serverTick() {
        this.state.serverTick();
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarInt(RemnantTypes.getRawId(this.remnantType));
        buf.writeBoolean(this.isVagrant());
        RemnantType defaultRemnantType = this.getDefaultRemnantType();
        buf.writeVarInt(defaultRemnantType == null ? 0 : RemnantTypes.getRawId(defaultRemnantType) + 1);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int remnantId = buf.readVarInt();
        boolean soul = buf.readBoolean();
        int defaultRemnantId = buf.readVarInt();

        this.become(RemnantTypes.get(remnantId));
        this.setVagrant(soul);
        this.setDefaultRemnantType(defaultRemnantId == 0 ? null : RemnantTypes.get(defaultRemnantId - 1));
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        RemnantType remnantType = RemnantTypes.get(new Identifier(compoundTag.getString("id")));
        this.become(remnantType);
        this.setVagrant(compoundTag.getBoolean(ETHEREAL_TAG));
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        compoundTag.putString("id", RemnantTypes.getId(this.remnantType).toString());
        compoundTag.putBoolean(ETHEREAL_TAG, this.isVagrant());
    }
}
