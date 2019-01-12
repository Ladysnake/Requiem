package ladysnake.satin.client.event;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.fabric.util.HandlerArray;
import net.fabricmc.fabric.util.HandlerRegistry;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Events emitted during the rendering process
 */
@API(status = EXPERIMENTAL, since = "3.0.0")
public final class RenderEvent {
    /**
     * Fired when Minecraft renders the entity outline shader.
     * Post process shader effects should generally be rendered at that time.
     */
    public static final HandlerRegistry<FloatConsumer> SHADER_EFFECT = new HandlerArray<>(FloatConsumer.class);

    private RenderEvent() { }
}
