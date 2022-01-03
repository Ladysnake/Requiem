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
package ladysnake.requiem.api.v1.event;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An event for which callbacks are uniquely identified.
 * Listeners can be individually removed using their identifier.
 * @param <T> The listener type.
 */
public class IdentifyingEvent<T> extends Event<T> {
    private final Class<T> type;
    private final Map<Identifier, T> handlers = new HashMap<>();
    private final Function<T[], T> invokerFactory;

    public IdentifyingEvent(Class<T> type, Function<T[], T> invokerFactory) {
        this.type = type;
        this.invokerFactory = invokerFactory;
    }

    private void update() {
        if (handlers.size() == 1) {
            this.invoker = handlers.values().iterator().next();
        } else {
            @SuppressWarnings("unchecked") T[] arr = handlers.values().toArray((T[]) Array.newInstance(type, 0));
            this.invoker = this.invokerFactory.apply(arr);
        }
    }

    /**
     * @deprecated use {@link #register(Identifier, Object)}
     */
    @Override
    @Deprecated
    public void register(T listener) {
        throw new UnsupportedOperationException("Identifying events require an identifier");
    }

    public void register(Identifier id, T listener) {
        handlers.put(id, listener);
        update();
    }

    /**
     * Unregisters a listener using its id
     * @param id the identifier of the listener to unregister
     */
    public void unregister(Identifier id) {
        handlers.remove(id);
        update();
    }
}
