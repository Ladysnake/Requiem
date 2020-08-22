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
package ladysnake.requiem.client.network;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

public class ClientMessageHandling {
    public static void init() {
        ClientSidePacketRegistry.INSTANCE.register(REMNANT_SYNC, (context, buf) -> {
            UUID playerUuid = buf.readUuid();
            int remnantId = buf.readVarInt();
            boolean incorporeal = buf.readBoolean();
            context.getTaskQueue().execute(() -> {
                PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
                if (player != null) {
                    ((RequiemPlayer)player).become(RemnantTypes.get(remnantId));
                    ((RequiemPlayer) player).asRemnant().setSoul(incorporeal);
                }
            });
        });
        ClientSidePacketRegistry.INSTANCE.register(POSSESSION_ACK, (context, buf) -> context.getTaskQueue().execute(RequiemFx.INSTANCE::onPossessionAck));
        ClientSidePacketRegistry.INSTANCE.register(POSSESSION_SYNC, (context, buf) -> {
            UUID playerUuid = buf.readUuid();
            int possessedId = buf.readInt();
            context.getTaskQueue().execute(() -> {
                PlayerEntity player = context.getPlayer().world.getPlayerByUuid(playerUuid);
                MinecraftClient client = MinecraftClient.getInstance();
                if (player != null) {
                    Entity entity = player.world.getEntityById(possessedId);
                    if (entity instanceof MobEntity) {
                        ((RequiemPlayer)player).asPossessor().startPossessing((MobEntity) entity);
                        if (client.options.getPerspective().isFirstPerson()) {
                            client.gameRenderer.onCameraEntitySet(entity);
                        }
                    } else {
                        ((RequiemPlayer)player).asPossessor().stopPossessing();
                        client.gameRenderer.onCameraEntitySet(player);
                    }
                }
            });
        });
        ClientSidePacketRegistry.INSTANCE.register(OPUS_USE, ((context, buf) -> {
            boolean cure = buf.readBoolean();
            boolean showBook = buf.readBoolean();
            context.getTaskQueue().execute(() -> {
                PlayerEntity player = context.getPlayer();
                MinecraftClient mc = MinecraftClient.getInstance();
                if (showBook) {
                    mc.particleManager.addEmitter(player, ParticleTypes.PORTAL, 120);
                    mc.gameRenderer.showFloatingItem(new ItemStack(cure ? RequiemItems.OPUS_DEMONIUM_CURE : RequiemItems.OPUS_DEMONIUM_CURSE));
                }
                if (cure) {
                    RequiemFx.INSTANCE.playEtherealPulseAnimation(16, 0.0f, 0.8f, 0.6f);
                } else {
                    RequiemFx.INSTANCE.playEtherealPulseAnimation(16, 1.0f, 0.25f, 0.27f);
                }
            });
        }));
        ClientSidePacketRegistry.INSTANCE.register(DATA_SYNC, (context, buffer) -> {
            // We intentionally do not use the context's task queue directly
            // First, we make each sub data manager process its data, then we apply it synchronously with the task queue
            Map<Identifier, SubDataManager<?>> map = SubDataManagerHelper.getClientHelper().streamDataManagers().collect(Collectors.toMap(IdentifiableResourceReloadListener::getFabricId, Function.identity()));
            int nbManagers = buffer.readVarInt();
            for (int i = 0; i < nbManagers; i++) {
                Identifier id = buffer.readIdentifier();
                SubDataManager<?> manager = Objects.requireNonNull(map.get(id), "Unknown sub data manager " + id);
                Requiem.LOGGER.info("[Requiem] Received data for {}", manager.getFabricId());
                syncSubDataManager(buffer, manager, context.getTaskQueue());
            }
        });
    }

    private static <T> void syncSubDataManager(PacketByteBuf buffer, SubDataManager<T> subManager, ThreadExecutor<?> taskQueue) {
        T data = subManager.loadFromPacket(buffer);
        taskQueue.execute(() -> subManager.apply(data));
    }

 }
