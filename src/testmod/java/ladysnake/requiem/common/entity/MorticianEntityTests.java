/*
 * Requiem
 * Copyright (C) 2017-2023 Ladysnake
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
package ladysnake.requiem.common.entity;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.api.v1.record.GlobalRecord;
import ladysnake.requiem.common.RequiemRecordTypes;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.block.obelisk.ObeliskTests;
import ladysnake.requiem.common.block.obelisk.RunestoneBlockEntity;
import ladysnake.requiem.core.record.EntityPositionClerk;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public class MorticianEntityTests implements FabricGameTest {
    @GameTest(structureName = ObeliskTests.POWERED_SMALL_OBELISK)
    public void oldEtherealMorticiansGetConverted(TestContext ctx) {
        BlockPos controllerPos = new BlockPos(20, 3, 20);
        ctx.setBlockState(controllerPos.up(), RequiemBlocks.RIFT_RUNE);
        ctx.succeedIf(() -> {
            RunestoneBlockEntity controller = Objects.requireNonNull(((RunestoneBlockEntity) ctx.getBlockEntity(controllerPos)));
            GlobalRecord obeliskRecord = controller.getDescriptorRecord().orElseThrow(() -> new GameTestException("Unavailable obelisk"));
            NbtCompound morticianNbt = Util.make(new NbtCompound(), nbt -> {
                MorticianEntity m = new MorticianEntity(RequiemEntities.MORTICIAN, ctx.getWorld());
                EntityPositionClerk.get(m).linkWith(obeliskRecord);
                m.setPosition(ctx.getAbsolute(new Vec3d(18, 1, 20)));
                m.saveNbt(nbt);
                nbt.putUuid("linked_obelisk", obeliskRecord.getUuid());
            });
            MorticianEntity mortician = new MorticianEntity(RequiemEntities.MORTICIAN, ctx.getWorld());
            mortician.readNbt(morticianNbt);
            ctx.getWorld().spawnEntity(mortician);
            mortician.tick();
            GameTestUtil.assertTrue(
                "Obelisk should not reference mortician directly",
                obeliskRecord.get(RequiemRecordTypes.ENTITY_REF).isEmpty()
            );
            GameTestUtil.assertTrue(
                "Obelisk should reference mortician indirectly",
                obeliskRecord.get(RequiemRecordTypes.PROJECTED_MORTICIAN)
                    .flatMap(ptr -> ptr.resolveEntity(ctx.getWorld().getServer(), RequiemRecordTypes.ENTITY_REF))
                    .filter(mortician::equals)
                    .isPresent()
            );
        });
    }
}
