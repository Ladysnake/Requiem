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
package ladysnake.pandemonium.client;

import com.mojang.authlib.GameProfile;
import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.pandemonium.common.entity.fakeplayer.FakeClientPlayerEntity;
import ladysnake.pandemonium.common.entity.fakeplayer.RequiemFakePlayer;
import ladysnake.requiem.client.RequiemClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static ladysnake.pandemonium.common.network.PandemoniumNetworking.*;

public class ClientMessageHandling {
    private static final float[] ETHEREAL_DAMAGE_COLOR = {0.5f, 0.0f, 0.0f};

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(PLAYER_SHELL_SPAWN, (client, handler, buf, responseSender) -> {
            int id = buf.readVarInt();
            UUID uuid = buf.readUuid();
            String name = buf.readString();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            float yaw = (float)(buf.readByte() * 360) / 256.0F;
            float pitch = (float)(buf.readByte() * 360) / 256.0F;
            GameProfile profile = readShellProfile(buf);
            client.execute(() -> {
                ClientWorld world = MinecraftClient.getInstance().world;
                assert world != null;
                FakeClientPlayerEntity other = new FakeClientPlayerEntity(PandemoniumEntities.PLAYER_SHELL, world, new GameProfile(uuid, name));
                other.setEntityId(id);
                other.resetPosition(x, y, z);
                other.updateTrackedPosition(x, y, z);
                other.updatePositionAndAngles(x, y, z, yaw, pitch);
                other.setOwnerProfile(profile);
                world.addEntity(id, other);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(PLAYER_PROFILE_SET, (client, handler, buf, responseSender) -> {
            int entityId = buf.readVarInt();
            GameProfile profile = readShellProfile(buf);
            client.execute(() -> {
                    Entity entity = Objects.requireNonNull(client.world).getEntityById(entityId);
                    if (entity instanceof RequiemFakePlayer) {
                        ((RequiemFakePlayer) entity).setOwnerProfile(profile);
                    }
                }
            );
        });
        ClientPlayNetworking.registerGlobalReceiver(ANCHOR_DAMAGE, (client, handler, buf, responseSender) -> {
            boolean dead = buf.readBoolean();
            client.execute(() -> RequiemClient.INSTANCE.getRequiemFxRenderer().playEtherealPulseAnimation(
                dead ? 4 : 1, ETHEREAL_DAMAGE_COLOR[0], ETHEREAL_DAMAGE_COLOR[1], ETHEREAL_DAMAGE_COLOR[2]
            ));
        });
    }

    @Nullable
    private static GameProfile readShellProfile(net.minecraft.network.PacketByteBuf buf) {
        boolean hasProfile = buf.readBoolean();
        GameProfile profile = hasProfile ? new GameProfile(buf.readUuid(), buf.readString()) : null;
        return profile;
    }
}
