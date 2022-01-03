/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.util;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SubDataManager<T> extends SimpleResourceReloadListener<T> {
    @Override
    default CompletableFuture<Void> apply(T data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> apply(data), executor);
    }

    void toPacket(PacketByteBuf buf);

    /**
     * Asynchronously process and load data from a packet. The code
     * must be thread-safe and not modify game state!
     */
    T loadFromPacket(PacketByteBuf buf);

    /**
     * Synchronously apply loaded data to the game state.
     */
    void apply(T data);
}
