package ladysnake.requiem.api.v1.dialogue;

import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface DialogueAction {
    void handle(ServerPlayerEntity player);

    DialogueAction NONE = (p) -> {};
}
