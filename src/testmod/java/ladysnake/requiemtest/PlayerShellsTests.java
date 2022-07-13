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
package ladysnake.requiemtest;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.api.v1.remnant.PlayerSplitResult;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.gamerule.PossessionKeepInventory;
import ladysnake.requiem.common.gamerule.RequiemGamerules;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;

public class PlayerShellsTests implements FabricGameTest {
    public static final String POSSESSION_KEEPS_INVENTORY_BATCH = "possessionKeepsInventory";

    @BeforeBatch(batchId = POSSESSION_KEEPS_INVENTORY_BATCH)
    public void setPossessionKeepInventory(ServerWorld world) {
        world.getGameRules().get(RequiemGamerules.POSSESSION_KEEP_INVENTORY).set(PossessionKeepInventory.LIVING, world.getServer());
    }

    @AfterBatch(batchId = POSSESSION_KEEPS_INVENTORY_BATCH)
    public void resetPossessionKeepInventory(ServerWorld world) {
        world.getGameRules().get(RequiemGamerules.POSSESSION_KEEP_INVENTORY).set(PossessionKeepInventory.NEVER, world.getServer());
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void mergingWithShellsShouldMergeXp(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 0, 2);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        player.addExperience(200);
        PlayerSplitResult result = RemnantComponent.get(player).splitPlayer(true).orElseThrow();
        GameTestUtil.assertTrue("Shell should have no experience", result.shell().totalExperience == 0);
        GameTestUtil.assertTrue("Soul should keep all the experience", result.soul().totalExperience == 200);
        result.shell().addExperience(100);
        RemnantComponent.get(result.soul()).merge(result.shell());
        GameTestUtil.assertTrue("Soul and body experience should merge", result.soul().totalExperience == 300);
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void splittingTransfersItems(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 0, 2);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        ItemStack pickaxe = Items.DIAMOND_PICKAXE.getDefaultStack();
        ItemStack torch = Items.TORCH.getDefaultStack();
        ItemStack dirt = new ItemStack(Blocks.DIRT, 42);
        int slot = 10;
        player.setStackInHand(Hand.MAIN_HAND, pickaxe.copy());
        player.setStackInHand(Hand.OFF_HAND, torch.copy());
        player.getInventory().insertStack(slot, dirt.copy());
        PlayerSplitResult result = RemnantComponent.get(player).splitPlayer(true).orElseThrow();
        GameTestUtil.assertTrue("Player's main inventory should be empty", result.soul().getMainHandStack().isEmpty() && result.soul().getInventory().getStack(slot).isEmpty());
        GameTestUtil.assertTrue("Player's offhand should be empty", result.soul().getOffHandStack().isEmpty());
        GameTestUtil.assertTrue("Shell's main inventory should get player's items",
            ItemStack.areEqual(result.shell().getMainHandStack(), pickaxe) &&
                ItemStack.areEqual(result.shell().getInventory().getStack(slot), dirt));
        GameTestUtil.assertTrue("Shell's offhand should get player's item", ItemStack.areEqual(result.shell().getOffHandStack(), torch));
        RemnantComponent.get(result.soul()).merge(result.shell());
        GameTestUtil.assertTrue("Player's main inventory should get items back",
            ItemStack.areEqual(result.soul().getMainHandStack(), pickaxe) &&
                ItemStack.areEqual(result.soul().getInventory().getStack(slot), dirt));
        GameTestUtil.assertTrue("Player should get offhand item back", ItemStack.areEqual(result.soul().getOffHandStack(), torch));
        ctx.complete();
    }

    @GameTest(structureName = EMPTY_STRUCTURE, batchId = POSSESSION_KEEPS_INVENTORY_BATCH)
    public void splittingWithKeepInventoryKeepsItems(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(2, 0, 2);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        ItemStack pickaxe = Items.DIAMOND_PICKAXE.getDefaultStack();
        ItemStack torch = Items.TORCH.getDefaultStack();
        ItemStack dirt = new ItemStack(Blocks.DIRT, 42);
        int slot = 10;
        player.setStackInHand(Hand.MAIN_HAND, pickaxe.copy());
        player.setStackInHand(Hand.OFF_HAND, torch.copy());
        player.getInventory().insertStack(slot, dirt.copy());
        PlayerSplitResult result = RemnantComponent.get(player).splitPlayer(true).orElseThrow();
        GameTestUtil.assertFalse("Player's main inventory should keep items", result.soul().getMainHandStack().isEmpty() && result.soul().getInventory().getStack(slot).isEmpty());
        GameTestUtil.assertFalse("Player's offhand should keep item", result.soul().getOffHandStack().isEmpty());
        GameTestUtil.assertFalse("Shell's main inventory should be empty",
            ItemStack.areEqual(result.shell().getMainHandStack(), pickaxe) &&
                ItemStack.areEqual(result.shell().getInventory().getStack(slot), dirt));
        GameTestUtil.assertFalse("Shell's offhand should be empty", ItemStack.areEqual(result.shell().getOffHandStack(), torch));
        result.shell().getInventory().insertStack(slot, Items.POTION.getDefaultStack());
        RemnantComponent.get(result.soul()).merge(result.shell());
        GameTestUtil.assertTrue("Player should get mainhand item back",
            ItemStack.areEqual(result.soul().getMainHandStack(), pickaxe));
        GameTestUtil.assertTrue("Player should get offhand item back", ItemStack.areEqual(result.soul().getOffHandStack(), torch));
        GameTestUtil.assertTrue("Player should keep item from shell's main inventory", result.soul().getInventory().getStack(slot).isOf(Items.POTION));
        ctx.expectEntity(EntityType.ITEM);  // dirt should have been dropped
        ctx.complete();
    }
}
