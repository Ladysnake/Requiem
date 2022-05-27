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
package ladysnake.requiem.common.util.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import ladysnake.requiem.api.v1.event.minecraft.DynamicRegistryRegistrationCallback;
import ladysnake.requiem.common.RequiemRegistries;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

public final class DynamicRegistryRegistrationHelperImpl implements DynamicRegistryRegistrationCallback.Helper {
    private final RegistrationSink backing;
    private final SyncedRegistrationSink backingSynced;

    public DynamicRegistryRegistrationHelperImpl(RegistrationSink backing, SyncedRegistrationSink backingSynced) {
        this.backing = backing;
        this.backingSynced = backingSynced;
    }

    @Override
    public <E> void register(RegistryKey<? extends Registry<E>> ref, Codec<E> entryCodec) {
        this.registerBuiltinIfNeeded(ref);
        this.backing.register(ref, entryCodec);
    }

    @Override
    public <E> void registerSynced(RegistryKey<? extends Registry<E>> ref, Codec<E> entryCodec, Codec<E> networkEntryCodec) {
        this.registerBuiltinIfNeeded(ref);
        this.backingSynced.register(ref, entryCodec, networkEntryCodec);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <E> void registerBuiltinIfNeeded(RegistryKey<? extends Registry<E>> ref) {
        if (!BuiltinRegistries.REGISTRIES.containsId(ref.getValue())) {
            ((MutableRegistry) BuiltinRegistries.REGISTRIES).add(
                ref,
                new SimpleRegistry<>(RequiemRegistries.MOB_ITEM_OVERRIDE_KEY, Lifecycle.stable(), null),
                Lifecycle.stable()
            );
        }
    }

    @FunctionalInterface
    public interface RegistrationSink {
        void register(RegistryKey<? extends Registry<?>> registryRef, Codec<?> entryCodec);
    }

    @FunctionalInterface
    public interface SyncedRegistrationSink {
        void register(RegistryKey<? extends Registry<?>> registryRef, Codec<?> entryCodec, Codec<?> networkEntryCodec);
    }
}
