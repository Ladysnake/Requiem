package ladysnake.satin.client.shader;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import ladysnake.satin.Satin;
import ladysnake.satin.client.event.RenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apiguardian.api.API;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.lwjgl.opengl.GL20.*;

public class ShaderHelper {

    public static final String SHADER_LOCATION_PREFIX = "shaders/";

    private static boolean initialized = false;
    /**Used for opengl interactions such as matrix retrieval*/
    private static FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
    /**A secondary buffer for the same purpose as {@link #buffer}*/
    private static FloatBuffer buffer1 = BufferUtils.createFloatBuffer(16);

    /* Generic shaders variables */

    private static int prevProgram = 0;
    private static int currentProgram = 0;

    /**Map of shader id's to opengl shader names*/
    private static final Object2IntMap<Identifier> linkedShaders = new Object2IntOpenHashMap<>();
    /**A map of programs to maps of uniform names to location*/
    private static final Int2ObjectMap<Object2IntMap<String>> uniformsCache = new Int2ObjectOpenHashMap<>();

    public static boolean areShadersDisallowed() {
        return !GLX.isNextGen();
    }

    /**
     * Subscribes this class to MinecraftClient's resource manager to reload shaders like normal assets.
     */
    @API(status = INTERNAL)
    public static void init(ReloadableResourceManager resourceManager) {
        if (!initialized) {
            resourceManager.addListener(ShaderHelper::loadShaders);
            resourceManager.addListener(ShaderEffectManager.INSTANCE);
            RenderEvent.RESOLUTION_CHANGED.register(ShaderEffectManager.INSTANCE);
//            ClientCommandHandler.instance.registerCommand(new ShaderReloadCommand());
            initialized = true;
        }
    }

    /**
     * Sets the currently used program to a previously registered shader
     *
     * @param id the resource location used to register the shader
     */
    public static void useShader(Identifier id) {
        useShader(linkedShaders.getInt(id));
    }

    /**
     * Sets the currently used program
     *
     * @param program the reference to the desired shader (0 to remove any current shader)
     */
    public static void useShader(int program) {
        if (areShadersDisallowed()) {
            return;
        }

        prevProgram = GlStateManager.getInteger(GL20.GL_CURRENT_PROGRAM);
        GLX.glUseProgram(program);

        currentProgram = program;
    }

    /**
     * Sets the value of an attribute from the current shader program using the given operation.
     * <p>
     * <code>operation</code> will only be called if shaders are enabled and an attribute with the given name exists
     * in the current program. It should call one of {@link GL20} attrib functions (eg. {@link GL20#glBindAttribLocation(int, int, ByteBuffer)}).
     *
     * @param attribName the name of the attribute field in the shader source file
     * @param operation   a gl operation to apply to this uniform
     */
    public static void setAttribValue(String attribName, IntConsumer operation) {
        if (areShadersDisallowed() || currentProgram == 0) {
            return;
        }

        int attrib = GL20.glGetAttribLocation(currentProgram, attribName);
        if (attrib != -1) {
            operation.accept(attrib);
        }
    }

    /**
     * Sets the value of a uniform from the current shader program using the given operation.
     * <p>
     * <code>operation</code> will only be called if shaders are enabled and a uniform with the given name exists
     * in the current program. It should call one of {@link GL20} uniform functions (eg. {@link GL20#glUniform1iv(int, IntBuffer)}).
     *
     * @param uniformName the name of the uniform field in the shader source file
     * @param operation   a gl operation to apply to this uniform
     */
    public static void setUniformValue(String uniformName, IntConsumer operation) {
        if (areShadersDisallowed() || currentProgram == 0) {
            return;
        }

        int uniform = getUniform(uniformName);
        if (uniform != -1) {
            operation.accept(uniform);
        }
    }

    /**
     * Sets the value of an int uniform from the current shader program
     *
     * @param uniformName the name of the uniform field in the shader source file
     * @param value       an int value for this uniform
     */
    public static void setUniform(String uniformName, int value) {
        if (areShadersDisallowed() || currentProgram == 0) {
            return;
        }

        int uniform = getUniform(uniformName);
        if (uniform != -1) {
            GL20.glUniform1i(uniform, value);
        }
    }

