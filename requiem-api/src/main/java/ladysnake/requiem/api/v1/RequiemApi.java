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
package ladysnake.requiem.api.v1;

import com.google.common.collect.ImmutableSet;
import ladysnake.requiem.api.v1.internal.ApiInternals;
import org.apiguardian.api.API;

/**
 * This class offers static methods to manipulate (register and query) {@link RequiemPlugin} instances.
 */
public final class RequiemApi {
    private RequiemApi() { throw new AssertionError(); }

    /**
     * The key used to declare a requiem plugin in a <tt>fabric.mod.json</tt> "entrypoints" block.
     */
    public static final String ENTRYPOINT_KEY = "requiem:plugin";

    /**
     * Programmatically registers a new entry point.
     * <p>
     * Calling this method is functionally equivalent to declaring the entry point
     * in <tt>fabric.mod.json</tt>, with the benefit of letting consumers control the
     * time of registration.
     * <p>
     * Plugin instances can be registered at any time during the game's loading process.
     * Registering the same plugin instance twice is an error condition and will raise
     * an {@link IllegalStateException}.
     * <p>
     * The registration behaviour depends on the availability of an API provider:
     * <ul>
     * <li>If the API providing mod has not yet been initialized, the plugin will be put in
     * a queue, and {@code registerPlugin} will return immediately.
     * Such early plugins will be called back at a later time, when the provider initializes.</li>
     * <li>If the providing mod has already been initialized, the plugin will be initialized
     * immediately. {@code registerPlugin} will return once the initialization has been performed.</li>
     * <li>If no API providing mod are available in the current game instance, {@code registerPlugin}
     * will return immediately, and the plugin's methods will never get called.</li>
     * </ul>
     * In any case, the plugin instance will be added to the list of registered plugins and will
     * be immediately accessible through {@link #getRegisteredPlugins()}.
     * <p>
     * This method can be called off-thread, although doing so is discouraged as there are
     * no guarantee regarding which thread the plugin's methods will be called on.
     *
     * @param entryPoint a {@link RequiemPlugin} to register
     * @throws IllegalStateException if {@code entryPoint} has already been registered
     * @see RequiemPlugin
     * @see #getRegisteredPlugins()
     * @since 1.0.0
     */
    @API(status = API.Status.MAINTAINED)
    public static void registerPlugin(RequiemPlugin entryPoint) {
        ApiInternals.registerPluginInternal(entryPoint);
    }

    /**
     * Return an {@link ImmutableSet}, the elements of which are currently registered Requiem plugins.
     * <p>
     * The elements of the set are {@link RequiemPlugin} objects that have been previously registered,
     * either by being listed as an adequate entry point in a <tt>fabric.mod.json</tt>, or by being passed
     * as an argument to {@link #registerPlugin(RequiemPlugin) registerPlugin()}.
     * <p>
     * The values contained in the source set can evolve over the game initialization
     * as new plugins get registered. The returned {@code ImmutableSet} will provide a view over all plugins
     * that were registered at the time {@code getRegisteredPlugins()} was called.
     * <p>
     * Note that while plugins registered programmatically will always be visible after
     * calling {@link #registerPlugin(RequiemPlugin) registerPlugin()}, entry points declared from a
     * <tt>fabric.mod.json</tt> will only be listed after being processed by an API provider.
     * <p>
     * This method can be safely called from any thread.
     *
     * @return an immutable set containing the currently registered plugins
     * @see RequiemPlugin
     * @see #registerPlugin(RequiemPlugin)
     * @since 1.0.0
     */
    @API(status = API.Status.MAINTAINED)
    public static ImmutableSet<RequiemPlugin> getRegisteredPlugins() {
        return ApiInternals.copyRegisteredPlugins();
    }

}
