package ladysnake.dissolution.lib.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.dissolution.Dissolution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import static org.apiguardian.api.API.Status.*;

/**
 * A post processing shader that is applied to the main framebuffer
 * <p>
 *     Post shaders loaded through {@link #loadShader(Identifier, Consumer)} are self-managed and will be
 *     reloaded when shader assets are reloaded (through <tt>F3-T</tt> or <tt>/ladylib_shader_reload</tt>) or the
 *     screen resolution changes.
 * <p>
 * @since 2.4.0
 * @see ShaderHelper
 * @see "<tt>assets/minecraft/shaders</tt> for examples"
 */
public final class ManagedShaderEffect {

    // Let shaders be garbage collected when no one uses them
    private static Set<ManagedShaderEffect> managedShaderEffects = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Loads a post processing shader from a json definition file
     * @param location the location of the json within your mod's assets
     * @return a lazily initialized screen shader
     */
    @API(status = MAINTAINED, since = "2.6.2")
    public static ManagedShaderEffect loadShader(Identifier location) {
        return loadShader(location, s -> {});
    }

    /**
     * Loads a post processing shader from a json definition file
     * @param location the location of the json within your mod's assets
     * @param uniformInitBlock a block ran once to initialize uniforms
     * @return a lazily initialized screen shader
     */
    @API(status = MAINTAINED, since = "2.6.2")
    public static ManagedShaderEffect loadShader(Identifier location, Consumer<ManagedShaderEffect> uniformInitBlock) {
        ManagedShaderEffect ret = new ManagedShaderEffect(location, uniformInitBlock);
        managedShaderEffects.add(ret);
        return ret;
    }

    private final Identifier location;
    private final Consumer<ManagedShaderEffect> uniformInitBlock;
    private ShaderEffect shaderGroup;
    private boolean errored;

    private ManagedShaderEffect(Identifier location, Consumer<ManagedShaderEffect> uniformInitBlock) {
        this.location = location;
        this.uniformInitBlock = uniformInitBlock;
    }

    /**
     * Returns this shader's {@link ShaderEffect}, creating and initializing it if it doesn't exist.
     * <p>
     *     This method will return <code>null</code> if an error occurs during initialization.
     * <p>
     *     <em>Note: calling this before the graphic context is ready will cause issues.</em>
     * @see #initialize()
     * @see #isInitialized()
     */
    @Nullable
    @API(status = MAINTAINED, since = "2.6.2")
    public ShaderEffect getShaderEffect() {
        if (!this.isInitialized() && !this.errored) {
            try {
                initialize();
            } catch (Exception e) {
                Dissolution.LOGGER.error("Could not create screen shader {}", location, e);
                this.errored = true;
            }
        }
        return this.shaderGroup;
    }

