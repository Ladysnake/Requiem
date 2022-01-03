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
package ladysnake.requiem.client.network;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.api.v1.util.SubDataManagerHelper;
import ladysnake.requiem.client.RequiemClient;
import ladysnake.requiem.client.RequiemFx;
import ladysnake.requiem.common.particle.RequiemParticleTypes;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.core.RequiemCoreNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ladysnake.requiem.common.network.RequiemNetworking.*;

public class ClientMessageHandler {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final RequiemClient rc;

    public ClientMessageHandler(RequiemClient requiemClient) {
        this.rc = requiemClient;
    }

    public void init() {
        ClientPlayNetworking.registerGlobalReceiver(ANCHOR_DAMAGE, (client, handler, buf, responseSender) -> {
            boolean dead = buf.readBoolean();
            client.execute(() -> RequiemClient.instance().fxRenderer().playEtherealPulseAnimation(
                dead ? 4 : 1, RequiemFx.ETHEREAL_DAMAGE_COLOR[0], RequiemFx.ETHEREAL_DAMAGE_COLOR[1], RequiemFx.ETHEREAL_DAMAGE_COLOR[2]
            ));
        });
        ClientPlayNetworking.registerGlobalReceiver(BODY_CURE, (client, handler, buf, responseSender) -> {
            int entityId = buf.readVarInt();
            client.execute(() -> {
                Entity entity = handler.getWorld().getEntityById(entityId);
                if (entity != null) {
                    for(int i = 0; i < 40; ++i) {
                        double vx = entity.world.random.nextGaussian() * 0.05D;
                        double vy = entity.world.random.nextGaussian() * 0.05D;
                        double vz = entity.world.random.nextGaussian() * 0.05D;
                        entity.world.addParticle(RequiemParticleTypes.CURE, entity.getParticleX(0.5D), entity.getRandomBodyY(), entity.getParticleZ(0.5D), vx, vy, vz);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(RequiemCoreNetworking.CONSUME_RESURRECTION_ITEM, (client, handler, buf, responseSender) -> {
            int entityId = buf.readVarInt();
            ItemStack stack = buf.readItemStack();
            client.execute(() -> {
                World world = handler.getWorld();
                Entity entity = world.getEntityById(entityId);
                if (entity != null) {
                    world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);
                    if (entity == this.mc.player || ((Possessable)entity).getPossessor() == this.mc.player) {
                        this.mc.gameRenderer.showFloatingItem(stack);
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(DATA_SYNC, (client, handler, buf, responseSender) -> {
            // We intentionally do not use the context's task queue directly
            // First, we make each sub data manager process its data, then we apply it synchronously with the task queue
            Map<Identifier, SubDataManager<?>> map = SubDataManagerHelper.getClientHelper().streamDataManagers().collect(Collectors.toMap(IdentifiableResourceReloadListener::getFabricId, Function.identity()));
            int nbManagers = buf.readVarInt();
            for (int i = 0; i < nbManagers; i++) {
                Identifier id = buf.readIdentifier();
                SubDataManager<?> manager = Objects.requireNonNull(map.get(id), "Unknown sub data manager " + id);
                Requiem.LOGGER.info("[Requiem] Received data for {}", manager.getFabricId());
                syncSubDataManager(buf, manager, client);
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(ETHEREAL_ANIMATION, (client, handler, buf, responseSender) -> client.execute(() -> {
            MinecraftClient mc = this.mc;
            assert mc.player != null;
            mc.player.world.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), RequiemSoundEvents.EFFECT_DISSOCIATE, SoundCategory.PLAYERS, 2, 0.6f);
            this.rc.fxRenderer().beginEtherealAnimation();
        }));
        ClientPlayNetworking.registerGlobalReceiver(OPUS_USE, (client, handler, buf, responseSender) -> {
            int remnantId = buf.readVarInt();
            boolean showBook = buf.readBoolean();
            RemnantType remnantType = RemnantTypes.get(remnantId);
            boolean cure = !remnantType.isDemon();

            client.execute(() -> {
                PlayerEntity player = Objects.requireNonNull(client.player);
                if (showBook) {
                    mc.particleManager.addEmitter(player, ParticleTypes.PORTAL, 120);
                    mc.gameRenderer.showFloatingItem(remnantType.getConversionBook(player));
                }
                if (cure) {
                    this.rc.fxRenderer().playEtherealPulseAnimation(16, 0.0f, 0.8f, 0.6f);
                } else {
                    this.rc.fxRenderer().playEtherealPulseAnimation(16, 1.0f, 0.25f, 0.27f);
                }
            });
        });
    }

    private static <T> void syncSubDataManager(PacketByteBuf buffer, SubDataManager<T> subManager, ThreadExecutor<?> taskQueue) {
        T data = subManager.loadFromPacket(buffer);
        taskQueue.execute(() -> subManager.apply(data));
    }
 }
