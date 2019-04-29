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
package ladysnake.requiem.client.network;

import ladysnake.requiem.api.v1.player.RequiemPlayer;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.remnant.RemnantStates;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.UUID;
import java.util.function.BiConsumer;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

public class ClientMessageHandling {
    public static void init() {
        register(REMNANT_SYNC, (context, buf) -> {
            UUID playerUuid = buf.readUuid();
            int remnantId = buf.readVarInt();
            boolean incorporeal = buf.readBoolean();
            PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
            if (player != null) {
                ((RequiemPlayer)player).setRemnantState(RemnantStates.get(remnantId).create(player));
                ((RequiemPlayer) player).getRemnantState().setSoul(incorporeal);
            }
        });
        register(POSSESSION_ACK, (context, buf) -> RequiemFx.INSTANCE.onPossessionAck());
        register(POSSESSION_SYNC, ((context, buf) -> {
            UUID playerUuid = buf.readUuid();
            int possessedId = buf.readInt();
            PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
            MinecraftClient client = MinecraftClient.getInstance();
            if (player != null) {
                Entity entity = player.world.getEntityById(possessedId);
                if (entity instanceof MobEntity) {
                    ((RequiemPlayer)player).getPossessionComponent().startPossessing((MobEntity) entity);
                    if (client.options.perspective == 0) {
                        client.gameRenderer.onCameraEntitySet(entity);
                    }
                } else {
                    ((RequiemPlayer)player).getPossessionComponent().stopPossessing();
                    client.gameRenderer.onCameraEntitySet(player);
                }
            }
        }));
        register(ETHEREAL_ANIMATION, ((context, buf) -> RequiemFx.INSTANCE.beginEtherealAnimation()));
        register(OPUS_USE, ((context, buf) -> {
            boolean cure = buf.readBoolean();
            boolean showBook = buf.readBoolean();
            PlayerEntity player = context.getPlayer();
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.particleManager.addEmitter(player, ParticleTypes.PORTAL, 120);
            if (showBook) {
                mc.gameRenderer.showFloatingItem(new ItemStack(cure ? RequiemItems.OPUS_DEMONIUM_CURE : RequiemItems.OPUS_DEMONIUM_CURSE));
            }
            if (cure) {
                RequiemFx.INSTANCE.playEtherealPulseAnimation(16, 0.0f, 0.8f, 0.6f);
            } else {
                RequiemFx.INSTANCE.playEtherealPulseAnimation(16, 1.0f, 0.25f, 0.27f);
            }
        }));
    }

    private static void register(Identifier id, BiConsumer<PacketContext, PacketByteBuf> handler) {
        ClientSidePacketRegistry.INSTANCE.register(
                id,
                (context, packet) -> context.getTaskQueue().execute(() -> {
                    handler.accept(context, packet);
                    packet.release();
                })
        );
    }
}
