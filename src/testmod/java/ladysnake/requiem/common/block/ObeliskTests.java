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
package ladysnake.requiem.common.block;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.common.block.obelisk.InertRunestoneBlock;
import ladysnake.requiem.common.block.obelisk.RunestoneBlockEntity;
import ladysnake.requiemtest.mixin.WorldAccessor;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class ObeliskTests implements FabricGameTest {
    public static final String SMALL_OBELISK_STRUCTURE = "requiem-test:small_obelisk";

    @GameTest(structureName = SMALL_OBELISK_STRUCTURE)
    public void obelisksGetDetected(TestContext ctx) {
        BlockPos controllerPos = new BlockPos(2, 3, 2);
        BlockPos absoluteControllerPos = ctx.getAbsolutePos(controllerPos);
        InertRunestoneBlock.tryActivateObelisk(ctx.getWorld(), absoluteControllerPos, true);
        ctx.waitAndRun(2, () -> {
            BlockState controllerState = ctx.getBlockState(controllerPos);
            GameTestUtil.assertTrue("Bottommost runestone block should be activated", controllerState.get(InertRunestoneBlock.ACTIVATED));
            GameTestUtil.assertTrue("Topmost runestone block should be activated", ctx.getBlockState(controllerPos.up()).get(InertRunestoneBlock.ACTIVATED));
            GameTestUtil.assertTrue("Bottommost runestone block should be marked as controller", controllerState.get(InertRunestoneBlock.HEAD));
            GameTestUtil.assertTrue("Topmost runestone block should have a block entity", ctx.getBlockEntity(controllerPos.up()) instanceof RunestoneBlockEntity);
            GameTestUtil.assertTrue("Bottommost runestone block should have a block entity", ctx.getBlockEntity(controllerPos) instanceof RunestoneBlockEntity);
            GameTestUtil.assertTrue("Controller block entity should be ticking", ((WorldAccessor) ctx.getWorld()).getBlockEntityTickers().stream().anyMatch(i -> absoluteControllerPos.equals(i.getPos())));
            GameTestUtil.assertFalse("Non-controller block entity should not be ticking", ((WorldAccessor) ctx.getWorld()).getBlockEntityTickers().stream().anyMatch(i -> absoluteControllerPos.up().equals(i.getPos())));
            ctx.complete();
        });
    }
}
