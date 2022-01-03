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
package ladysnake.requiem.core.remnant;

import ladysnake.requiem.api.v1.remnant.VagrantInteractionRegistry;
import ladysnake.requiem.core.RequiemCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class VagrantInteractionRegistryImpl implements VagrantInteractionRegistry {
    public static final VagrantInteractionRegistryImpl INSTANCE = new VagrantInteractionRegistryImpl();
    public static final Identifier POSSESSION_ICON = RequiemCore.id("textures/gui/possession_icon.png");

    private final List<VagrantInteraction> interactions = new ArrayList<>();

    @Override
    public <E extends LivingEntity> void registerPossessionInteraction(Class<E> targetType, BiPredicate<E, PlayerEntity> precondition, BiConsumer<E, PlayerEntity> action) {
        registerPossessionInteraction(targetType, precondition, action, POSSESSION_ICON);
    }

    @Override
    public <E extends LivingEntity> void registerPossessionInteraction(Class<E> targetType, BiPredicate<E, PlayerEntity> precondition, BiConsumer<E, PlayerEntity> action, Identifier icon) {
        this.interactions.add(new VagrantInteraction((e, p) -> targetType.isInstance(e) && precondition.test(targetType.cast(e), p), (e, p) -> action.accept(targetType.cast(e), p), icon));
    }

    public @Nullable VagrantInteraction getAction(LivingEntity tested, PlayerEntity player) {
        for (var interaction : interactions) {
            if (interaction.predicate().test(tested, player)) {
                return interaction;
            }
        }
        return null;
    }
}
