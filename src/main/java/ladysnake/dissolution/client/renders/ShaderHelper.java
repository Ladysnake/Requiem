package ladysnake.dissolution.client.renders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import ladysnake.dissolution.client.renders.entities.RenderPlayerCorpse;
import net.minecraft.client.Minecraft;

public class ShaderHelper {
	
	public static int corpseDissolution = 0;
	private static int prevProgram = 0, currentProgram = 0;
	private static final String LOCATION_PREFIX = "/assets/dissolution/shaders/special/";
	
	static {
		initShaders();
	}
	
	/**
	 * Initializes all known shaders
	 */
	public static void initShaders() {
		corpseDissolution = initShader("corpsedissolution");
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
		int fragmentShader = 0;
		int vertexShader = 0;

		String vertexShaderSource = "";
		String fragmentShaderSource = "";
		// source code for the vertex shader
		if(!vertexLocation.trim().isEmpty())
			vertexShaderSource = fromFile(LOCATION_PREFIX + vertexLocation);
		// source code for the fragment shader
		if(!fragmentLocation.trim().isEmpty())
			fragmentShaderSource = fromFile(LOCATION_PREFIX + fragmentLocation);
		
		// vertex shader creation
		if(!vertexShaderSource.isEmpty()) {
			vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
			GL20.glShaderSource(vertexShader, vertexShaderSource);
			GL20.glCompileShader(vertexShader);
		}
		
		// fragment shader creation
		if(!fragmentShaderSource.isEmpty()) {
			fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
			GL20.glShaderSource(fragmentShader, fragmentShaderSource);
			GL20.glCompileShader(fragmentShader);
		}

		// program creation
		int program = GL20.glCreateProgram();
		if(fragmentShader != 0)
			GL20.glAttachShader(program, fragmentShader);
		if(vertexShader != 0)
			GL20.glAttachShader(program, vertexShader);

		GL20.glLinkProgram(program);
		
		return program;
	}
	
	public static void useShader(int program) {
		prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		GL20.glUseProgram(program);
		
		currentProgram = program;
		/*
		int time = GL20.glGetUniformLocation(program, "time");
		if(time != -1)
			GL20.glUniform1i(time, (int) Minecraft.getMinecraft().player.world.getWorldTime());*/
		setUniform("time", (int) Minecraft.getMinecraft().player.world.getWorldTime());
	}
	
	public static void setUniform(String uniformName, int value) {
		int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
		if(uniform != -1)
			GL20.glUniform1i(uniform, value);
	}
	
	public static void setUniform(String uniformName, float value) {
		int uniform = GL20.glGetUniformLocation(currentProgram, uniformName);
		if(uniform != -1)
			GL20.glUniform1f(uniform, value);
	}
	
	public static void revert() {
		GL20.glUseProgram(prevProgram);
	}
	
	public static String fromFile(String filename) {
		StringBuilder source = new StringBuilder();
        
        try (InputStream in = RenderPlayerCorpse.class.getResourceAsStream(filename);
        		BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"))){
             
                String line;
                while((line = reader.readLine()) != null)
                    source.append(line).append('\n');
        }
        catch(IOException exc) {
            exc.printStackTrace();
        }
         
        return source.toString();
    }

}
