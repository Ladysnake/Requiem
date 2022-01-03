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
package ladysnake.requiem.common.screen;

import com.google.common.collect.ImmutableList;
import ladysnake.requiem.api.v1.dialogue.ChoiceResult;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class DialogueScreenHandler extends ScreenHandler {
    private final CutsceneDialogue dialogue;

    public DialogueScreenHandler(int syncId, CutsceneDialogue dialogue) {
        this(RequiemScreenHandlers.DIALOGUE_SCREEN_HANDLER, syncId, dialogue);
    }

    public DialogueScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId, CutsceneDialogue dialogue) {
        super(type, syncId);
        this.dialogue = dialogue;
    }

    public boolean isUnskippable() {
        return this.dialogue.isUnskippable();
    }

    public Text getCurrentText() {
        return this.dialogue.getCurrentText();
    }

    public ImmutableList<Text> getCurrentChoices() {
        return this.dialogue.getCurrentChoices();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public ChoiceResult makeChoice(int choice) {
        RequiemNetworking.sendDialogueActionMessage(choice);
        return this.dialogue.choose(choice, action -> {});
    }

    public void makeChoice(ServerPlayerEntity player, int choice) {
        ChoiceResult result = this.dialogue.choose(choice, action -> action.handle(player));
        if (result == ChoiceResult.END_DIALOGUE) {
            DialogueTracker.get(player).endDialogue();
        }
    }
}
