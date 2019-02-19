package ladysnake.satin.client.shader;

import ladysnake.satin.client.event.ResolutionChangeCallback;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;

public final class ShaderEffectManager implements SimpleSynchronousResourceReloadListener, ResolutionChangeCallback {
    public static final ShaderEffectManager INSTANCE = new ShaderEffectManager();
    public static final Identifier SHADER_RESOURCE_KEY = new Identifier("dissolution:shaders");

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
    public Identifier getFabricId() {
        return SHADER_RESOURCE_KEY;
    }

    @Override
    public void apply(ResourceManager var1) {
        for (ManagedShaderEffect ss : managedShaderEffects) {
            ss.release();
        }
    }

    @Override
    public void onWindowResized(int newWidth, int newHeight) {
        if (!ShaderHelper.areShadersDisallowed() && !managedShaderEffects.isEmpty()) {
            for (ManagedShaderEffect ss : managedShaderEffects) {
                if (ss.isInitialized()) {
                    ss.setup(newWidth, newHeight);
                }
            }
        }
    }

}
