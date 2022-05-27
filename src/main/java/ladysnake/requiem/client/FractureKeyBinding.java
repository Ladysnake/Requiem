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
package ladysnake.requiem.client;

import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.network.RequiemNetworking;
import ladysnake.requiem.core.tag.RequiemCoreTags;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.mob.MobEntity;
import org.lwjgl.glfw.GLFW;

import static ladysnake.requiem.common.network.RequiemNetworking.createEmptyBuffer;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToServer;

public final class FractureKeyBinding {

    public static final String ETHEREAL_FRACTURE = "key.requiem.dissociation";

    public static final KeyBinding etherealFractureKey = new KeyBinding(
            ETHEREAL_FRACTURE,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_COMMA,  // '<'
            "key.categories.gameplay"
        );

    public static void init() {
        KeyBindingHelper.registerKeyBinding(etherealFractureKey);
        ClientTickEvents.END_CLIENT_TICK.register(FractureKeyBinding::update);
    }

    public static void update(MinecraftClient client) {
        if (client.player != null) {
            if (etherealFractureKey.wasPressed() || pressesEmergencyFracture(client.player)) {
                sendToServer(RequiemNetworking.ETHEREAL_FRACTURE, createEmptyBuffer());
            }
        }
    }

    private static boolean pressesEmergencyFracture(ClientPlayerEntity player) {
        // Immovable mobs are a specific kind of boring, so we let players leave them through a simple sneak
        if (player.isSneaking()) {
            MobEntity possessedEntity = PossessionComponent.getHost(player);
            return possessedEntity != null && possessedEntity.getType().isIn(RequiemCoreTags.Entity.IMMOVABLE);
        }
        return false;
    }
}
