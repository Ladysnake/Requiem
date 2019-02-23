package ladysnake.dissolution.api.v1.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface PlayerCloneCallback {
    void onPlayerClone(ServerPlayerEntity original, ServerPlayerEntity clone, boolean returnFromEnd);

    Event<PlayerCloneCallback> EVENT = EventFactory.createArrayBacked(PlayerCloneCallback.class,
            (listeners) -> (original, clone, returnFromEnd) -> {
                for (PlayerCloneCallback handler : listeners) {
                    handler.onPlayerClone(original, clone, returnFromEnd);
                }
            });
}
