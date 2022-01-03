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

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class JumpingMountEvents {
    public static final Event<MountCheckCallback> MOUNT_CHECK = EventFactory.createArrayBacked(MountCheckCallback.class, callbacks -> player -> {
        for (MountCheckCallback callback : callbacks) {
            JumpingMount mount = callback.getJumpingMount(player);
            if (mount != null) {
                return mount;
            }
        }
        return null;
    });

    @FunctionalInterface
    public interface MountCheckCallback {
        @Nullable JumpingMount getJumpingMount(LivingEntity entity);
    }

}
