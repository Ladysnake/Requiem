package ladysnake.dissolution.client.renders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import ladysnake.dissolution.client.renders.entities.RenderPlayerCorpse;
import ladysnake.dissolution.common.DissolutionConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;

/**
 * Helper class for shader creation and usage
 * @author Pyrofab
 *
 */
public final class ShaderHelper {
	
	/**the shader used during the corpse dissolution animation*/
	public static int dissolution = 0;
	/**the shader used with the blue overlay*/
	public static int intangible = 0;
	public static int doppleganger = 0;
	public static int incorp = 0;
	
	private static int prevProgram = 0, currentProgram = 0;
	private static final String LOCATION_PREFIX = "/assets/dissolution/shaders/";
	
	static {
		initShaders();
	}
	
	public static boolean shouldUseShaders() {
		return OpenGlHelper.shadersSupported && DissolutionConfig.useShaders;
	}
	
	/**
	 * Initializes all known shaders
	 */
	public static void initShaders() {
		if(!shouldUseShaders())
			return;
		dissolution = initShader("corpsedissolution");
		intangible = initShader("intangible");
	}
	
	/**
	 * Initializes a program with two shaders having the same name
	 * @param shaderName the common name or relative location of both shaders, minus the file extension
	 * @return the reference to the initialized program
	 */
	public static int initShader(String shaderName) {
		return initShader(shaderName + ".vsh", shaderName + ".fsh");
	}
	
	/**
	 * Initializes a program with one or two shaders
	 * @param vertexLocation the name or relative location of the vertex shader
	 * @param fragmentLocation the name or relative location of the fragment shader
	 * @return the reference to the initialized program
	 */
	public static int initShader(String vertexLocation, String fragmentLocation) {

		// program creation
		int program = OpenGlHelper.glCreateProgram();
		
		// vertex shader creation
		if(vertexLocation != null && !vertexLocation.trim().isEmpty()) {
			int vertexShader = OpenGlHelper.glCreateShader(OpenGlHelper.GL_VERTEX_SHADER);
			ARBShaderObjects.glShaderSourceARB(vertexShader, fromFile(LOCATION_PREFIX + vertexLocation));
			OpenGlHelper.glCompileShader(vertexShader);
			OpenGlHelper.glAttachShader(program, vertexShader);
		}
		
		// fragment shader creation
		if(fragmentLocation != null && !fragmentLocation.trim().isEmpty()) {
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
	 * @param program the reference to the desired shader (0 to remove any current shader)
	 */
	public static void useShader(int program) {
		if(!shouldUseShaders())
			return;

		prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		OpenGlHelper.glUseProgram(program);
		
		currentProgram = program;
		
		setUniform("time", (int) Minecraft.getMinecraft().player.world.getWorldTime());
	}
	
	/**
	 * Sets the value of a uniform from the current program
	 * @param uniformName
	 * @param value an int value for this uniform
	 */
	public static void setUniform(String uniformName, int value) {
		if(!shouldUseShaders() || currentProgram == 0)
			return;

		int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
		if(uniform != -1)
			GL20.glUniform1i(uniform, value);
	}
	
	/**
	 * Sets the value of a uniform from the current program
	 * @param uniformName
	 * @param value a float value for this uniform
	 */
	public static void setUniform(String uniformName, float value) {
		if(!shouldUseShaders())
			return;

		int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
		if(uniform != -1)
			GL20.glUniform1f(uniform, value);
	}
	
	/**
	 * Reverts to the previous shader used
	 */
	public static void revert() {
		useShader(prevProgram);
	}
	
	/**
	 * Reads a text file into a single String
	 * @param filename the path to the file to read
	 * @return a string with the content of the file
	 */
	public static String fromFile(String filename) {
		StringBuilder source = new StringBuilder();
        
        try (InputStream in = RenderPlayerCorpse.class.getResourceAsStream(filename);
        		BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"))){
             
                String line;
                while((line = reader.readLine()) != null)
                    source.append(line).append('\n');
        } catch(IOException exc) {
            exc.printStackTrace();
        } catch (NullPointerException e) {
        	System.err.println(e + " : " + filename + " does not exist");
        }
         
       // System.out.println(source);
        return source.toString();
    }
	
	private ShaderHelper(){}

}
