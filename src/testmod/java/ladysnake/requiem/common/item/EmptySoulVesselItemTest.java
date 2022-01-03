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
package ladysnake.requiem.common.item;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.entity.RequiemEntityAttributes;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.mockito.Mockito;

public class EmptySoulVesselItemTest implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void computeSoulDefense(TestContext ctx) {
        LivingEntity mob = Mockito.mock(LivingEntity.class);
        setupMob(mob, PiglinBruteEntity.createPiglinBruteAttributes());
        // "integration testing"
        Requiem.LOGGER.info(EmptySoulVesselItem.computeSoulDefense(mob));
        Mockito.when(mob.getHealth()).thenReturn(1.0F);
        Requiem.LOGGER.info(EmptySoulVesselItem.computeSoulDefense(mob));
        setupMob(mob, PillagerEntity.createPillagerAttributes());
        Requiem.LOGGER.info(EmptySoulVesselItem.computeSoulDefense(mob));
        Mockito.when(mob.getHealth()).thenReturn(3.0F);
        Requiem.LOGGER.info(EmptySoulVesselItem.computeSoulDefense(mob));
        ctx.complete();
    }

    private void setupMob(LivingEntity mob, DefaultAttributeContainer.Builder attributeBuilder) {
        AttributeContainer attributes = new AttributeContainer(attributeBuilder.build());
        Mockito.when(mob.getAttributeValue(RequiemEntityAttributes.SOUL_DEFENSE)).thenReturn(0.0);
        Mockito.when(mob.getAttributeBaseValue(Mockito.any(EntityAttribute.class))).thenCallRealMethod();
        Mockito.when((double) mob.getMaxHealth()).thenCallRealMethod();
        Mockito.when(mob.getHealth()).thenReturn((float) attributes.getValue(EntityAttributes.GENERIC_MAX_HEALTH));
        Mockito.when(mob.getAttributes()).thenReturn(attributes);
    }
}