    /**
     * Initializes this shader, allocating required system resources
     * such as framebuffer objects, shaders objects and texture objects.
     * Any exception thrown during initialization is relayed to the caller.
     * <p>
     *     If the shader is already initialized, previously allocated
     *     resources will be disposed of before initializing new ones.
     * @apiNote Calling this method directly is not required in most cases.
     * @see #getShaderEffect()
     * @see #isInitialized()
     * @see #dispose(boolean)
     */
    @API(status = MAINTAINED, since = "2.6.2")
    public void initialize() throws IOException {
        this.dispose(false);
        MinecraftClient mc = MinecraftClient.getInstance();
        this.shaderGroup = new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), this.location);
        this.shaderGroup.setupDimensions(mc.window.getWindowWidth(), mc.window.getWindowHeight());
        this.uniformInitBlock.accept(this);
    }

    /**
     * Checks whether this shader is initialized. If it is not, next call to {@link #getShaderEffect()}
     * will setup the shader group.
     * @return true if this does not require initialization
     * @see #initialize()
     */
    @API(status = MAINTAINED, since = "2.6.2")
    public boolean isInitialized() {
        return this.shaderGroup != null;
    }

    /**
     * @return <code>true</code> if this shader erred during initialization
     */
    @API(status = MAINTAINED, since = "2.6.2")
    public boolean isErrored() {
        return this.errored;
    }

    /**
     * Releases this shader's resources.
     * <p>
     *     After this method is called, this shader will go back to its uninitialized state.
     *     Future calls to {@link #isInitialized()} will return false until {@link #initialize()}
     *     is called again, recreating the shader group.
     * <p>
     *     If <code>removeFromManaged</code> is true, this shader will also be removed from the global
     *     list of managed shaders, making it not respond to resource reloading and screen resizing.
     *     A <code>ManagedShaderEffect</code> object cannot be used after <code>dispose(true)</code>
     *     has been called.
     * <p>
     *     Although the finalization process of the garbage collector
     *     also disposes of the same system resources, it is preferable
     *     to manually free the associated resources by calling this
     *     method rather than to rely on a finalization process which
     *     may not run to completion for a long period of time.
     * @param removeFromManaged whether this shader should stop being automatically managed
     * @see #isInitialized()
     * @see #getShaderEffect()
     * @see #finalize()
     */
    @API(status = STABLE, since = "2.6.2")
    public void dispose(boolean removeFromManaged) {
        if (this.isInitialized()) {
            this.shaderGroup.close();
            this.shaderGroup = null;
        }
        this.errored = false;
        if (removeFromManaged) {
            managedShaderEffects.remove(this);
        }
    }

    /**
     * Renders this shader.
     *
     * <p>
     *     Calling this method first setups the graphic state for rendering,
     *     then uploads uniforms to the GPU if they have been changed since last
     *     draw, draws the {@link MinecraftClient#getFramebuffer() main framebuffer}'s texture
     *     to intermediate {@link GlFramebuffer framebuffers} as defined by the JSON files
     *     and resets part of the graphic state. The shader will be {@link #initialize() initialized}
     *     if it has not been before.
     * <p>
     *     This method should be called every frame when the shader is active.
     *     Uniforms should be set before rendering.
     */
    @API(status = STABLE, since = "2.6.2")
    public void render(float partialTicks) {
        ShaderEffect sg = this.getShaderEffect();
        if (sg != null) {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            sg.render(partialTicks);
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // restore blending
            GlStateManager.enableDepthTest();
        }
    }

    /**
     * Forwards to {@link #setupDynamicUniforms(int, Runnable)} with an index of 0
     * @param dynamicSetBlock a block in which dynamic uniforms are set
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public void setupDynamicUniforms(Runnable dynamicSetBlock) {
        this.setupDynamicUniforms(0, dynamicSetBlock);
    }

    /**
     * Runs the given block while the shader at the given index is active
     *
     * @param index the shader index within the group
     * @param dynamicSetBlock a block in which dynamic name uniforms are set
     */
    @API(status = EXPERIMENTAL, since = "2.6.2")
    public void setupDynamicUniforms(int index, Runnable dynamicSetBlock) {
        AccessiblePassesShaderEffect sg = (AccessiblePassesShaderEffect) this.getShaderEffect();
        if (sg != null) {
            GlProgram sm = sg.getPasses().get(index).getProgram();
            ShaderHelper.useShader(sm.getProgramRef());
            dynamicSetBlock.run();
            ShaderHelper.revert();
        }
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value int value
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, int value) {
        setUniformValue(uniformName, value, 0, 0, 0);
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value0 int value
     * @param value1 int value
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, int value0, int value1) {
        setUniformValue(uniformName, value0, value1, 0, 0);
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value0 int value
     * @param value1 int value
     * @param value2 int value
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, int value0, int value1, int value2) {
        setUniformValue(uniformName, value0, value1, value2, 0);
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value0 int value
     * @param value1 int value
     * @param value2 int value
     * @param value3 int value
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, int value0, int value1, int value2, int value3) {
        AccessiblePassesShaderEffect sg = (AccessiblePassesShaderEffect) this.getShaderEffect();
        if (sg != null) {
            for (PostProcessShader shader : sg.getPasses()) {
                shader.getProgram().getUniformByNameOrDummy(uniformName).put(value0, value1, value2, value3);
            }
        }
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value float value
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, float value) {
        AccessiblePassesShaderEffect sg = (AccessiblePassesShaderEffect) this.getShaderEffect();
        if (sg != null) {
            for (PostProcessShader shader : sg.getPasses()) {
                shader.getProgram().getUniformByNameOrDummy(uniformName).put(value);
            }
        }
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value0 float value
     * @param value1 float value
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, float value0, float value1) {
        AccessiblePassesShaderEffect sg = (AccessiblePassesShaderEffect) this.getShaderEffect();
        if (sg != null) {
            for (PostProcessShader shader : sg.getPasses()) {
                shader.getProgram().getUniformByNameOrDummy(uniformName).put(value0, value1);
            }
        }
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value0 float value
     * @param value1 float value
     * @param value2 float value
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, float value0, float value1, float value2) {
        AccessiblePassesShaderEffect sg = (AccessiblePassesShaderEffect) this.getShaderEffect();
        if (sg != null) {
            for (PostProcessShader shader : sg.getPasses()) {
                shader.getProgram().getUniformByNameOrDummy(uniformName).put(value0, value1, value2);
            }
        }
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value0 float value
     * @param value1 float value
     * @param value2 float value
     * @param value3 float value
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, float value0, float value1, float value2, float value3) {
        AccessiblePassesShaderEffect sg = (AccessiblePassesShaderEffect) this.getShaderEffect();
        if (sg != null) {
            for (PostProcessShader shader : sg.getPasses()) {
                shader.getProgram().getUniformByNameOrDummy(uniformName).put(value0, value1, value2, value3);
            }
        }
    }

    /**
     * Sets the value of a uniform declared in json
     * @param uniformName the name of the uniform field in the shader source file
     * @param value a matrix
     */
    @API(status = STABLE, since = "2.6.2")
    public void setUniformValue(String uniformName, Matrix4f value) {
        AccessiblePassesShaderEffect sg = (AccessiblePassesShaderEffect) this.getShaderEffect();
        if (sg != null) {
            for (PostProcessShader shader : sg.getPasses()) {
                shader.getProgram().getUniformByNameOrDummy(uniformName).put(value);
            }
        }
    }

    /**
     * Sets the value of a sampler uniform declared in json
     * @param samplerName the name of the sampler uniform field in the shader source file and json
     * @param texture a texture object
     */
    @API(status = STABLE, since = "2.6.2")
    public void setSamplerUniform(String samplerName, Texture texture) {
        setSamplerUniform(samplerName, (Object) texture);
    }

    /**
     * Sets the value of a sampler uniform declared in json
     * @param samplerName the name of the sampler uniform field in the shader source file and json
     * @param textureFbo a framebuffer which main texture will be used
     */
    @API(status = STABLE, since = "2.6.2")
    public void setSamplerUniform(String samplerName, GlFramebuffer textureFbo) {
        setSamplerUniform(samplerName, (Object) textureFbo);
    }

    /**
     * Sets the value of a sampler uniform declared in json
     * @param samplerName the name of the sampler uniform field in the shader source file and json
     * @param textureName an opengl texture name
     */
    @API(status = STABLE, since = "2.6.2")
    public void setSamplerUniform(String samplerName, int textureName) {
        setSamplerUniform(samplerName, Integer.valueOf(textureName));
    }

    private void setSamplerUniform(String samplerName, Object texture) {
        AccessiblePassesShaderEffect sg = (AccessiblePassesShaderEffect) this.getShaderEffect();
        if (sg != null) {
            for (PostProcessShader shader : sg.getPasses()) {
                shader.getProgram().bindSampler(samplerName, texture);
            }
        }
    }

    /**
     * Disposes of this shader once it is no longer referenced.
     * @see #dispose
     */
    @Override
    protected void finalize() {
        this.dispose(true);
    }

    static class ReloadHandler implements ResourceReloadListener {
        static final ReloadHandler INSTANCE = new ReloadHandler();

        private static int oldDisplayWidth = MinecraftClient.getInstance().window.getWindowWidth();
        private static int oldDisplayHeight = MinecraftClient.getInstance().window.getWindowHeight();

        @Override
        public void onResourceReload(ResourceManager resourceManager) {
            for (ManagedShaderEffect ss : managedShaderEffects) {
                ss.dispose(false);
            }
        }

        void refreshScreenShaders(MinecraftClient mc) {
            if (!ShaderHelper.areShadersForbidden() && !managedShaderEffects.isEmpty()) {
                int windowHeight = mc.window.getWindowHeight();
                int windowWidth = mc.window.getWindowWidth();
                if (windowWidth != oldDisplayWidth || oldDisplayHeight != windowHeight) {
                    for (ManagedShaderEffect ss : managedShaderEffects) {
                        if (ss.isInitialized()) {
                            ss.shaderGroup.setupDimensions(windowWidth, windowHeight);
                            ss.uniformInitBlock.accept(ss);
                        }
                    }

                    oldDisplayWidth = windowWidth;
                    oldDisplayHeight = windowHeight;
                }
            }
        }
    }

}