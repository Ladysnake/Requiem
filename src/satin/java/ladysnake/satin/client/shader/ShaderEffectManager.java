package ladysnake.satin.client.shader;

import ladysnake.satin.client.event.RenderEvent;
import net.fabricmc.fabric.util.HandlerArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static org.apiguardian.api.API.Status.*;

public final class ShaderEffectManager implements ResourceReloadListener {
    public static final ShaderEffectManager INSTANCE = new ShaderEffectManager();

    private static int oldDisplayWidth = -1;
    private static int oldDisplayHeight = -1;

    // Let shaders be garbage collected when no one uses them
    private static Set<ManagedShaderEffect> managedShaderEffects = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Manages a post processing shader loaded from a json definition file
     *
     * @param location the location of the json within your mod's assets
     * @return a lazily initialized shader effect
     */
    @API(status = MAINTAINED, since = "2.6.2")
    public static ManagedShaderEffect manage(Identifier location) {
        return manage(location, s -> { });
    }

    /**
     * Manages a post processing shader loaded from a json definition file
     *
     * @param location         the location of the json within your mod's assets
     * @param uniformInitBlock a block ran once to initialize uniforms
     * @return a lazily initialized screen shader
     */
    @API(status = MAINTAINED, since = "2.6.2")
    public static ManagedShaderEffect manage(Identifier location, Consumer<ManagedShaderEffect> uniformInitBlock) {
        ManagedShaderEffect ret = new ManagedShaderEffect(location, uniformInitBlock);
        managedShaderEffects.add(ret);
        return ret;
    }

    /**
     * Removes a shader from the global list of managed shaders,
     * making it not respond to resource reloading and screen resizing.
     * This also calls {@link ManagedShaderEffect#release()} to release the shader's resources.
     * A <code>ManagedShaderEffect</code> object cannot be used after it has been disposed of.
     *
     * @param shader the shader to stop managing
     * @see ManagedShaderEffect#release()
     */
    @API(status = EXPERIMENTAL)
    public static void dispose(ManagedShaderEffect shader) {
        shader.release();
        managedShaderEffects.remove(shader);
    }


    @Override
    public void onResourceReload(ResourceManager resourceManager) {
        for (ManagedShaderEffect ss : managedShaderEffects) {
            ss.release();
        }
    }

    @API(status = INTERNAL)
    public void refreshScreenShaders(MinecraftClient mc) {
        int windowHeight = mc.window.getHeight();
        int windowWidth = mc.window.getWidth();
        if (windowWidth != oldDisplayWidth || oldDisplayHeight != windowHeight) {
            for (RenderEvent.WindowResized handler : ((HandlerArray<RenderEvent.WindowResized>)RenderEvent.WINDOW_RESIZED).getBackingArray()) {
                handler.onWindowResized(windowWidth, windowHeight);
            }
            if (!ShaderHelper.areShadersDisallowed() && !managedShaderEffects.isEmpty()) {
                for (ManagedShaderEffect ss : managedShaderEffects) {
                    if (ss.isInitialized()) {
                        ss.setup(windowWidth, windowHeight);
                    }
                }
            }
            oldDisplayWidth = windowWidth;
            oldDisplayHeight = windowHeight;
        }
    }
}
