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
package ladysnake.requiem.core;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import static io.netty.buffer.Unpooled.buffer;

public final class RequiemCoreNetworking {
    // Client -> Server
    public static final Identifier USE_DIRECT_ABILITY = RequiemCore.id("direct_ability");
    public static final Identifier HUGGING_WALL = RequiemCore.id("hugging_wall");
    public static final Identifier CONSUME_RESURRECTION_ITEM = RequiemCore.id("consume_resurrection_item");

    public static void sendAbilityUseMessage(AbilityType type, Entity entity) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeEnumConstant(type);
        buf.writeVarInt(entity.getId());
        ClientPlayNetworking.send(USE_DIRECT_ABILITY, buf);
    }

    public static void sendHugWallMessage(boolean hugging) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeBoolean(hugging);
        ClientPlayNetworking.send(HUGGING_WALL, buf);
    }

    public static void sendItemConsumptionPacket(Entity user, ItemStack stack) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(user.getId());
        buf.writeItemStack(stack);
        sendToAllTrackingIncluding(user, new CustomPayloadS2CPacket(CONSUME_RESURRECTION_ITEM, buf));
    }

    public static void sendToAllTrackingIncluding(Entity tracked, Packet<?> message) {
        if (tracked.world instanceof ServerWorld) {
            for (ServerPlayerEntity player : PlayerLookup.tracking(tracked)) {
                player.networkHandler.sendPacket(message);
            }
            if (tracked instanceof ServerPlayerEntity player) {
                player.networkHandler.sendPacket(message);
            }
        }
    }
}
