package ladysnake.satin.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface ResolutionChangeCallback {
    /**
     * Fired each time Minecraft's window resolution changes
     */
    Event<ResolutionChangeCallback> EVENT = EventFactory.createArrayBacked(ResolutionChangeCallback.class,
            (listeners) -> (newWidth, newHeight) -> {
                for (ResolutionChangeCallback event : listeners) {
                    event.onWindowResized(newWidth, newHeight);
                }
            });

    void onWindowResized(int newWidth, int newHeight);
}
