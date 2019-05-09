package ladysnake.requiem.api.v1.dialogue;

import net.minecraft.util.Identifier;

public interface DialogueRegistry {
    CutsceneDialogue getDialogue(Identifier id);

    void registerAction(Identifier actionId, DialogueAction action);

    DialogueAction getAction(Identifier actionId);
}
