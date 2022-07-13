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

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.event.requiem.SoulCaptureEvents;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.core.entity.SoulHolderComponent;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;

public class EmptySoulVesselItemTests implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void computeSoulDefense(TestContext ctx) {
        LivingEntity mob = new PiglinBruteEntity(EntityType.PIGLIN_BRUTE, ctx.getWorld());
        SoulCaptureEvents.CaptureType captureType = SoulCaptureEvents.CaptureType.NORMAL;
        // "integration testing"
        Requiem.LOGGER.info(EmptySoulVesselItem.computeSoulDefense(mob, captureType));
        mob.setHealth(1.0F);
        Requiem.LOGGER.info(EmptySoulVesselItem.computeSoulDefense(mob, captureType));
        mob = new PillagerEntity(EntityType.PILLAGER, ctx.getWorld());
        Requiem.LOGGER.info(EmptySoulVesselItem.computeSoulDefense(mob, captureType));
        mob.setHealth(3.0F);
        Requiem.LOGGER.info(EmptySoulVesselItem.computeSoulDefense(mob, captureType));
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void soulVesselStealsWitherSouls(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 0, 2);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        ItemStack soulVessel = new ItemStack(RequiemItems.EMPTY_SOUL_VESSEL);
        player.setStackInHand(Hand.MAIN_HAND, soulVessel);
        WitherEntity wither = ctx.spawnMob(EntityType.WITHER, 3, 0, 3);
        GameTestUtil.assertTrue("Wither should start with full health", wither.getHealth() == wither.getMaxHealth());
        soulVessel.useOnEntity(player, wither, Hand.MAIN_HAND);
        ItemStack result = soulVessel.finishUsing(ctx.getWorld(), player);
        GameTestUtil.assertTrue("Wither should get damaged by soul stealing", wither.getHealth() < wither.getMaxHealth());
        GameTestUtil.assertTrue("Vessel should get filled with random soul", result.getItem() == RequiemItems.FILLED_SOUL_VESSEL);
        GameTestUtil.assertFalse("Wither should not lose its soul", SoulHolderComponent.isSoulless(wither));
        ctx.complete();
    }
}
