package ladysnake.satin.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resource.ReloadableResourceManager;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Events fired during client initialization
 */
@API(status = EXPERIMENTAL, since = "3.0.0")
public interface ResourceManagerLoadedCallback {
    /**
     * Fired when Minecraft creates its resource manager
     */
    Event<ResourceManagerLoadedCallback> EVENT = EventFactory.createArrayBacked(ResourceManagerLoadedCallback.class,
            (listeners) -> (client) -> {
                for (ResourceManagerLoadedCallback event : listeners) {
                    event.onResourceManagerLoaded(client);
                }
            });

    void onResourceManagerLoaded(ReloadableResourceManager manager);
}
