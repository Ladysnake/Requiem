package ladysnake.requiem.common.impl.remnant.dialogue;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.dialogue.DialogueRegistry;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public class DialogueTrackerImpl implements DialogueTracker {
    public static final Identifier BECOME_REMNANT = Requiem.id("become_remnant");
    public static final Identifier STAY_MORTAL = Requiem.id("stay_mortal");

    private DialogueRegistry manager;
    @Nullable
    private CutsceneDialogue currentDialogue;
    private PlayerEntity player;

    public DialogueTrackerImpl(PlayerEntity player) {
        this.manager = Requiem.getDialogueManager(player.world.isClient);
        this.player = player;
    }

    @Override
    public void handleAction(Identifier action) {
        if (!this.player.world.isClient) {
            this.manager.getAction(action).handle((ServerPlayerEntity) this.player);
        } else {
            Requiem.LOGGER.warn("DialogueTrackerImpl#handleAction called on the wrong side !");
        }
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
