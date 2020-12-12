package ladysnake.requiem.api.v1.event.requiem;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

public interface RemnantStateChangeCallback {
    void onRemnantStateChange(PlayerEntity player, RemnantComponent state);

    /**
     * Fired after a player dissociates from or merges with a congruous body
     */
    Event<RemnantStateChangeCallback> EVENT = EventFactory.createArrayBacked(RemnantStateChangeCallback.class,
        (callbacks) -> (player, remnant) -> {
            for (RemnantStateChangeCallback callback : callbacks) {
                callback.onRemnantStateChange(player, remnant);
            }
        });
}