    /**
     * Sets the value of an uniform from the current shader program
     * If exactly 1 value is supplied, will set the value of a float uniform field
     * If between 2 and 4 values are supplied, will set the value of a vec uniform of corresponding length
     *
     * @param uniformName the name of the uniform field in the shader source file
     * @param values      between 1 and 4 float values
     */
    public static void setUniform(String uniformName, float... values) {
        if (areShadersDisallowed()) {
            return;
        }

        int uniform = getUniform(uniformName);
        if (uniform != -1) {
            switch (values.length) {
                case 1:
                    GL20.glUniform1f(uniform, values[0]);
                    break;
                case 2:
                    GL20.glUniform2f(uniform, values[0], values[1]);
                    break;
                case 3:
                    GL20.glUniform3f(uniform, values[0], values[1], values[2]);
                    break;
                case 4:
                    GL20.glUniform4f(uniform, values[0], values[1], values[2], values[3]);
                    break;
                default:
                    throw new IllegalArgumentException("Shader float uniforms only support between 1 and 4 values");
            }
        }
    }

    /**
     * Sets the value of a mat4 uniform in the current shader
     *
     * @param uniformName the name of the uniform field in the shader source file
     * @param mat4        a raw array of float values
     */
    public static void setUniform(String uniformName, FloatBuffer mat4) {
        if (areShadersDisallowed()) {
            return;
        }

        int uniform = getUniform(uniformName);
        if (uniform != -1) {
            GL20.glUniformMatrix4fv(uniform, false, mat4);
        }
    }

    private static int getUniform(String uniformName) {
        // Gets the uniform cache for the current program
        Object2IntMap<String> shaderUniformsCache = uniformsCache.get(currentProgram);
        // Computee if absent
        if (shaderUniformsCache == null) {
            shaderUniformsCache = new Object2IntOpenHashMap<>();
            uniformsCache.put(currentProgram, shaderUniformsCache);
        }
        // Gets the uniform location from the cache
        int uniform;
        if (shaderUniformsCache.containsKey(uniformName)) {
            uniform = shaderUniformsCache.getInt(uniformName);
        } else {
            // Compute if absent
            uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
            shaderUniformsCache.put(uniformName, uniform);
        }
        return uniform;
    }

    /**
     * Binds any number of additional textures to be used by the current shader.
     * <p>
     * The default texture (0) is unaffected.
     * Shaders can access these textures by using uniforms named "textureN" with N
     * being the index of the additional texture, starting at 1.
     * </p>
     *
     * <u>Example:</u> The call {@code bindAdditionalTextures(rl1, rl2, rl3)} will let the shader
     * access those textures via the uniforms <pre>{@code
     * uniform sampler2D texture;   // the texture that's currently being drawn
     * uniform sampler2D texture1;  // the texture designated by rl1
     * uniform sampler2D texture2;  // the texture designated by rl2
     * uniform sampler2D texture3;  // the texture designated by rl3
     * }</pre>
     */
    public static void bindAdditionalTextures(Identifier... textures) {
        for (int i = 0; i < textures.length; i++) {
            Identifier texture = textures[i];
            // don't mess with the lightmap (1) nor the default texture (0)
            GlStateManager.activeTexture(i + GLX.GL_TEXTURE2);
            MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
            // start texture uniforms at 1, as 0 would be the default texture which doesn't require any special operation
            setUniform("texture" + (i + 1), i + 2);
        }
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    /**
     *
     * @return the gl name of the main framebuffer's depth texture
     */
    public static int getDepthTexture() {
        throw new UnsupportedOperationException("Depth texture replacement is not yet supported");
//        return FramebufferReplacement.getMainDepthTexture(); // TODO adapt this to the new rendering code
    }

    /**
     * A 16-sized float buffer that can be used to send data to shaders.
     * Do not use this buffer for long term data storage, it can be cleared at any time.
     */
    public static FloatBuffer getTempBuffer() {
        buffer.clear();
        return buffer;
    }

    public static FloatBuffer getProjectionMatrix() {
        FloatBuffer projection = getTempBuffer();
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projection);
        projection.rewind();
        return projection;
    }

    public static FloatBuffer getProjectionMatrixInverse() {
        FloatBuffer projection = ShaderHelper.getProjectionMatrix();
        FloatBuffer projectionInverse = buffer1;
        projectionInverse.clear();
        MatUtil.invertMat4FBFA(projectionInverse, projection);
        projection.rewind();
        projectionInverse.rewind();
        return projectionInverse;
    }

