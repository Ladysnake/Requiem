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
package ladysnake.requiem.common;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.api.v1.util.SubDataManager;
import ladysnake.requiem.common.impl.movement.MovementAltererManager;
import ladysnake.requiem.common.impl.remnant.RevivingDeathSuspender;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.ObjectPath;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public final class RequiemComponents {
    public static final ComponentType<SubDataManager> DIALOGUES = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("dialogue_registry"), SubDataManager.class
    );
    public static final ComponentType<SubDataManager> MOVEMENT_ALTERERS = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("movement_alterer"), SubDataManager.class
    );
    public static final ObjectPath<World, DialogueRegistry> DIALOGUE_REGISTRY = DIALOGUES
            .asComponentPath()
            .compose(ComponentProvider::fromWorld)
            .thenCastTo(DialogueRegistry.class);
    public static final ObjectPath<World, MovementAltererManager> MOVEMENT_ALTERER_MANAGER = MOVEMENT_ALTERERS
            .asComponentPath()
            .compose(ComponentProvider::fromWorld)
            .thenCastTo(MovementAltererManager.class);
    public static final ComponentType<DeathSuspender> DEATH_SUSPENDER = ComponentRegistry.INSTANCE.registerIfAbsent(
            Requiem.id("death_suspension"), DeathSuspender.class
    );

    public static void initComponents() {
        EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> {
            components.put(DEATH_SUSPENDER, new RevivingDeathSuspender(player));
        });
    }
}
