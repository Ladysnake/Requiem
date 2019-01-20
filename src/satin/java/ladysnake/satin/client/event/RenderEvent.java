package ladysnake.satin.client.event;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.fabric.util.HandlerArray;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.class_856;
import net.minecraft.entity.Entity;
import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Events emitted during the rendering process
 */
@API(status = EXPERIMENTAL, since = "3.0.0")
public final class RenderEvent {

    @FunctionalInterface
    public interface PreBlockEntities {
        void onPreRenderBlockEntities(Entity camera, class_856 frustum, float tickDelta);
    }
    /**
     * Fired when Minecraft renders the entity outline framebuffer.
     * Post process shader effects should generally be rendered at that time.
     */
    public static final HandlerRegistry<FloatConsumer> SHADER_EFFECT = new HandlerArray<>(FloatConsumer.class);

    /**
     * Fired after Minecraft has rendered all entities and before it renders block entities.
     */
    public static final HandlerRegistry<PreBlockEntities> BLOCK_ENTITIES_RENDER = new HandlerArray<>(PreBlockEntities.class);

    private RenderEvent() { }
}
