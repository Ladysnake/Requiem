package ladysnake.dissolution.client.renders.entities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import ladysnake.dissolution.client.models.ModelMinionZombie;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

public class RenderPlayerCorpse extends RenderBiped<EntityPlayerCorpse> {
	
	private static CharSequence fragmentShaderSource, vertexShaderSource;
	private static int fragmentShader, vertexShader, program;
	private static int baseImageLoc, normalMapLoc, shadowMapLoc;
	
	static {
		initShader();
	}
	
	/**
	 * Initializes the shaders to be used and links them to a program
	 */
	public static void initShader() {
		// source code for the vertex shader
		vertexShaderSource =
				""
				+ "varying vec4 vPosition;"
				+ "varying vec4 vColor;"
				+ "uniform vec4 translation;"
				+ "void main(void) {"
				+ "	vec4 a = gl_Vertex;"
				+ "	gl_Position = gl_ModelViewProjectionMatrix * a;"
				+ "	vPosition = gl_Position;"
				+ "	vColor = gl_Color;"
				+ "}";
		
		// source code for the fragment shader
		fragmentShaderSource = 
				""
				+ "varying vec4 vPosition;"
				+ "varying vec4 vColor;"
				+ "uniform vec4 uColor;"
				+ "	void main (void) { "
				+ "	gl_FragColor = vColor;"
				+ "}";
		vertexShaderSource = fromFile("/assets/dissolution/shaders/special/corpsedissolution.vsh");
		fragmentShaderSource = fromFile("/assets/dissolution/shaders/special/corpsedissolution.fsh");
		
		// vertex shader creation
		vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		GL20.glShaderSource(vertexShader, vertexShaderSource);
		GL20.glCompileShader(vertexShader);
		
		// fragment shader creation
		fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		GL20.glShaderSource(fragmentShader, fragmentShaderSource);
		GL20.glCompileShader(fragmentShader);

		// program creation
		program = GL20.glCreateProgram();
		GL20.glAttachShader(program, fragmentShader);
		GL20.glAttachShader(program, vertexShader);
		GL20.glLinkProgram(program);
		
		baseImageLoc = GL20.glGetUniformLocation(program, "baseImage");
		normalMapLoc = GL20.glGetUniformLocation(program, "normalMap");
		shadowMapLoc = GL20.glGetUniformLocation(program, "shadowMap");
	}
	
	public RenderPlayerCorpse(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelMinionZombie(), 0.5F);
		LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this)
        {
            protected void initArmor()
            {
                this.modelLeggings = new ModelMinionZombie(0.5F, true);
                this.modelArmor = new ModelMinionZombie(1.0F, true);
            }
        };
        this.addLayer(layerbipedarmor);
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityPlayerCorpse entity) {
		return DefaultPlayerSkin.getDefaultSkinLegacy();
	}
	
	@Override
	protected void preRenderCallback(EntityPlayerCorpse entitylivingbaseIn, float partialTickTime) {
	}
	
	@Override
	public void doRender(EntityPlayerCorpse entity, double x, double y, double z, float entityYaw, float partialTicks) {
		//GlStateManager.pushAttrib();
		//GlStateManager.pushMatrix();
		
		int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		GL20.glUseProgram(program);
		
		int uniformFragment = GL20.glGetUniformLocation(program, "uColor");
		Random rand = entity.world.rand;
		if(uniformFragment != -1) {
			GL20.glUniform4f(uniformFragment, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
		}
		
		int uniformVertex = GL20.glGetUniformLocation(program, "translation");
		if(uniformVertex != -1) {
			GL20.glUniform4f(uniformVertex, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
		}
		
		this.bindEntityTexture(entity);
		//GL11.glBindTexture(GL11.GL_TEXTURE, );
		
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		GL20.glUseProgram(prevProgram);
		//GlStateManager.popMatrix();
		//GlStateManager.popAttrib();
	}
	
	public static CharSequence fromFile(String filename) {
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
	
	public static CharSequence fromFile2(String filename) {
		try (FileInputStream fis = new FileInputStream(filename)) {
		    FileChannel fc = fis.getChannel();
	
		    // Create a read-only CharBuffer on the file
		    ByteBuffer bbuf = fc.map(FileChannel.MapMode.READ_ONLY, 0,
		        (int) fc.size());
		    CharBuffer cbuf = Charset.forName("8859_1").newDecoder().decode(bbuf);
		    fis.close();
		    return cbuf;
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    return "";
	  }

}
