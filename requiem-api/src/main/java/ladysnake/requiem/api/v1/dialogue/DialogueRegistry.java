/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package ladysnake.requiem.api.v1.dialogue;

import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.internal.ApiInternals;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apiguardian.api.API;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

public interface DialogueRegistry {
    /**
     * Retrieve the dialogue registry for the given {@link World}.
     *
     * <p> If {@code world} is {@code null}, the returned dialogue registry is the one
     * used by server worlds. Although that registry is fit for registering new dialogues,
     * one should prefer using implementing {@link RequiemPlugin#registerDialogueActions(DialogueRegistry)}
     * to do so at an appropriate time.
     *
     * @param world the world for which to get the dialogue registry, or {@code null} to get the main registry
     * @return the dialogue registry for the given world
     */
    static DialogueRegistry get(@Nullable World world) {
        return ApiInternals.getDialogueRegistry(world);
    }

    CutsceneDialogue getDialogue(Identifier id);

    @API(status = EXPERIMENTAL)
    void registerAction(Identifier actionId, DialogueAction action);

    @API(status = EXPERIMENTAL)
    DialogueAction getAction(Identifier actionId);
}
