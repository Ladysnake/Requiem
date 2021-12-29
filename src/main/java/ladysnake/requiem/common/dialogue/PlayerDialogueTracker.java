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
package ladysnake.requiem.common.dialogue;

import com.google.common.base.Preconditions;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.common.screen.DialogueScreenHandler;
import ladysnake.requiem.common.screen.RequiemScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public final class PlayerDialogueTracker implements DialogueTracker {
    public static final Identifier BECOME_REMNANT = Requiem.id("become_remnant");
    public static final Identifier BECOME_WANDERING_SPIRIT = Requiem.id("become_wandering_spirit");
    public static final Identifier STAY_MORTAL = Requiem.id("stay_mortal");

    private final DialogueRegistry manager;
    private final PlayerEntity player;
    private @Nullable CutsceneDialogue currentDialogue;

    public PlayerDialogueTracker(PlayerEntity player) {
        this.manager = DialogueRegistry.get();
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
        this.currentDialogue = this.manager.startDialogue(this.player.world, id);
        KEY.sync(this.player);
    }

    @Override
    public void endDialogue() {
        this.currentDialogue = null;

        if (this.player instanceof ServerPlayerEntity sp && this.player.currentScreenHandler instanceof DialogueScreenHandler) {
            sp.closeScreenHandler();
        }
    }

    @Nullable
    @Override
    public CutsceneDialogue getCurrentDialogue() {
        return this.currentDialogue;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("current_dialogue_id", NbtElement.STRING_TYPE)) {
            Identifier dialogueId = Identifier.tryParse(tag.getString("current_dialogue_id"));
            if (dialogueId != null) {
                try {
                    this.startDialogue(dialogueId);
                    if (this.currentDialogue != null && tag.contains("current_dialogue_state", NbtElement.STRING_TYPE)) {
                        ((DialogueStateMachine) this.currentDialogue).selectState(tag.getString("current_dialogue_state"));
                    }
                } catch (IllegalArgumentException e) {
                    Requiem.LOGGER.warn("[Requiem] Unknown dialogue {}", dialogueId);
                }
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (this.currentDialogue != null) {
            tag.putString("current_dialogue_id", this.currentDialogue.getId().toString());
            tag.putString("current_dialogue_state", this.currentDialogue.getCurrentStateKey());
        }
    }

    @Override
    public void serverTick() {
        if (this.currentDialogue != null && this.currentDialogue.isUnskippable() && this.player.currentScreenHandler == null) {
            this.openDialogueScreen();
        }
    }

    private void openDialogueScreen() {
        Preconditions.checkState(this.currentDialogue != null);
        this.player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, p) -> new DialogueScreenHandler(
            RequiemScreenHandlers.DIALOGUE_SCREEN_HANDLER,
            syncId,
            this.currentDialogue.isUnskippable()
        ), Text.of("Requiem Dialogue Screen")));
    }
}
