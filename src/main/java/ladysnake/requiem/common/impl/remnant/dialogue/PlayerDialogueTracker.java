/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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
package ladysnake.requiem.common.impl.remnant.dialogue;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public final class PlayerDialogueTracker implements DialogueTracker {
    public static final Identifier BECOME_REMNANT = Requiem.id("become_remnant");
    public static final Identifier STAY_MORTAL = Requiem.id("stay_mortal");

    private DialogueRegistry manager;
    @Nullable
    private CutsceneDialogue currentDialogue;
    private PlayerEntity player;

    public PlayerDialogueTracker(PlayerEntity player) {
        this.manager = DialogueRegistry.get(player.world);
        this.player = player;
    }

    @Override
    public void handleAction(Identifier action) {
        if (!this.player.world.isClient) {
            this.manager.getAction(action).handle((ServerPlayerEntity) this.player);
        } else {
            Requiem.LOGGER.warn("PlayerDialogueTracker#handleAction called on the wrong side !");
        }
    }

    @Override
    public void startDialogue(Identifier id) {
        this.currentDialogue = this.manager.getDialogue(id);
        this.currentDialogue.start();
    }

    @Override
    public void endDialogue() {
        this.currentDialogue = null;
    }

    @Nullable
    @Override
    public CutsceneDialogue getCurrentDialogue() {
        return this.currentDialogue;
    }
}
