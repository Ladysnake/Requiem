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
package ladysnake.requiem.common;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.entity.MovementAlterer;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityRegistry;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.impl.ability.ImmutableMobAbilityController;
import ladysnake.requiem.common.impl.movement.PlayerMovementAlterer;
import ladysnake.requiem.common.impl.possession.PossessionComponentImpl;
import ladysnake.requiem.common.impl.remnant.RemnantComponentImpl;
import ladysnake.requiem.common.impl.remnant.RevivingDeathSuspender;
import ladysnake.requiem.common.impl.remnant.dialogue.PlayerDialogueTracker;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.entity.mob.MobEntity;

public final class RequiemComponents implements EntityComponentInitializer {

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // the order here is important, possession must be synced/deserialized after remnant
        registry.registerForPlayers(RemnantComponent.KEY, RemnantComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PossessionComponent.KEY, PossessionComponentImpl::new, RespawnCopyStrategy.INVENTORY);
        // order does not matter for the other components
        registry.registerForPlayers(MovementAlterer.KEY, PlayerMovementAlterer::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerForPlayers(DeathSuspender.KEY, RevivingDeathSuspender::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerForPlayers(DialogueTracker.KEY, PlayerDialogueTracker::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerFor(MobEntity.class, MobAbilityController.KEY,
            e -> new ImmutableMobAbilityController<>(MobAbilityRegistry.instance().getConfig(e), (MobEntity & Possessable) e));
    }
}