    /**
     * Call this once before doing any transform to get the <em>view</em> matrix, then load identity and call this again after
     * doing any rendering transform to get the <em>model</em> matrix for the rendered object
     */
    public static FloatBuffer getModelViewMatrix() {
        FloatBuffer modelView = getTempBuffer();
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView);
        modelView.rewind();
        return modelView;
    }

    public static FloatBuffer getModelViewMatrixInverse() {
        FloatBuffer modelView = ShaderHelper.getModelViewMatrix();
        FloatBuffer modelViewInverse = buffer1;
        modelViewInverse.clear();
        MatUtil.invertMat4FBFA(modelViewInverse, modelView);
        modelView.rewind();
        modelViewInverse.rewind();
        return modelViewInverse;
    }

    /**
     * Reverts to the previous shader used
     */
    public static void revert() {
        useShader(prevProgram);
    }


    /**
     * Loads and links all registered shaders
     *
     * @param resourceManager MinecraftClient's resource manager
     */
    private static void loadShaders(ResourceManager resourceManager) {
        if (areShadersDisallowed()) {
            return;
        }
        Map<Identifier, ShaderPair> registeredShaders = new HashMap<>();
        // TODO actually send the event dynamically
        BaseShaders.onShaderRegistry(new ShaderRegistryEvent(registeredShaders));
        registeredShaders.forEach((rl, sh) -> {
            try {
                int old = linkedShaders.put(rl, loadShader(resourceManager, sh.getVertex(), sh.getFragment()));
                if (old > 0) {
                    glDeleteProgram(old);
                }
            } catch (Exception e) {
                Satin.LOGGER.error(new FormattedMessage("Could not create shader {} from vertex {} and fragment {}", rl, sh.getVertex(), sh.getFragment(), e));
            }
        });
    }

    /**
     * Initializes a program with one or two shaders
     *
     * @param vertexLocation   the name or relative location of the vertex shader
     * @param fragmentLocation the name or relative location of the fragment shader
     * @return the reference to the initialized program
     */
    private static int loadShader(ResourceManager resourceManager, @Nullable Identifier vertexLocation, @Nullable Identifier fragmentLocation) throws IOException {

        // program creation
        int programId = GLX.glCreateProgram();

        int vertexShaderId = 0;
        int fragmentShaderId = 0;

        // vertex shader creation
        if (vertexLocation != null) {
            vertexShaderId = GLX.glCreateShader(GLX.GL_VERTEX_SHADER);
            ARBShaderObjects.glShaderSourceARB(vertexShaderId, fromFile(resourceManager, vertexLocation));
            ARBShaderObjects.glCompileShaderARB(vertexShaderId);
            ARBShaderObjects.glAttachObjectARB(programId, vertexShaderId);
            String log = glGetShaderInfoLog(vertexShaderId, 1024);
            if (!log.isEmpty()) {
                Satin.LOGGER.error("Could not compile vertex shader " + vertexLocation + ": " + log);
            }
        }

        // fragment shader creation
        if (fragmentLocation != null) {
            fragmentShaderId = GLX.glCreateShader(GLX.GL_FRAGMENT_SHADER);
            ARBShaderObjects.glShaderSourceARB(fragmentShaderId, fromFile(resourceManager, fragmentLocation));
            ARBShaderObjects.glCompileShaderARB(fragmentShaderId);
            ARBShaderObjects.glAttachObjectARB(programId, fragmentShaderId);
            String log = glGetShaderInfoLog(fragmentShaderId, 1024);
            if (!log.isEmpty()) {
                Satin.LOGGER.error("Could not compile fragment shader " + fragmentLocation + ": " + log);
            }
        }

        GLX.glLinkProgram(programId);
        // check potential linkage errors
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        // free up the vertex and fragment shaders
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
            glDeleteShader(vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
            glDeleteShader(fragmentShaderId);
        }

        // validate the program
        // this may fail even when it works fine so only show the message in a dev environment
        if (Satin.isDevEnv()) {
            glValidateProgram(programId);
            if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
                Satin.LOGGER.warn("Warning validating Shader code:" + glGetProgramInfoLog(programId, 1024));
            }
        }


        return programId;
    }

    /**
     * Reads a text file into a single String
     *
     * @param fileLocation the path to the file to read
     * @return a string with the content of the file
     */
    private static String fromFile(ResourceManager resourceManager, Identifier fileLocation) throws IOException {
        StringBuilder source = new StringBuilder();

        try (InputStream in = resourceManager.getResource(fileLocation).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append('\n');
            }
        }
        return source.toString();
    }

}