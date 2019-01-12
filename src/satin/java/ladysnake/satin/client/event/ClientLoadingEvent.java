package ladysnake.satin.client.event;

import net.fabricmc.fabric.util.HandlerArray;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.resource.ReloadableResourceManager;
import org.apiguardian.api.API;

import java.util.function.Consumer;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Events fired during client initialization
 */
@API(status = EXPERIMENTAL, since = "3.0.0")
public final class ClientLoadingEvent {
    /**
     * Fired when Minecraft creates its resource manager
     */
    public static final HandlerRegistry<Consumer<ReloadableResourceManager>> RESOURCE_MANAGER = new HandlerArray<>(Consumer.class);

    private ClientLoadingEvent() { }
}
