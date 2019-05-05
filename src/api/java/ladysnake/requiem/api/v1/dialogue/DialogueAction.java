package ladysnake.requiem.api.v1.dialogue;

import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface DialogueAction {
    /**
     * Handles a dialogue action triggered by the given player.
     * <p>
     * There is no guarantee that the game is in the desired state at
     * the time this method is called, as such it should do all necessary
     * checks to prevent possible exploits.
     *
     * @param player the player executing the action
     */
    void handle(ServerPlayerEntity player);

    DialogueAction NONE = (p) -> {};
}
