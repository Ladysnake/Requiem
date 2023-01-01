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
package ladysnake.requiem.common.possession;

import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiemtest.RequiemTestUtil;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public class PossessionStatusEffectsTests implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void statusEffectsGetTransferred(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(0, 0, 0);
        ZombieEntity zombie = ctx.spawnMob(EntityType.ZOMBIE, 1, 0, 1);
        RequiemTestUtil.startPossession(player, zombie);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION));
        GameTestUtil.assertTrue("Possessed mob should inherit player's effects", zombie.hasStatusEffect(StatusEffects.ABSORPTION));
        player.removeStatusEffect(StatusEffects.ABSORPTION);
        GameTestUtil.assertFalse("Possessed mob should lose effect when player does", zombie.hasStatusEffect(StatusEffects.ABSORPTION));
        zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.BAD_OMEN));
        GameTestUtil.assertTrue("Player should inherit host's effects", player.hasStatusEffect(StatusEffects.BAD_OMEN));
        zombie.removeStatusEffect(StatusEffects.BAD_OMEN);
        GameTestUtil.assertFalse("Player should lose effect when host does", player.hasStatusEffect(StatusEffects.BAD_OMEN));
        ctx.complete();
    }
}
