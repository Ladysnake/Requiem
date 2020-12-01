/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.api.v1.entity.ability;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.TransientComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import ladysnake.requiem.api.v1.internal.DummyMobAbilityController;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/**
 * A {@link MobAbilityController} is interacted with by a player to use special {@link MobAbility mob abilities}
 */
public interface MobAbilityController extends TransientComponent, CommonTickingComponent, AutoSyncedComponent {
    ComponentKey<MobAbilityController> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "ability_controller"), MobAbilityController.class);

    static MobAbilityController get(Entity entity) {
        MobAbilityController c = KEY.getNullable(entity);
        return c != null ? c : DummyMobAbilityController.INSTANCE;
    }

    double getRange(AbilityType type);

    boolean canTarget(AbilityType type, Entity target);

    boolean useDirect(AbilityType type, Entity target);

    boolean useIndirect(AbilityType type);

    @Override
    void tick();
}
