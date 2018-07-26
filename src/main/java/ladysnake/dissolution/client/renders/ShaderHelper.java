package ladysnake.dissolution.client.renders;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.message.FormattedMessage;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for shader creation and usage
 *
 * @author Pyrofab
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Side.CLIENT)
public final class ShaderHelper {

    /**
     * the shader used during the corpse dissolution animation
     */
    public static int dissolution = 0;
    /**
     * the shader used with the blue overlay
     */
    public static int bloom = 0;

    private static int prevProgram = 0, currentProgram = 0;
    private static final String LOCATION_PREFIX = "/assets/dissolution/shaders/";
    private static final Map<ResourceLocation, ShaderGroup> screenShaders = new HashMap<>();
    private static boolean resetScreenShaders;
    private static int oldDisplayWidth = Minecraft.getMinecraft().displayWidth;
    private static int oldDisplayHeight = Minecraft.getMinecraft().displayHeight;

    static {
        initShaders();
    }

    private static boolean shouldUseShaders() {
        return OpenGlHelper.shadersSupported && Dissolution.config.client.useShaders;
    }

    /**
     * Initializes all known shaders
     */
    private static void initShaders() {
        if (!shouldUseShaders()) {
            return;
        }
        dissolution = initShader("vertex_base.vsh", "corpsedissolution.fsh");
        bloom = initShader("vertex_base.vsh", "bloom.fsh");
    }

    /**
     * Initializes a program with two shaders having the same name
     *
     * @param shaderName the common name or relative location of both shaders, minus the file extension
     * @return the reference to the initialized program
     */
    public static int initShader(String shaderName) {
        return initShader(shaderName + ".vsh", shaderName + ".fsh");
    }

    /**
     * Initializes a program with one or two shaders
     *
     * @param vertexLocation   the name or relative location of the vertex shader
     * @param fragmentLocation the name or relative location of the fragment shader
     * @return the reference to the initialized program
     */
    public static int initShader(String vertexLocation, String fragmentLocation) {

        // program creation
        int program = OpenGlHelper.glCreateProgram();

        // vertex shader creation
        if (vertexLocation != null && !vertexLocation.trim().isEmpty()) {
            int vertexShader = OpenGlHelper.glCreateShader(OpenGlHelper.GL_VERTEX_SHADER);
            ARBShaderObjects.glShaderSourceARB(vertexShader, fromFile(LOCATION_PREFIX + vertexLocation));
            OpenGlHelper.glCompileShader(vertexShader);
            OpenGlHelper.glAttachShader(program, vertexShader);
        }

        // fragment shader creation
        if (fragmentLocation != null && !fragmentLocation.trim().isEmpty()) {
            int fragmentShader = OpenGlHelper.glCreateShader(OpenGlHelper.GL_FRAGMENT_SHADER);
            ARBShaderObjects.glShaderSourceARB(fragmentShader, fromFile(LOCATION_PREFIX + fragmentLocation));
            OpenGlHelper.glCompileShader(fragmentShader);
            OpenGlHelper.glAttachShader(program, fragmentShader);
        }

        OpenGlHelper.glLinkProgram(program);

        return program;
    }

    /**
     * Sets the currently used program
     *
     * @param program the reference to the desired shader (0 to remove any current shader)
     */
    public static void useShader(int program) {
        if (!shouldUseShaders()) {
            return;
        }

        prevProgram = GlStateManager.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        OpenGlHelper.glUseProgram(program);

        currentProgram = program;

        setUniform("time", (int) Minecraft.getMinecraft().player.world.getWorldTime());
    }

    /**
     * Sets the value of a uniform from the current program
     *
     * @param uniformName the uniform's name
     * @param value       an int value for this uniform
     */
    public static void setUniform(String uniformName, int value) {
        if (!shouldUseShaders() || currentProgram == 0) {
            return;
        }

        int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
        if (uniform != -1) {
            GL20.glUniform1i(uniform, value);
        }
    }

    /**
     * Sets the value of a uniform from the current program
     *
     * @param uniformName the name of the uniform variable
     * @param values      one or more float values for this uniform
     */
    public static void setUniform(String uniformName, float... values) {
        if (!shouldUseShaders()) {
            return;
        }

        int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
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
            }
        }
    }

    /**
     * Reverts to the previous shader used
     */
    public static void revert() {
        useShader(prevProgram);
    }

    public static void enableScreenShader(ResourceLocation id) {
        if (shouldUseShaders() && !screenShaders.containsKey(id)) {
            try {
                Minecraft mc = Minecraft.getMinecraft();
                resetScreenShaders = true;
                screenShaders.put(id, new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), id));
            } catch (IOException e) {
                Dissolution.LOGGER.error(new FormattedMessage("Could not enable screen shader {}", id), e);
            }
        }
    }

    public static void disableScreenShader(ResourceLocation id) {
        if (screenShaders.containsKey(id)) {
            screenShaders.remove(id).deleteShaderGroup();
        }
    }

    @SubscribeEvent
    public static void renderScreenShaders(RenderGameOverlayEvent.Pre event) {
        if (shouldUseShaders() && !screenShaders.isEmpty() && event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            resetScreenShaders();
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            for (ShaderGroup shaderGroup : screenShaders.values()) {
                GlStateManager.pushMatrix();
                setScreenUniform(shaderGroup);
                shaderGroup.render(event.getPartialTicks());
                GlStateManager.popMatrix();
            }
            Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
        }
    }

    public static void setScreenUniform(ShaderGroup shaderGroup) {
        for (Shader shader : shaderGroup.listShaders) {
            shader.getShaderManager().getShaderUniformOrDefault("SystemTime").set(System.currentTimeMillis());
        }
    }

    private static void resetScreenShaders() {
        Minecraft mc = Minecraft.getMinecraft();
        if (resetScreenShaders || mc.displayWidth != oldDisplayWidth || oldDisplayHeight != mc.displayHeight) {
            for (ShaderGroup sg : screenShaders.values()) {
                sg.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            }

            oldDisplayWidth = mc.displayWidth;
            oldDisplayHeight = mc.displayHeight;
            resetScreenShaders = false;
        }
    }

    /**
     * Reads a text file into a single String
     *
     * @param filename the path to the file to read
     * @return a string with the content of the file
     */
    public static String fromFile(String filename) {
        StringBuilder source = new StringBuilder();

        try (InputStream in = ShaderHelper.class.getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append('\n');
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println(e + " : " + filename + " does not exist");
        }

        // System.out.println(source);
        return source.toString();
    }

    private ShaderHelper() {
    }

}
