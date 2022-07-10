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
package ladysnake.requiem.common.block.obelisk;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.block.RequiemBlocks;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiem.common.remnant.RemnantTypes;
import ladysnake.requiem.common.screen.RequiemScreenHandlers;
import ladysnake.requiem.common.screen.RiftScreenHandler;
import ladysnake.requiem.common.util.ObeliskDescriptor;
import ladysnake.requiemtest.mixin.WorldAccessor;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ObeliskTests implements FabricGameTest {
    public static final String SMALL_OBELISK = "requiem-test:small_obelisk";
    public static final String POWERED_SMALL_OBELISK = "requiem-test:powered_small_obelisk";

    @GameTest(structureName = SMALL_OBELISK)
    public void obelisksGetDetected(TestContext ctx) {
        BlockPos controllerPos = new BlockPos(4, 3, 4);
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
            RunestoneBlockEntity controller = Objects.requireNonNull(((RunestoneBlockEntity) ctx.getBlockEntity(controllerPos)));
            controller.update(ctx.getWorld());
            GameTestUtil.assertFalse("Obelisks without soul sand should not be powered", controller.isPowered());
            ctx.complete();
        });
    }

    @GameTest(structureName = POWERED_SMALL_OBELISK)
    public void obelisksGetPower(TestContext ctx) {
        BlockPos controllerPos = new BlockPos(20, 3, 20);
        InertRunestoneBlock.tryActivateObelisk(ctx.getWorld(), ctx.getAbsolutePos(controllerPos), true);
        ServerPlayerEntity player1 = ctx.spawnServerPlayer(1, 1, 1);
        ServerPlayerEntity player2 = ctx.spawnServerPlayer(1, 1, 2);
        RemnantComponent.get(player2).become(RemnantTypes.REMNANT);
        ctx.waitAndRun(2, () -> {
            RunestoneBlockEntity controller = Objects.requireNonNull(((RunestoneBlockEntity) ctx.getBlockEntity(controllerPos)));
            controller.update(ctx.getWorld());
            GameTestUtil.assertTrue("Obelisk should have 1 emancipation rune and 1 reclamation rune", controller.levels.equals(Map.of(
                RequiemBlocks.RUNIC_TACHYLITE_EMANCIPATION, 1,
                RequiemBlocks.RUNIC_TACHYLITE_RECLAMATION, 1
            )));
            GameTestUtil.assertTrue("Obelisks with enough soul sand should be powered", controller.isPowered());
            GameTestUtil.assertTrue("Nearby remnant player should have emancipation applied", player2.hasStatusEffect(RequiemStatusEffects.EMANCIPATION));
            GameTestUtil.assertTrue("Nearby remnant player should have reclamation applied", player2.hasStatusEffect(RequiemStatusEffects.RECLAMATION));
            GameTestUtil.assertFalse("Nearby mortal player should not have effects applied", player1.hasStatusEffect(RequiemStatusEffects.EMANCIPATION) || player1.hasStatusEffect(RequiemStatusEffects.RECLAMATION));
            ctx.complete();
        });
    }

    @GameTest(structureName = POWERED_SMALL_OBELISK)
    public void interactingWithRiftsOpensMenu(TestContext ctx) {
        BlockPos controllerPos = new BlockPos(20, 3, 20);
        BlockPos absoluteControllerPos = ctx.getAbsolutePos(controllerPos);
        ctx.setBlockState(controllerPos.up(), RequiemBlocks.RIFT_RUNE);
        ServerPlayerEntity player = ctx.spawnServerPlayer(18, 1, 20);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        ctx.waitAndRun(2, () -> {
            RunestoneBlockEntity controller = Objects.requireNonNull(((RunestoneBlockEntity) ctx.getBlockEntity(controllerPos)));
            controller.update(ctx.getWorld());
            GameTestUtil.assertTrue("Obelisk should have 1 rift rune", controller.levels.getInt(RequiemBlocks.RIFT_RUNE) == 1);
            GameTestUtil.assertTrue("Obelisks with enough soul sand should be powered", controller.isPowered());
            BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(absoluteControllerPos.up()), Direction.EAST, absoluteControllerPos.up(), false);
            ctx.getBlockState(controllerPos.up()).onUse(ctx.getWorld(), player, Hand.MAIN_HAND, hitResult);
            GameTestUtil.assertTrue("Corporeal player should not open the rift GUI", player.currentScreenHandler == player.playerScreenHandler);
            RemnantComponent.get(player).setVagrant(true);
            ctx.getBlockState(controllerPos.up()).onUse(ctx.getWorld(), player, Hand.MAIN_HAND, hitResult);
            GameTestUtil.assertTrue("Incorporeal player should open the rift GUI", player.currentScreenHandler.getType() == RequiemScreenHandlers.RIFT_SCREEN_HANDLER);
            GameTestUtil.assertTrue("Player should get opened rift in GUI", ((RiftScreenHandler) player.currentScreenHandler).getObelisks().equals(Set.of(new ObeliskDescriptor(
                ctx.getWorld().getRegistryKey(), absoluteControllerPos, 1, 2, Optional.empty()
            ))));
            ctx.complete();
        });
    }
}
