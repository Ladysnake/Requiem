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
package ladysnake.requiem.api.v1;

import ladysnake.requiem.api.v1.internal.ApiInternals;
import org.apiguardian.api.API;

import java.util.stream.Stream;

public final class RequiemApi {
    private RequiemApi() { throw new AssertionError(); }

    @API(status = API.Status.EXPERIMENTAL)
    public static void registerPlugin(RequiemPlugin entryPoint) {
        ApiInternals.registerPluginInternal(entryPoint);
    }

    @API(status = API.Status.EXPERIMENTAL)
    public static Stream<RequiemPlugin> getRegisteredPlugins() {
        return ApiInternals.streamRegisteredPlugins();
    }

}
