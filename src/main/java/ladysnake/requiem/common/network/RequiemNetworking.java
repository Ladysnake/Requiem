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
package ladysnake.requiem.common.network;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.util.ObeliskDescriptor;
import ladysnake.requiem.core.RequiemCoreNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

import java.util.List;

import static io.netty.buffer.Unpooled.buffer;

public final class RequiemNetworking {
    // Server -> Client
    public static final Identifier ANCHOR_DAMAGE = Requiem.id("anchor_damage");
    public static final Identifier DATA_SYNC = Requiem.id("data_sync");
    public static final Identifier OPUS_USE = Requiem.id("opus_use");
    public static final Identifier ETHEREAL_ANIMATION = Requiem.id("ethereal_animation");
    public static final Identifier BODY_CURE = Requiem.id("body_cure");

    // Client -> Server
    public static final Identifier DIALOGUE_ACTION = Requiem.id("dialogue_action");
    public static final Identifier ETHEREAL_FRACTURE = Requiem.id("ethereal_fracture");
    public static final Identifier OPEN_CRAFTING_MENU = Requiem.id("open_crafting");
    public static final Identifier USE_INDIRECT_ABILITY = Requiem.id("indirect_ability");
    public static final Identifier USE_RIFT = Requiem.id("use_rift");

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

    private static void sendToPlayer(ServerPlayerEntity player, Packet<?> message) {
        if (player.networkHandler != null) {
            player.networkHandler.sendPacket(message);
        }
    }

    public static CustomPayloadS2CPacket createOpusUsePacket(RemnantType chosenType, boolean showBook) {
        PacketByteBuf buf = createEmptyBuffer();
        buf.writeVarInt(RemnantTypes.getRawId(chosenType));
        buf.writeBoolean(showBook);
        return new CustomPayloadS2CPacket(OPUS_USE, buf);
    }

    @Contract(pure = true)
    public static CustomPayloadS2CPacket createEmptyMessage(Identifier id) {
        return new CustomPayloadS2CPacket(id, createEmptyBuffer());
    }

    public static CustomPayloadS2CPacket createDataSyncMessage(SubDataManagerHelper helper) {
        PacketByteBuf buf = createEmptyBuffer();
        List<SubDataManager<?>> managers = helper.streamDataManagers().toList();
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

    public static void sendDialogueActionMessage(int choice) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeByte(choice);
        sendToServer(new CustomPayloadC2SPacket(DIALOGUE_ACTION, buf));
    }

    public static void sendRiftUseMessage(ObeliskDescriptor target) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.encode(ObeliskDescriptor.CODEC, target);
        sendToServer(USE_RIFT, buf);
    }

    public static void sendIndirectAbilityUseMessage(AbilityType type) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeEnumConstant(type);
        sendToServer(USE_INDIRECT_ABILITY, buf);
    }

    public static void sendSupercrafterMessage() {
        sendToServer(new CustomPayloadC2SPacket(OPEN_CRAFTING_MENU, createEmptyBuffer()));
    }

    public static void sendEtherealAnimationMessage(ServerPlayerEntity player) {
        sendTo(player, createEmptyMessage(ETHEREAL_ANIMATION));
    }

    public static void sendBodyCureMessage(LivingEntity entity) {
        PacketByteBuf buf = new PacketByteBuf(buffer());
        buf.writeVarInt(entity.getId());
        RequiemCoreNetworking.sendToAllTrackingIncluding(entity, new CustomPayloadS2CPacket(BODY_CURE, buf));
    }

    public static void sendAnchorDamageMessage(ServerPlayerEntity player, boolean dead) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(dead);
        ServerPlayNetworking.send(player, ANCHOR_DAMAGE, buf);
    }
}
