package ladysnake.requiem.api.v1.dialogue;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public interface DialogueTracker {
    void handleAction(Identifier action);

    void startDialogue(Identifier dialogue);

    void endDialogue();

    @Nullable
    CutsceneDialogue getCurrentDialogue();
}
