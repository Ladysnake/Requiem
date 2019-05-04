package ladysnake.requiem.api.v1.dialogue;

import net.minecraft.util.Identifier;

public interface DialogueManager {
    CutsceneDialogue getDialogue(Identifier id);
}
