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
package ladysnake.requiem.api.v1.event.minecraft;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public interface DynamicRegistryRegistrationCallback {
    /**
     * Called when {@link net.minecraft.util.registry.DynamicRegistryManager} gets classloaded.
     */
    void registerDynamicRegistries(Helper helper);

    Event<DynamicRegistryRegistrationCallback> EVENT = EventFactory.createArrayBacked(
        DynamicRegistryRegistrationCallback.class,
        callbacks -> registration -> {
            for (DynamicRegistryRegistrationCallback callback : callbacks) {
                callback.registerDynamicRegistries(registration);
            }
        }
    );

    interface Helper {
        /**
         * Registers a severside dynamic registry.
         *
         * <p>If the corresponding builtin registry has not been registered when this method
         * is called, a default empty one will be automatically registered.
         */
        <E> void register(RegistryKey<? extends Registry<E>> ref, Codec<E> entryCodec);
        /**
         * Registers a dynamic registry which content gets synced between the server and connected clients.
         *
         * <p>If the corresponding builtin registry has not been registered when this method
         * is called, a default empty one will be automatically registered.
         */
        <E> void registerSynced(RegistryKey<? extends Registry<E>> ref, Codec<E> entryCodec, Codec<E> syncCodec);
    }
}
