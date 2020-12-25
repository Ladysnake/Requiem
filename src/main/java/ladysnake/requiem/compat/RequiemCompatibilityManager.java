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
package ladysnake.requiem.compat;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import ladysnake.requiem.Requiem;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.loader.api.FabricLoader;

public final class RequiemCompatibilityManager {
    public static void init() {
        try {
            load("eldritch_mobs", EldritchMobsCompat::init);
            load("the_bumblezone", BumblezoneCompat::init);
            load("origins", OriginsCompat::init);
            load("golemsgalore", GolemsGaloreCompat::init);
            load("haema", HaemaCompat::init);
        } catch (Throwable t) {
            Requiem.LOGGER.error("[Requiem] Failed to load compatibility hooks", t);
        }
    }

    public static void load(String modId, ThrowingRunnable action) {
        try {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                action.run();
            }
        } catch (Throwable t) {
            Requiem.LOGGER.error("[Requiem] Failed to load compatibility hooks for {}", modId, t);
        }
    }

    public static void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("origins")) {
            registry.registerForPlayers(OriginsCompat.HOLDER_KEY, p -> new ComponentDataHolder<>(OriginsCompat.ORIGIN_KEY, OriginsCompat.HOLDER_KEY), RespawnCopyStrategy.ALWAYS_COPY);
        }
        if (FabricLoader.getInstance().isModLoaded("haema")) {
            registry.registerForPlayers(HaemaCompat.HOLDER_KEY, p -> new ComponentDataHolder<>(HaemaCompat.VAMPIRE_KEY, HaemaCompat.HOLDER_KEY), RespawnCopyStrategy.ALWAYS_COPY);
        }
    }
}
