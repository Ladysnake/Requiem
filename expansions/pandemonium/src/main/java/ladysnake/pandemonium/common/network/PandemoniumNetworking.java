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
package ladysnake.pandemonium.common.network;

import com.mojang.authlib.GameProfile;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.pandemonium.common.entity.fakeplayer.FakePlayerEntity;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static ladysnake.requiem.common.network.RequiemNetworking.createEmptyBuffer;

public final class PandemoniumNetworking {
    public static final Identifier PLAYER_SHELL_SPAWN = Requiem.id("player_shell_spawn");
    public static final Identifier PLAYER_PROFILE_SET = Requiem.id("player_shell_skin");
    public static final Identifier ANCHOR_DAMAGE = Requiem.id("anchor_damage");

    public static CustomPayloadS2CPacket createPlayerShellSpawnPacket(PlayerShellEntity player) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeVarInt(player.getEntityId());
        buf.writeUuid(player.getGameProfile().getId());
        buf.writeString(player.getGameProfile().getName());
        buf.writeDouble(player.getX());
        buf.writeDouble(player.getY());
        buf.writeDouble(player.getZ());
        buf.writeByte((byte)((int)(player.yaw * 256.0F / 360.0F)));
        buf.writeByte((byte)((int)(player.pitch * 256.0F / 360.0F)));
        writePlayerProfile(player, buf);
        return new CustomPayloadS2CPacket(PLAYER_SHELL_SPAWN, buf);
    }

    public static void sendPlayerShellSkinPacket(FakePlayerEntity player) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeVarInt(player.getEntityId());
        writePlayerProfile(player, buf);

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(PLAYER_PROFILE_SET, buf);

        for (ServerPlayerEntity e : PlayerLookup.tracking(player)) {
            e.networkHandler.sendPacket(packet);
        }
    }

    private static void writePlayerProfile(FakePlayerEntity shell, PacketByteBuf buf) {
        GameProfile ownerProfile = shell.getOwnerProfile();
        buf.writeBoolean(ownerProfile != null);

        if (ownerProfile != null) {
            buf.writeUuid(ownerProfile.getId());
            buf.writeString(ownerProfile.getName());
        }
    }

    public static void sendAnchorDamageMessage(ServerPlayerEntity player, boolean dead) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeBoolean(dead);
        RequiemNetworking.sendTo(player, new CustomPayloadS2CPacket(ANCHOR_DAMAGE, buf));
    }
}
