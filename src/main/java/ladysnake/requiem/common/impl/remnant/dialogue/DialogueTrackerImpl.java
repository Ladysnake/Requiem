package ladysnake.requiem.common.impl.remnant.dialogue;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.dialogue.CutsceneDialogue;
import ladysnake.requiem.api.v1.dialogue.DialogueManager;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
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

    public DialogueTrackerImpl(PlayerEntity player) {
        this.manager = Requiem.getDialogueManager(player.world.isClient);
        this.player = player;
    }

    @Override
    public void handleAction(Identifier action) {
        if (BECOME_REMNANT.equals(action) || STAY_MORTAL.equals(action)) {
            RemnantType chosenType = BECOME_REMNANT.equals(action) ? RemnantStates.REMNANT : RemnantStates.MORTAL;
            if (!this.player.world.isClient) {
                DeathSuspender deathSuspender = ((RequiemPlayer) player).getDeathSuspender();
                if (deathSuspender.isLifeTransient()) {
                    ((RequiemPlayer) this.player).setRemnantState(chosenType.create(player));
                    RequiemCriteria.MADE_REMNANT_CHOICE.handle((ServerPlayerEntity) player, chosenType);
                    deathSuspender.resumeDeath();
                }
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
