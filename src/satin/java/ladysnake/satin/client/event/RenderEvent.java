package ladysnake.satin.client.event;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.fabricmc.fabric.util.HandlerArray;
import net.fabricmc.fabric.util.HandlerRegistry;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;

import java.util.function.Function;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

/**
 * Events emitted during the rendering process
 */
@API(status = EXPERIMENTAL, since = "3.0.0")
public final class RenderEvent {

    @FunctionalInterface
    public interface PreBlockEntities {
        void onPreRenderBlockEntities(Entity camera, VisibleRegion frustum, float tickDelta);
    }

    @FunctionalInterface
    public interface WindowResized {
        void onWindowResized(int newWidth, int newHeight);
    }

    /**
     * Fired when Minecraft renders the entity outline framebuffer.
     * Post process shader effects should generally be rendered at that time.
     */
    public static final HandlerRegistry<FloatConsumer> SHADER_EFFECT = new HandlerArray<>(FloatConsumer.class);

    /**
     * Fired in {@link net.minecraft.client.render.GameRenderer#onCameraEntitySet(Entity)}
     */
    public static final HandlerRegistry<Function<Entity, Identifier>> PICK_ENTITY_SHADER = new HandlerArray<>(Function.class);

    /**
     * Fired after Minecraft has rendered all entities and before it renders block entities.
     */
    public static final HandlerRegistry<PreBlockEntities> BLOCK_ENTITIES_RENDER = new HandlerArray<>(PreBlockEntities.class);

    /**
     * Fired each time Minecraft's window is resized
     */
    public static final HandlerRegistry<WindowResized> WINDOW_RESIZED = new HandlerArray<>(WindowResized.class);

    private RenderEvent() { }
}
