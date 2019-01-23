package ladysnake.satin.client.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.satin.Satin;
import ladysnake.satin.mixin.client.gl.AccessiblePassesShaderEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlFramebuffer;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.util.Identifier;
import org.apiguardian.api.API;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

import static org.apiguardian.api.API.Status.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * A post processing shader that is applied to the main framebuffer
 * <p>
 *     Post shaders loaded through {@link ShaderEffectManager#manage(Identifier, Consumer)} are self-managed and will be
 *     reloaded when shader assets are reloaded (through <tt>F3-T</tt> or <tt>/ladylib_shader_reload</tt>) or the
 *     screen resolution changes.
 * <p>
 * @since 2.4.0
 * @see ShaderHelper
 * @see ShaderEffectManager
 * @see "<tt>assets/minecraft/shaders</tt> for examples"
 */
public final class ManagedShaderEffect {

    private final Identifier location;
    private final Consumer<ManagedShaderEffect> uniformInitBlock;
    private ShaderEffect shaderGroup;
    private boolean errored;

    /**
     * Creates a new shader effect. <br>
     * <b>Users should call {@link ShaderEffectManager} to obtain instances of this class.</b>
     * @param location the location of a shader effect JSON definition file
     * @param uniformInitBlock code to run in {@link #setup(int, int)}
     * @see ShaderEffectManager#manage(Identifier)
     * @see ShaderEffectManager#manage(Identifier, Consumer)
     */
    @API(status = INTERNAL, consumers = "ladysnake.satin.client.shader")
    ManagedShaderEffect(Identifier location, Consumer<ManagedShaderEffect> uniformInitBlock) {
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
                Satin.LOGGER.error("Could not create screen shader {}", location, e);
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
     * @see #release()
     */
    @API(status = MAINTAINED, since = "2.6.2")
    public void initialize() throws IOException {
        this.release();
        MinecraftClient mc = MinecraftClient.getInstance();
        this.shaderGroup = new ShaderEffect(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), this.location);
        this.setup(mc.window.getWidth(), mc.window.getHeight());
    }

    @API(status = INTERNAL)
    public void setup(int windowWidth, int windowHeight) {
        this.shaderGroup.setupDimensions(windowWidth, windowHeight);
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
     *     Although the finalization process of the garbage collector
     *     also disposes of the same system resources, it is preferable
     *     to manually free the associated resources by calling this
     *     method rather than to rely on a finalization process which
     *     may not run to completion for a long period of time.
     * <p>
     *     If the caller does not intend to use this shader effect again, they
     *     should call {@link ShaderEffectManager#dispose(ManagedShaderEffect)}.
     * </p>
     * @see ShaderEffectManager#dispose(ManagedShaderEffect)
     * @see #isInitialized()
     * @see #getShaderEffect()
     * @see #finalize()
     */
    @API(status = EXPERIMENTAL, since = "3.0.0")
    public void release() {
        if (this.isInitialized()) {
            this.shaderGroup.close();
            this.shaderGroup = null;
        }
        this.errored = false;
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
    public void render(float tickDelta) {
        ShaderEffect sg = this.getShaderEffect();
        if (sg != null) {
            GlStateManager.matrixMode(GL_TEXTURE);
            GlStateManager.loadIdentity();
            sg.render(tickDelta);
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // restore blending
            GlStateManager.enableDepthTest();
            GlStateManager.matrixMode(GL_MODELVIEW);
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
     * @see ShaderEffectManager#dispose(ManagedShaderEffect)
     * @see #release
     */
    @Override
    protected void finalize() {
        ShaderEffectManager.dispose(this);
    }

}