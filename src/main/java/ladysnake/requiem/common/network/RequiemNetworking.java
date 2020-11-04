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
package ladysnake.requiem.common.network;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.stream.Collectors;

import static io.netty.buffer.Unpooled.buffer;

public class RequiemNetworking {
    public static final Identifier POSSESSION_ACK = Requiem.id("possession_ack");
    public static final Identifier OPUS_USE = Requiem.id("opus_use");
    public static final Identifier DATA_SYNC = Requiem.id("data_sync");

    // Client -> Server
    public static final Identifier USE_DIRECT_ABILITY = Requiem.id("direct_ability");
    public static final Identifier USE_INDIRECT_ABILITY = Requiem.id("indirect_ability");
    public static final Identifier POSSESSION_REQUEST = Requiem.id("possession_request");
    public static final Identifier ETHEREAL_FRACTURE = Requiem.id("ethereal_fracture");
    public static final Identifier OPUS_UPDATE = Requiem.id("opus_update");
    public static final Identifier DIALOGUE_ACTION = Requiem.id("dialogue_action");
    public static final Identifier HUGGING_WALL = Requiem.id("hugging_wall");

    public static void sendToServer(Identifier identifier, PacketByteBuf data) {
        sendToServer(new CustomPayloadC2SPacket(identifier, data));
    }

    public static void sendToServer(CustomPayloadC2SPacket message) {
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.networkHandler.sendPacket(message);
    }

    public static void sendTo(ServerPlayerEntity player, Packet<?> message) {
        sendToPlayer(player, message);
    }

    public static void sendToAllTrackingIncluding(Entity tracked, Packet<?> message) {
        if (tracked.world instanceof ServerWorld) {
            PlayerStream.watching(tracked).forEach(p -> sendToPlayer((ServerPlayerEntity) p, message));
            if (tracked instanceof ServerPlayerEntity) {
                sendToPlayer((ServerPlayerEntity) tracked, message);
            }
        }
    }

    private static void sendToPlayer(ServerPlayerEntity player, Packet<?> message) {
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(message);
        }
    }

    public static CustomPayloadS2CPacket createOpusUsePacket(boolean cure, boolean showBook) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeBoolean(cure);
        buf.writeBoolean(showBook);
        return new CustomPayloadS2CPacket(OPUS_USE, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createEmptyMessage(Identifier id) {
        return new CustomPayloadS2CPacket(id, createEmptyBuffer());
    }

    public static CustomPayloadS2CPacket createDataSyncMessage(SubDataManagerHelper helper) {
        PacketByteBuf buf = createEmptyBuffer();
        List<SubDataManager<?>> managers = helper.streamDataManagers().collect(Collectors.toList());
        buf.writeVarInt(managers.size());
        for (SubDataManager<?> manager : managers) {
            Requiem.LOGGER.info("[Requiem] Synchronizing data for {} ({})", manager.getFabricId(), manager);
            buf.writeIdentifier(manager.getFabricId());
            manager.toPacket(buf);
        }
        return new CustomPayloadS2CPacket(DATA_SYNC, buf);
    }

    @Contract(pure = true)
    public static PacketByteBuf createEmptyBuffer() {
        return new PacketByteBuf(buffer());
    }

    @Contract(pure = true)
    public static PacketByteBuf createPossessionRequestBuffer(Entity entity) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeInt(entity.getEntityId());
        return buf;
    }

    public static CustomPayloadC2SPacket createOpusUpdateBuffer(String content, boolean sign, RemnantType resultingBook, Hand hand) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeString(content);
        buf.writeBoolean(sign);
        if (sign) {
            buf.writeString(RemnantTypes.getId(resultingBook).toString());
        }
        buf.writeEnumConstant(hand);
        return new CustomPayloadC2SPacket(OPUS_UPDATE, buf);
    }

    public static CustomPayloadC2SPacket createDialogueActionMessage(Identifier action) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeIdentifier(action);
        return new CustomPayloadC2SPacket(DIALOGUE_ACTION, buf);
    }

    public static void sendAbilityUseMessage(AbilityType type, Entity entity) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeEnumConstant(type);
        buf.writeVarInt(entity.getEntityId());
        sendToServer(USE_DIRECT_ABILITY, buf);
    }

    public static void sendAbilityUseMessage(AbilityType type) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeEnumConstant(type);
        sendToServer(USE_INDIRECT_ABILITY, buf);
    }

    public static void sendHugWallMessage(boolean hugging) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeBoolean(hugging);
        sendToServer(new CustomPayloadC2SPacket(HUGGING_WALL, buf));
    }

}
