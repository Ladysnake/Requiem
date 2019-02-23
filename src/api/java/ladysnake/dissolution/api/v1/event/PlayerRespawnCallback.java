package ladysnake.dissolution.api.v1.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface PlayerRespawnCallback {
    void onPlayerRespawn(ServerPlayerEntity player, boolean returnFromEnd);

    Event<PlayerRespawnCallback> EVENT = EventFactory.createArrayBacked(PlayerRespawnCallback.class,
            (listeners) -> (player, returnFromEnd) -> {
                for (PlayerRespawnCallback handler : listeners) {
                    handler.onPlayerRespawn(player, returnFromEnd);
                }
            });
}
