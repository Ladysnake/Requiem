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
package ladysnake.pandemonium.compat;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import ladysnake.pandemonium.api.event.PlayerShellEvents;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.compat.ComponentDataHolder;
import ladysnake.requiem.compat.HaemaCompat;
import ladysnake.requiem.compat.OriginsCompat;
import ladysnake.requiem.compat.RequiemCompatibilityManager;
import net.fabricmc.loader.api.FabricLoader;

public final class PandemoniumCompatibilityManager {
    public static void init() {
        try {
            // Haema must be loaded before Origins, because vampire data must be stored before the origin gets cleared
            RequiemCompatibilityManager.load("haema", PandemoniumHaemaCompat.class);
            RequiemCompatibilityManager.load("origins", PandemoniumOriginsCompat.class);
        } catch (Throwable t) {
            Requiem.LOGGER.error("[Pandemonium] Failed to load compatibility hooks", t);
        }
    }

    public static void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        if (FabricLoader.getInstance().isModLoaded("origins")) {
            registry.registerFor(PlayerShellEntity.class, OriginsCompat.HOLDER_KEY, shell -> new ComponentDataHolder<>(OriginsCompat.ORIGIN_KEY, OriginsCompat.HOLDER_KEY));
        }
        if (FabricLoader.getInstance().isModLoaded("haema")) {
            registry.registerFor(PlayerShellEntity.class, HaemaCompat.HOLDER_KEY, shell -> new ComponentDataHolder<>(HaemaCompat.VAMPIRE_KEY, HaemaCompat.HOLDER_KEY));
        }
    }

    public static <C extends ComponentV3> void registerShellDataCallbacks(ComponentKey<ComponentDataHolder<C>> holderKey) {
        PlayerShellEvents.DATA_TRANSFER.register((from, to, merge) -> {
            // First, store a backup of the player's actual origin
            if (merge) holderKey.get(to).storeData(to);

            if (RemnantComponent.isVagrant(from)) {    // can happen with /pandemonium shell create
                holderKey.get(from).restoreData(to);
            } else {
                ComponentDataHolder<C> holder = holderKey.get(merge ? from : to);
                holder.storeData(from);
                holder.restoreData(to);
            }
        });
    }
}
