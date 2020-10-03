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

import dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantState;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class RemnantComponentImpl implements RemnantComponent, AutoSyncedComponent {
    private final PlayerEntity player;

    private RemnantState state = NullRemnantState.NULL_STATE;
    private RemnantType remnantType = RemnantTypes.MORTAL;

    public RemnantComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void become(RemnantType type) {
        if (type == this.remnantType) {
            return;
        }

        RemnantState handler = type.create(this.player);
        this.state.setSoul(false);
        this.state = handler;
        this.remnantType = type;
        RemnantComponent.KEY.sync(this.player);
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
    public boolean isSoul() {
        return this.state.isSoul();
    }

    @Override
    public void setSoul(boolean incorporeal) {
        this.state.setSoul(incorporeal);
    }

    @Override
    public void tick() {
        this.state.serverTick();
    }

    @Override
    public void copyFrom(ServerPlayerEntity original, boolean lossless) {
        this.state.copyFrom(original, lossless);
    }

    @Override
    public void writeToPacket(PacketByteBuf buf, ServerPlayerEntity recipient, int syncOp) {
        buf.writeVarInt(RemnantTypes.getRawId(this.remnantType));
        buf.writeBoolean(this.isSoul());
    }

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        int remnantId = buf.readVarInt();
        boolean soul = buf.readBoolean();

        this.become(RemnantTypes.get(remnantId));
        this.setSoul(soul);
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        RemnantType remnantType = RemnantTypes.get(new Identifier(compoundTag.getString("id")));
        this.become(remnantType);
        this.state.fromTag(compoundTag);
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        compoundTag.putString("id", RemnantTypes.getId(this.remnantType).toString());
        this.state.toTag(compoundTag);
    }
}
