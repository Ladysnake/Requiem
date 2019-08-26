/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.pandemonium.client;

import ladysnake.requiem.Requiem;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static ladysnake.requiem.common.network.RequiemNetworking.createEmptyBuffer;
import static ladysnake.requiem.common.network.RequiemNetworking.sendToServer;

public class FractureKeyBinding {

    public static final Identifier ETHEREAL_FRACTURE = Requiem.id("ethereal_fracture");

    public static final FabricKeyBinding etherealFractureKey = FabricKeyBinding.Builder.create(
            ETHEREAL_FRACTURE,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_WORLD_2,
            "key.categories.gameplay"
    ).build();

    public static void init() {
        KeyBindingRegistry.INSTANCE.register(etherealFractureKey);
    }

    public static void update(MinecraftClient client) {
        if (client.player != null && etherealFractureKey.wasPressed()) {
            sendToServer(ETHEREAL_FRACTURE, createEmptyBuffer());
        }
    }
}
