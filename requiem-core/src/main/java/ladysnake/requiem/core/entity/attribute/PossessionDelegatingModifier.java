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
package ladysnake.requiem.core.entity.attribute;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.registry.Registry;

import java.util.OptionalDouble;
import java.util.function.Supplier;

public class PossessionDelegatingModifier implements NonDeterministicModifier {
    private final EntityAttribute attribute;
    private final Supplier<LivingEntity> handler;

    public PossessionDelegatingModifier(EntityAttribute attribute, Supplier<LivingEntity> handler) {
        this.attribute = attribute;
        this.handler = handler;
    }

    public static void replaceAttributes(PlayerEntity e) {
        replaceAttributes(e, PossessionComponent.get(e)::getHost);
    }

    public static void replaceAttributes(LivingEntity e, Supplier<LivingEntity> entitySupplier) {
        // Replace every registered attribute
        for (EntityAttribute attribute : Registry.ATTRIBUTE) {
            // Note: this fills the attribute map for the player, whether this is an issue is to be determined
            EntityAttributeInstance current = e.getAttributeInstance(attribute);
            if (current != null) {
                ((NonDeterministicAttribute) current).addFinalModifier(new PossessionDelegatingModifier(current.getAttribute(), entitySupplier));
            }
        }
    }

    /**
     * @return the attribute instance to which calls should be delegated
     */
    @Override
    public OptionalDouble apply(double value) {
        LivingEntity possessed = handler.get();

        if (possessed != null) {
            EntityAttributeInstance ret = possessed.getAttributeInstance(this.attribute);
            // the attribute can be null if it is not registered in the possessed entity
            if (ret != null) {
                return OptionalDouble.of(ret.getValue());
            }
        }
        return OptionalDouble.empty();
    }
}
