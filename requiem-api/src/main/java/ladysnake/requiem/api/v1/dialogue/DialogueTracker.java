/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;

import javax.annotation.Nullable;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

public interface DialogueTracker extends Component {
    ComponentKey<DialogueTracker> KEY = ComponentRegistry.getOrCreate(new Identifier("requiem", "dialogue_tracker"), DialogueTracker.class);

    static DialogueTracker get(PlayerEntity player) {
        return KEY.get(player);
    }

    @API(status = EXPERIMENTAL)
    void handleAction(Identifier action);

    void startDialogue(Identifier dialogue);

    void endDialogue();

    @Nullable
    CutsceneDialogue getCurrentDialogue();
}
