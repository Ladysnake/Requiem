/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.elmendorf.GameTestUtil;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.common.item.RequiemItems;
import ladysnake.requiem.common.remnant.RemnantTypes;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class RequiemTestSuite implements FabricGameTest {
    @GameTest(structureName = EMPTY_STRUCTURE)
    public void doTheThing(TestContext ctx) {
        var player = spawnPlayer(ctx, 2, 2, 2);
        GameTestUtil.assertTrue(RemnantComponent.get(player).getRemnantType() == RemnantTypes.MORTAL, "Default remnant type should be mortal");
        player.setStackInHand(Hand.MAIN_HAND, new ItemStack(RequiemItems.SEALED_REMNANT_VESSEL));
        RequiemItems.SEALED_REMNANT_VESSEL.use(ctx.getWorld(), player, Hand.MAIN_HAND);
        GameTestUtil.assertTrue(RemnantComponent.get(player).getRemnantType() == RemnantTypes.REMNANT, "Sealed vessel should convert to remnant");
        ctx.complete();
    }

    public static ServerPlayerEntity spawnPlayer(TestContext ctx, double x, double y, double z) {
        ServerPlayerEntity mockPlayer = new ServerPlayerEntity(ctx.getWorld().getServer(), ctx.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-player"));
        mockPlayer.networkHandler = new ServerPlayNetworkHandler(ctx.getWorld().getServer(), new ClientConnection(NetworkSide.CLIENTBOUND), mockPlayer);
        Vec3d vec3d = ctx.getAbsolute(new Vec3d(x, y, z));
        mockPlayer.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, mockPlayer.getYaw(), mockPlayer.getPitch());
        ctx.getWorld().spawnEntity(mockPlayer);
        return mockPlayer;
    }
}
