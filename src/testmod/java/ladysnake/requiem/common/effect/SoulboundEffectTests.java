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
package ladysnake.requiem.common.effect;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.common.entity.effect.RequiemStatusEffects;
import ladysnake.requiemtest.RequiemTestUtil;
import ladysnake.requiemtest.mixin.EffectCommandAccessor;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;

import java.util.List;

public class SoulboundEffectTests implements QuiltGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void attritionSticksToSpirits(TestContext ctx) {
        ServerPlayerEntity ghost = RequiemTestUtil.spawnGhost(ctx);
        ghost.addStatusEffect(new StatusEffectInstance(RequiemStatusEffects.ATTRITION, 30, 0));
        ghost.clearStatusEffects();
        ctx.waitAndRun(1, () -> ctx.succeedIf(() -> GameTestUtil.assertTrue("Player should keep attrition", ghost.hasStatusEffect(RequiemStatusEffects.ATTRITION))));
    }

    @GameTest(structureName = EMPTY_STRUCTURE)
    public void clearCommandBypassesSticky(TestContext ctx) {
        ServerPlayerEntity ghost = RequiemTestUtil.spawnGhost(ctx);
        ghost.addStatusEffect(new StatusEffectInstance(RequiemStatusEffects.ATTRITION, 30, 0));
        EffectCommandAccessor.invokeExecuteClear(ghost.getCommandSource(), List.of(ghost));
        ctx.waitAndRun(1, () -> ctx.succeedIf(() -> GameTestUtil.assertFalse("Player should lose attrition", ghost.hasStatusEffect(RequiemStatusEffects.ATTRITION))));
    }
}
