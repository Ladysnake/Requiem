package ladysnake.requiem.common.impl.remnant.dialogue;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.dialogue.DialogueManager;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.common.advancement.criterion.RequiemCriteria;
import ladysnake.requiem.common.remnant.RemnantStates;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public class DialogueTrackerImpl implements DialogueTracker {
    public static final Identifier BECOME_REMNANT = Requiem.id("become_remnant");
    public static final Identifier STAY_MORTAL = Requiem.id("stay_mortal");

    private DialogueManager manager;
    @Nullable
    private CutsceneDialogue currentDialogue;
    private PlayerEntity player;

    public DialogueTrackerImpl(DialogueManager manager, PlayerEntity player) {
        this.manager = manager;
        this.player = player;
    }

    @Override
    public void handleAction(Identifier action) {
        if (BECOME_REMNANT.equals(action) || STAY_MORTAL.equals(action)) {
            RemnantType chosenType = BECOME_REMNANT.equals(action) ? RemnantStates.REMNANT : RemnantStates.MORTAL;
            if (!this.player.world.isClient) {
                RequiemCriteria.MADE_REMNANT_CHOICE.handle((ServerPlayerEntity) this.player, chosenType);
            }
        }
        Requiem.LOGGER.warn("[DialogueTracker] Unknown action {}", action);
    }

    @Override
    public void startDialogue(Identifier id) {
        this.currentDialogue = this.manager.getDialogue(id);
    }

    @Nullable
    @Override
    public CutsceneDialogue getCurrentDialogue() {
        return this.currentDialogue;
    }
}
