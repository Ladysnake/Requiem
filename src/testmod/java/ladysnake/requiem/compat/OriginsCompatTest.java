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
package ladysnake.requiem.compat;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

public class OriginsCompatTest implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void vagrantPlayersGetVagrantOrigin(TestContext ctx) {
        ServerPlayerEntity player = ctx.spawnServerPlayer(1, 0, 1);
        OriginComponent originComponent = ModComponents.ORIGIN.get(player);
        OriginLayer layer = OriginLayers.getLayer(new Identifier("origins:origin"));
        Origin humanOrigin = OriginRegistry.get(new Identifier("origins:human"));
        originComponent.setOrigin(layer, humanOrigin);
        RemnantComponent.get(player).become(RemnantTypes.REMNANT);
        Origin vagrantOrigin = OriginRegistry.get(OriginsCompat.VAGRANT_ORIGIN_ID);
        GameTestUtil.assertTrue("Vagrant origin should exist", vagrantOrigin != null);
        GameTestUtil.assertTrue("Vagrant origin should be special", vagrantOrigin.isSpecial());
        GameTestUtil.assertTrue("Corporeal player should have human origin", originComponent.getOrigin(layer) == humanOrigin);
        RemnantComponent.get(player).setVagrant(true);
        GameTestUtil.assertTrue("Vagrant player should have vagrant origin", originComponent.getOrigin(layer) == vagrantOrigin);
        RemnantComponent.get(player).setVagrant(false);
        GameTestUtil.assertTrue("Corporeal player should have human origin again", originComponent.getOrigin(layer) == humanOrigin);
        ctx.complete();
    }
}
